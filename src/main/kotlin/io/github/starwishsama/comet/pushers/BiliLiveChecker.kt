package io.github.starwishsama.comet.pushers

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BotVariables.cfg
import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.api.command.CommandExecutor.doFilter
import io.github.starwishsama.comet.api.thirdparty.bilibili.LiveApi
import io.github.starwishsama.comet.api.thirdparty.bilibili.MainApi
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.live.LiveRoomInfo
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import io.github.starwishsama.comet.utils.network.NetUtil
import io.github.starwishsama.comet.utils.verboseS
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.isContentNotEmpty
import net.mamoe.mirai.utils.uploadAsImage
import java.time.LocalDateTime
import java.util.concurrent.ScheduledFuture

object BiliLiveChecker : CometPusher {
    private val pushedList = mutableListOf<StoredLiveInfo>()
    override val delayTime: Long = cfg.biliInterval
    override val internal: Long = cfg.biliInterval
    override var future: ScheduledFuture<*>? = null
    override var bot: Bot? = null
    override var pushCount: Int = 0
    override var lastPushTime: LocalDateTime = LocalDateTime.now()

    override fun retrieve() {
        pushCount = 0

        val collectedUsers = mutableSetOf<Long>()

        BotVariables.perGroup.parallelStream().forEach {
            if (it.biliPushEnabled) {
                collectedUsers.plusAssign(it.biliSubscribers)
            }
        }

        collectedUsers.parallelStream().forEach { uid ->
            val roomId = LiveApi.getRoomIDByUID(uid)
            if (roomId > 0) {
                val data = LiveApi.getLiveInfo(roomId)?.data

                if (data != null) {
                    val sli = StoredLiveInfo(data, false)
                    if (pushedList.isEmpty() && data.isLiveNow()) {
                        pushedList.plusAssign(sli)
                        pushCount++
                    } else {
                        var hasOldData = false

                        for (i in pushedList.indices) {
                            val oldStatus = pushedList[i].data.liveStatus
                            val currentStatus = data.liveStatus
                            if (pushedList[i].data.roomId == uid) {
                                hasOldData = true
                                if (oldStatus != currentStatus && data.isLiveNow()) {
                                    pushedList[i] = sli
                                }
                                break
                            }
                        }

                        if (!hasOldData && data.isLiveNow()) {
                            pushedList.add(sli)
                            pushCount++
                        }
                    }
                }
            }
        }

        if (pushCount > 0) {
            daemonLogger.verboseS("Retrieve success, have collected $pushCount liver(s)!")
        }

        push()
    }

    override fun push() {
        val liverToGroups = mutableMapOf<StoredLiveInfo, MutableSet<Long>>()
        pushedList.parallelStream().forEach { liverToGroups.plusAssign(it to mutableSetOf()) }

        BotVariables.perGroup.parallelStream().forEach { cfg ->
            if (cfg.biliPushEnabled) {
                liverToGroups.forEach {
                    if (cfg.biliSubscribers.contains(it.key.getRoomId())) {
                        it.value.plusAssign(cfg.id)
                    }
                }
            }
        }

        val count = pushToGroups(liverToGroups)
        if (count > 0) daemonLogger.verboseS("Push bili info success, have pushed $count group(s)!")
    }

    private fun pushToGroups(pushQueue: MutableMap<StoredLiveInfo, MutableSet<Long>>): Int {
        var count = 0

        /** 遍历推送列表推送开播消息 */
        pushQueue.forEach { (info, pushGroups) ->
            if (!info.isPushed) {
                val data = info.data
                if (data.liveStatus != 0) {
                    val msg = "单推助手 > ${MainApi.getUserNameByMid(data.uid)} 正在直播!" +
                            "\n直播间标题: ${data.title}" +
                            "\n开播时间: ${data.liveTime}" +
                            "\n传送门: ${data.getRoomURL()}"
                    pushGroups.forEach {
                        val filtered = msg.convertToChain().doFilter()
                        if (filtered.isContentNotEmpty()) {
                            runBlocking {
                                try {
                                    val group = bot?.getGroup(it)
                                    val image = group?.let { sendGroup -> NetUtil.executeHttpRequest(url = data.keyFrameImageUrl, autoClose = true).body()?.byteStream()?.uploadAsImage(sendGroup) }
                                    group?.sendMessage(filtered + (image ?: PlainText("")))
                                    count++
                                    delay(2_500)
                                } catch (t: Throwable) {
                                    daemonLogger.verboseS("推送时出现了异常, ${t.message}")
                                }
                            }
                        }
                    }
                }
                info.isPushed = true
            }
        }

        lastPushTime = LocalDateTime.now()

        return count
    }

    data class StoredLiveInfo(val data: LiveRoomInfo.LiveRoomInfoData, var isPushed: Boolean) {
        fun getRoomId() = data.roomId
    }
}