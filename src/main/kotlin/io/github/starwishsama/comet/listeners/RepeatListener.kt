package io.github.starwishsama.comet.listeners

import cn.hutool.core.util.RandomUtil
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.managers.GroupConfigManager
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.isBotMuted
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.data.*
import kotlin.time.ExperimentalTime

object RepeatListener : NListener {
    @ExperimentalTime
    override fun register(bot: Bot) {
        bot.subscribeGroupMessages {
            always {
                if (BotVariables.switch && !group.isBotMuted && canRepeat(group.id)) {
                    handleRepeat(this, RandomUtil.randomDouble())
                }
            }
        }
    }

    private suspend fun handleRepeat(event: GroupMessageEvent, chance: Double) {
        if (event.message[QuoteReply] == null && chance in 0.50..0.508) {
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

                event.reply(msgChain.asMessageChain())
            }
        }
    }

    private fun canRepeat(groupId: Long): Boolean {
        val cfg = GroupConfigManager.getConfig(groupId)
        return cfg?.doRepeat ?: true
    }

    override fun getName(): String = "复读机"
}