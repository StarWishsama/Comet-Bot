package io.github.starwishsama.comet.listeners

import cn.hutool.core.util.RandomUtil
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BotVariables.cfg
import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.managers.GroupConfigManager
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.isBotMuted
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.MiraiExperimentalApi
import kotlin.time.ExperimentalTime

object RepeatListener : NListener {
    override val eventToListen = listOf(GroupMessageEvent::class)

    @MiraiExperimentalApi
    @ExperimentalTime
    override fun listen(event: Event) {
        if (BotVariables.switch && event is GroupMessageEvent && !event.group.isBotMuted && canRepeat(event.group.id)) {
            runBlocking { handleRepeat(event, RandomUtil.randomDouble()) }
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

                event.subject.sendMessage(msgChain.toMessageChain())
            }
        }
    }

    private fun canRepeat(groupId: Long): Boolean {
        return try {
            GroupConfigManager.getConfigOrNew(groupId).doRepeat
        } catch (e: NullPointerException) {
            daemonLogger.warning("检测到群 $groupId 的配置文件异常无法获取, 请及时查看!")
            false
        }
    }

    override fun getName(): String = "复读机"
}