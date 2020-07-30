package io.github.starwishsama.comet.tasks

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.api.bilibili.BiliBiliApi
import io.github.starwishsama.comet.api.bilibili.FakeClientApi
import io.github.starwishsama.comet.managers.GroupConfigManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.collections.HashMap

object BiliDynamicChecker : CometPusher {
    /** 推送过的直播间列表, 避免重复推送 */
    private val pushedList = HashMap<Long, HashSet<Long>>()
    override val delayTime: Long = 5
    override val cycle: Long = 10

    override fun retrieve() {
        val readyToRetrieveList = mutableMapOf<Long, LinkedList<Long>>()

        BotVariables.perGroup.forEach { cfg ->
            cfg.biliSubscribers.forEach { roomId ->
                readyToRetrieveList.forEach { (id, u) ->
                    run {
                        if (id == roomId) {
                            u.add(cfg.id)
                        }
                    }
                }
            }
        }

        readyToRetrieveList.forEach { (roomId, list) ->
            run {
                val data = runBlocking {
                    FakeClientApi.getLiveRoom(roomId)?.data
                }

                if (data != null) {
                    when (data.liveStatus) {
                        0 -> {
                            /** 如果下播了就删除, 等待下一次开播后做提醒 */
                            if (pushedList.contains(roomId)) {
                                pushedList.remove(roomId)
                            }
                        }
                        1 -> {
                            if (!pushedList.contains(roomId)) {
                                pushedList[roomId]?.addAll(list)
                            }
                        }
                    }
                }
            }
        }

        push()
    }

    override fun push() {
        pushedList.forEach { (roomId, set) ->
            run {
                val data = runBlocking {
                    FakeClientApi.getLiveRoom(roomId)?.data
                }
                val msg = "单推助手 > \n${data?.uid?.let { BiliBiliApi.getUserNameByMid(it) }} 开播了!" +
                        "\n标题: ${data?.title}" +
                        "\n开播时间: ${data?.liveTime}" +
                        "\n传送门: https://live.bilibili.com/${data?.roomId}"

                set.forEach { groupId ->
                    if (GroupConfigManager.getConfigSafely(groupId).biliPushEnabled) {
                        runBlocking {
                            val group = BotVariables.bot.getGroup(groupId)
                            group.sendMessage(msg)
                            delay(2_000)
                        }
                    }
                }
            }
        }
    }

    data class CachedUser(val id: Long, var isPushed: Boolean)
}