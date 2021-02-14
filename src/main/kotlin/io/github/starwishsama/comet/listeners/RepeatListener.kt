package io.github.starwishsama.comet.listeners

import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.managers.GroupConfigManager
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.isBotMuted
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.time.ExperimentalTime

object RepeatListener : NListener {
    override val eventToListen = listOf(GroupMessageEvent::class)

    private val repeatCachePool = mutableMapOf<Long, RepeatInfo>()

    @ExperimentalTime
    override fun listen(event: Event) {
        if (event is GroupMessageEvent && !event.group.isBotMuted && canRepeat(event.group.id)) {
            val groupId = event.group.id
            val repeatInfo = repeatCachePool[groupId]

            if (repeatInfo == null) {
                repeatCachePool[groupId] = RepeatInfo(
                    mutableListOf(RepeatInfo.CacheMessage(
                        event.sender.id,
                        event.message
                    ))
                )
                return
            }

            if (repeatInfo.check(event.sender.id, event.message)) {
                runBlocking { event.subject.sendMessage(doRepeat(repeatInfo.messageCache.last().message)) }
                repeatInfo.messageCache.clear()
            }
        }
    }

    private fun doRepeat(message: MessageChain): MessageChain {
        // 避免复读过多图片刷屏
        val count = message.parallelStream().filter { it is Image }.count()

        if (count <= 1L) {
            val msgChain = ArrayList<Message>()

            message.forEach {
                val msg: SingleMessage = if (it is PlainText) {
                    PlainText(it.content.replace("我".toRegex(), "你"))
                } else {
                    it
                }
                msgChain.add(msg)
            }

            return msgChain.toMessageChain()
        }

        return EmptyMessageChain
    }

    private fun canRepeat(groupId: Long): Boolean {
        return try {
            GroupConfigManager.getConfigOrNew(groupId).canRepeat
        } catch (e: NullPointerException) {
            daemonLogger.warning("检测到群 $groupId 的配置文件异常无法获取, 请及时查看!")
            false
        }
    }

    override fun getName(): String = "复读机"
}

data class RepeatInfo(
    val messageCache: MutableList<CacheMessage> = Collections.synchronizedList(mutableListOf())
) {
    data class CacheMessage(
        val senderId: Long,
        val message: MessageChain
    )

    fun check(id: Long, message: MessageChain): Boolean {
        if (messageCache.isEmpty()) {
            messageCache.add(CacheMessage(id, message))
            return false
        }

        val last = messageCache.last()


        if (last.senderId == id || last.message.contentToString() != message.contentToString()) {
            messageCache.clear()
            return false
        }

        if (last.senderId != id && last.message.contentToString() == message.contentToString()) {
            messageCache.add(CacheMessage(id, message))
        }

        if (messageCache.size > 1) {
            return true
        }

        return false
    }
}