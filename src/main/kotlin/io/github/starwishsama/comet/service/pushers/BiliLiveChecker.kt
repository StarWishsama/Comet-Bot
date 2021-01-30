package io.github.starwishsama.comet.service.pushers

import io.github.starwishsama.comet.BotVariables.cfg
import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.BotVariables.perGroup
import io.github.starwishsama.comet.api.command.CommandExecutor.doFilter
import io.github.starwishsama.comet.api.thirdparty.bilibili.BiliBiliMainApi
import io.github.starwishsama.comet.api.thirdparty.bilibili.LiveApi
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.live.LiveRoomInfo
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import io.github.starwishsama.comet.utils.network.NetUtil
import io.github.starwishsama.comet.utils.verboseS
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.isContentEmpty
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import java.time.LocalDateTime
import java.util.concurrent.ScheduledFuture

object BiliLiveChecker : CometPusher {
    private val pendingPushContents = mutableSetOf<StoredLiveInfo>()
    @Suppress("DEPRECATION")
    override val delayTime: Long = cfg.biliInterval
    @Suppress("DEPRECATION")
    override val internal: Long = cfg.biliInterval
    override var future: ScheduledFuture<*>? = null
    override var bot: Bot? = null
    override var pushCount: Int = 0
    override var lastPushTime: LocalDateTime = LocalDateTime.now()

    override fun retrieve() {
        pushCount = 0

        val collectedUsers = mutableSetOf<Long>()

        perGroup.forEach {
            if (it.biliPushEnabled) {
                collectedUsers.addAll(it.biliSubscribers)
            }
        }

        collectedUsers.forEach { uid ->
            val roomId = LiveApi.getRoomIDByUID(uid)
            if (roomId > 0) {
                val data = LiveApi.getLiveInfo(roomId)?.data ?: return@forEach

                val sli = StoredLiveInfo(data, false)
                if (pendingPushContents.isEmpty() && data.isLiveNow()) {
                    pendingPushContents.plusAssign(sli)
                    pushCount++
                } else {
                    pendingPushContents.forEach pushList@ {
                        val oldStatus = it.data.liveStatus
                        val currentStatus = data.liveStatus
                        if (it.data.roomId == uid) {
                            if (oldStatus != currentStatus && data.isLiveNow()) {
                                it.data = sli.data
                                it.isPushed = false
                                pendingPushContents.add(sli)
                                pushCount++
                            }
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
        pendingPushContents.forEach { liverToGroups.plusAssign(it to mutableSetOf()) }

        perGroup.forEach { cfg ->
            if (cfg.biliPushEnabled) {
                liverToGroups.forEach { (slf, groups) ->
                    if (cfg.biliSubscribers.contains(slf.getRoomId())) {
                        groups.add(cfg.id)
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
                    val msg = "单推助手 > ${BiliBiliMainApi.getUserNameByMid(data.uid)} 正在直播!" +
                            "\n直播间标题: ${data.title}" +
                            "\n开播时间: ${data.liveTime}" +
                            "\n传送门: ${data.getRoomURL()}"
                    pushGroups.forEach {
                        val filtered = msg.convertToChain().doFilter()
                        if (!filtered.isContentEmpty()) {
                            runBlocking {
                                try {
                                    val group = bot?.getGroup(it)
                                    val image = group?.let { sendGroup ->
                                        NetUtil.executeHttpRequest(
                                            url = data.keyFrameImageUrl,
                                            autoClose = true
                                        ).body?.byteStream()?.uploadAsImage(sendGroup) }
                                    group?.sendMessage(filtered + (image ?: EmptyMessageChain))
                                    count++
                                    delay(2_500)
                                } catch (t: Exception) {
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

    data class StoredLiveInfo(var data: LiveRoomInfo.LiveRoomInfoData, var isPushed: Boolean) {
        fun getRoomId() = data.roomId
    }
}