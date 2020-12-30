package io.github.starwishsama.comet.listeners

import cn.hutool.core.util.RandomUtil
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BotVariables.cfg
import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.managers.GroupConfigManager
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.isBotMuted
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.MiraiExperimentalApi
import kotlin.time.ExperimentalTime

object RepeatListener : NListener {
    @MiraiExperimentalApi
    @ExperimentalTime
    override fun register(bot: Bot) {
        bot.eventChannel.subscribeGroupMessages {
            always {
                if (BotVariables.switch && !group.isBotMuted && canRepeat(group.id)) {
                    handleRepeat(this, RandomUtil.randomDouble())
                }
            }
        }
    }

    @MiraiExperimentalApi
    private suspend fun handleRepeat(event: GroupMessageEvent, chance: Double) {
        if (event.message[QuoteReply] == null && chance in 0.50..0.505) {
            cfg.commandPrefix.forEach {
                if (event.message.contentToString().startsWith(it)) {
                    return
                }
            }

            // 避免复读过多图片刷屏
            val count = event.message.parallelStream().filter { it is Image }.count()

            if (count <= 1L) {
                val msgChain = ArrayList<Message>()

                event.message.forEach {
                    val msg: SingleMessage = if (it is PlainText) {
                        PlainText(it.content.replace("我".toRegex(), "你"))
                    } else {
                        it
                    }
                    msgChain.add(msg)
                }

                event.subject.sendMessage(msgChain.asMessageChain())
            }
        }
    }

    private fun canRepeat(groupId: Long): Boolean {
        return try {
            GroupConfigManager.getConfig(groupId)?.doRepeat ?: true
        } catch (e: NullPointerException) {
            daemonLogger.warning("检测到群 $groupId 的配置文件异常无法获取, 请及时查看!")
            false
        }
    }

    override fun getName(): String = "复读机"
}