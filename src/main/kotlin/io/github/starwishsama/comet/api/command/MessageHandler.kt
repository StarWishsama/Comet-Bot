/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.api.command

import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.api.command.interfaces.CallbackCommand
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.sessions.SessionHandler
import io.github.starwishsama.comet.utils.CometUtil.toChain
import io.github.starwishsama.comet.utils.NumberUtil.fixDisplay
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import io.github.starwishsama.comet.utils.StringUtil.getLastingTimeAsString
import io.github.starwishsama.comet.utils.StringUtil.limitStringSize
import io.github.starwishsama.comet.utils.doFilter
import io.github.starwishsama.comet.utils.network.NetUtil
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.isBotMuted
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.isContentEmpty
import java.time.LocalDateTime

/**
 * 彗星 Bot 消息处理器
 *
 * 处理聊天信息和后台中存在的命令
 *
 * @author StarWishsama
 */
object MessageHandler {
    fun startHandler(bot: Bot) {
        bot.eventChannel.subscribeMessages {
            always {
                val executedTime = LocalDateTime.now()
                if (sender.id != 80000000L) {
                    if (this is GroupMessageEvent && group.isBotMuted) return@always

                    val result = dispatchCommand(this)

                    try {
                        val filtered = result.msg.doFilter()

                        if (result.status.isOk() && !filtered.isContentEmpty()) {
                            val receipt = this.subject.sendMessage(filtered)

                            if (result.cmd is CallbackCommand) {
                                result.cmd.handleReceipt(receipt)
                            }
                        }
                    } catch (e: IllegalArgumentException) {
                        CometVariables.logger.warning("正在尝试发送空消息, 执行的命令为 $result")
                    }

                    if (result.status.isOk()) {
                        CometVariables.logger.debug(
                            "[命令] 命令执行耗时 ${executedTime.getLastingTimeAsString(msMode = true)}, 执行结果: ${result.status.name}"
                        )
                    }
                }
            }
        }
    }

    /**
     * 执行消息中的命令
     *
     * @param event Mirai 消息命令 (聊天)
     */
    private suspend fun dispatchCommand(event: MessageEvent): ExecutedResult {
        val senderId = event.sender.id
        val message = event.message.contentToString()

        val cmd = CommandManager.getCommand(CommandManager.getCommandName(message))

        val useDebug = cmd?.props?.name?.contentEquals("debug") == true

        val user = CometUser.getUserOrRegister(senderId)

        if (!CometVariables.switch && !useDebug) {
            return ExecutedResult(EmptyMessageChain, cmd, CommandStatus.CometIsClose())
        } else {
            try {
                /**
                 * 如果不是监听会话, 则停止尝试解析并执行可能的命令
                 * 反之在监听时仍然可以执行命令
                 */
                if (SessionHandler.handleSessions(event, user)) {
                    return ExecutedResult(EmptyMessageChain, cmd, CommandStatus.PassToSession())
                }

                /**
                 * 命令不存在
                 */
                if (cmd == null) {
                    return ExecutedResult(EmptyMessageChain, null, CommandStatus.NotACommand())
                }

                val useStatus = validateUseStatus(user, cmd.props)

                /**
                 * 检查是否在尝试执行被禁用命令
                 */
                if (event is GroupMessageEvent && cmd.props.isDisabledCommand(event.group.id)
                ) {
                    return if (useStatus) {
                        ExecutedResult(toChain("该命令已被管理员禁用"), cmd, CommandStatus.Disabled())
                    } else {
                        ExecutedResult(EmptyMessageChain, cmd, CommandStatus.Disabled())
                    }
                }

                if (!useStatus) {
                    return if (cmd.props.consumerType == CommandExecuteConsumerType.POINT) {
                        val response = CometVariables.localizationManager.getLocalizationText("message.no-enough-point")
                            .replace("%point%", user.checkInPoint.fixDisplay())
                            .replace("%cost%", cmd.props.consumePoint.fixDisplay())
                        ExecutedResult(response.toChain(), cmd, CommandStatus.ValidateFailed())
                    } else {
                        ExecutedResult(EmptyMessageChain, cmd, CommandStatus.ValidateFailed())
                    }
                }

                val prefix = CommandManager.getCommandPrefix(message)

                if (prefix.isNotEmpty()) {
                    // 前缀末尾下标
                    val index = message.indexOf(prefix) + prefix.length

                    val tempMessage = message.substring(index, message.length).trim().split(" ")
                    val splitMessage = tempMessage.subList(1, tempMessage.size)

                    CometVariables.logger.debug("[命令] $senderId 尝试执行命令: ${cmd.name} (原始消息: ${tempMessage}, 解析消息: ${splitMessage})")

                    val status: CommandStatus

                    /** 检查是否有权限执行命令 */
                    val result: MessageChain = if (cmd.hasPermission(user, event)) {
                        status = CommandStatus.Success()
                        cmd.execute(event, splitMessage, user)
                    } else {
                        status = CommandStatus.NoPermission()
                        CometVariables.localizationManager.getLocalizationText("message.no-permission").toChain()
                    }

                    return ExecutedResult(result, cmd, status)
                } else {
                    return ExecutedResult(EmptyMessageChain, cmd, CommandStatus.NotACommand())
                }
            } catch (e: Exception) {
                return if (NetUtil.isTimeout(e)) {
                    CometVariables.logger.warning("执行网络操作失败: ", e)
                    ExecutedResult("Bot > 在执行网络操作时连接超时: ${e.message ?: ""}".convertToChain(), cmd)
                } else {
                    CometVariables.logger.warning("[命令] 在试图执行命令时发生了一个错误, 原文: ${message}, 发送者: $senderId", e)
                    if (user.isBotOwner()) {
                        ExecutedResult(
                            toChain(
                                "在试图执行命令时发生了一个错误\n简易报错信息 :\n${e.javaClass.name}: ${e.message?.limitStringSize(30)}"
                            ), cmd
                        )
                    } else {
                        ExecutedResult(toChain("在试图执行命令时发生了一个错误, 请联系管理员"), cmd)
                    }
                }
            }
        }
    }

    /**
     * 判断指定用户是否可以执行命令
     * (可以自定义命令冷却时间)
     *
     * @author StarWishsama
     * @param user 检测用户
     * @param props 命令配置
     *
     * @return 目标用户是否可以执行命令
     */
    private fun validateUseStatus(user: CometUser, props: CommandProps): Boolean {
        if (user.isBotOwner()) {
            return true
        }

        return when (props.consumerType) {
            CommandExecuteConsumerType.COOLDOWN -> {
                user.checkCoolDown(coolDown = props.consumePoint.toInt())
            }
            CommandExecuteConsumerType.POINT -> {
                if (user.checkInPoint >= props.consumePoint) {
                    user.checkInPoint -= props.consumePoint
                    true
                } else {
                    false
                }
            }
            CommandExecuteConsumerType.NONE -> true
        }
    }

    data class ExecutedResult(
        val msg: MessageChain,
        val cmd: ChatCommand?,
        val status: CommandStatus = CommandStatus.Failed()
    )

    sealed class CommandStatus(val name: String, private val isSuccessful: Boolean) {
        class Success : CommandStatus("成功", true)
        class NoPermission : CommandStatus("无权限", true)
        class Failed : CommandStatus("失败", true)
        class Disabled : CommandStatus("命令被禁用", true)
        class PassToSession : CommandStatus("移交会话处理", false)
        class NotACommand : CommandStatus("非命令", false)
        class CometIsClose : CommandStatus("Comet 已关闭", false)
        class ValidateFailed : CommandStatus("冷却/无积分", true)

        fun isOk(): Boolean = this.isSuccessful
    }
}