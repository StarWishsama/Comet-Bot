package io.github.starwishsama.comet.listeners

import cn.hutool.core.util.RandomUtil
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.managers.TaskManager
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.BotIsBeingMutedException
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.asHumanReadable
import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

object RepeatListener : NListener {
    private var repeatTimes = 0

    init {
        TaskManager.runScheduleTaskAsync({
            repeatTimes = 0
        }, 1, 1, TimeUnit.MINUTES)
    }

    @ExperimentalTime
    override fun register(bot: Bot) {
        bot.subscribeGroupMessages {
            always {
                if (BotVariables.switch) {
                    try {
                        handleRepeat(this, RandomUtil.randomDouble())
                    } catch (e: BotIsBeingMutedException) {
                        BotVariables.logger.debug("[监听器] 机器人已被禁言, ${e.target.botMuteRemaining.seconds.asHumanReadable}s")
                    }
                }
            }
        }
    }

    private suspend fun handleRepeat(event: GroupMessageEvent, chance: Double) {
        if (repeatTimes <= 50 && event.message[QuoteReply] == null && chance in 0.9001..0.9002) {
            // 避免复读过多图片刷屏
            val count = event.message.stream().filter { it is Image }.count()

            if (count == 1L) {
                val msgChain = ArrayList<Message>()

                event.message.forEach { msgChain.add(it) }

                for (i in 0 until msgChain.size) {
                    val chain = msgChain[i]
                    if (chain is PlainText) {
                        msgChain[i] = PlainText(chain.content.replace("我", "你"))
                    }
                }
                repeatTimes += 1
                event.reply(msgChain.asMessageChain())
            }
        }
    }

    override fun getName(): String = "复读机"
}