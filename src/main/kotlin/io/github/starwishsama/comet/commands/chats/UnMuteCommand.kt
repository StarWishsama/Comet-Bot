/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.commands.chats

import io.github.starwishsama.comet.CometVariables.localizationManager
import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.objects.enums.UserLevel
import io.github.starwishsama.comet.service.command.MuteService
import io.github.starwishsama.comet.utils.CometUtil
import io.github.starwishsama.comet.utils.CometUtil.toChain
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain

class UnMuteCommand : ChatCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: CometUser): MessageChain {
        if (!hasPermission(user, event)) {
            return localizationManager.getLocalizationText("message.no-permission").toChain()
        }

        return if (event is GroupMessageEvent) {
            if (args.isNotEmpty()) {
                val at = CometUtil.parseAtAsBotUser(event, args[0])

                return if (at != null) {
                    MuteService.doMute(event.group, at.id, 0, false)
                } else {
                    getHelp().convertToChain()
                }
            } else {
                getHelp().convertToChain()
            }
        } else {
            "仅限群聊使用".toChain()
        }
    }

    override val props: CommandProps =
        CommandProps("unmute", arrayListOf("jj", "解禁", "解除禁言"), "解除禁言", UserLevel.USER)

    override fun getHelp(): String = """
        /unmute [@/QQ] 解除禁言
    """.trimIndent()

    private fun hasPermission(user: CometUser, e: MessageEvent): Boolean {
        if (e is GroupMessageEvent) {
            if (e.sender.permission >= MemberPermission.ADMINISTRATOR) return true
            val cfg = GroupConfigManager.getConfigOrNew(e.group.id)
            if (cfg.isHelper(e.sender.id)) return true
        }
        return user.hasPermission(props.permission)
    }
}