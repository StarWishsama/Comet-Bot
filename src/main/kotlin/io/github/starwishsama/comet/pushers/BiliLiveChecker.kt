package io.github.starwishsama.comet.pushers

import com.hiczp.bilibili.api.live.model.RoomInfo
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BotVariables.bot
import io.github.starwishsama.comet.api.bilibili.BiliBiliApi
import io.github.starwishsama.comet.api.bilibili.FakeClientApi
import io.github.starwishsama.comet.commands.CommandExecutor.doFilter
import io.github.starwishsama.comet.utils.convertToChain
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.getGroupOrNull
import net.mamoe.mirai.message.data.isContentNotEmpty
import java.util.concurrent.ScheduledFuture

object BiliLiveChecker : CometPusher {
    private val pushedList = mutableListOf<StoredLiveInfo>()
    override val delayTime: Long = 1
    override val cycle: Long = 1
    override var future: ScheduledFuture<*>? = null

    override fun retrieve() {
        if (!bot.isOnline) future?.cancel(false)

        val collectedUsers = mutableSetOf<Long>()

        BotVariables.perGroup.parallelStream().forEach {
            if (it.biliPushEnabled) {
                collectedUsers.plusAssign(it.biliSubscribers)
            }
        }

        collectedUsers.parallelStream().forEach { roomId ->
            val data = runBlocking { FakeClientApi.getLiveRoom(roomId) }
            if (data != null) {
                val sli = StoredLiveInfo(data.data, false)
                if (pushedList.isEmpty() && data.data.liveStatus == 1) {
                    pushedList.plusAssign(sli)
                } else {
                    var hasOldData = false
                    for (i in pushedList.indices) {
                        if (pushedList[i].data.roomId == roomId) {
                            hasOldData = true
                            if (pushedList[i].data.liveStatus != data.data.liveStatus) pushedList[i] = sli
                            break
                        }
                    }

                    if (!hasOldData && data.data.liveStatus == 1) {
                        pushedList.add(sli)
                    }
                }
            }
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

        pushToGroups(liverToGroups)
    }

    private fun pushToGroups(pushQueue: MutableMap<StoredLiveInfo, MutableSet<Long>>) {
        /** 遍历推送列表推送开播消息 */
        pushQueue.forEach { (info, pushGroups) ->
            if (!info.isPushed) {
                val data = info.data
                if (data.liveStatus != 0) {
                    val msg = "单推助手 > \n${BiliBiliApi.getUserNameByMid(data.uid)} 正在直播!" +
                            "\n直播间标题: ${data.title}" +
                            "\n开播时间: ${data.liveTime}" +
                            "\n传送门: https://live.bilibili.com/${data.roomId}"
                    pushGroups.forEach {
                        val filtered = msg.convertToChain().doFilter()
                        if (filtered.isContentNotEmpty()) {
                            runBlocking {
                                bot.getGroupOrNull(it)?.sendMessage(filtered)
                                delay(2_500)
                            }
                        }
                    }
                }
                info.isPushed = true
            }
        }
    }

    data class StoredLiveInfo(val data: RoomInfo.Data, var isPushed: Boolean) {
        fun getRoomId() = data.roomId
    }
}