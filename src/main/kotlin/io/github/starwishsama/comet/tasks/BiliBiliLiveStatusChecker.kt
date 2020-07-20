package io.github.starwishsama.comet.tasks

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.api.bilibili.BiliBiliApi
import io.github.starwishsama.comet.api.bilibili.FakeClientApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

@Deprecated("Will be refactor soon")
object BiliBiliLiveStatusChecker : Runnable {
    /** 推送过的直播间列表, 避免重复推送 */
    private val pushedList = mutableSetOf<Long>()

    override fun run() {
        if (BotVariables.cfg.subList.isNotEmpty() && BotVariables.cfg.pushGroups.isNotEmpty()) {
            BotVariables.cfg.subList.forEach { roomId ->
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
                                val msg = "单推助手 > \n${BiliBiliApi.getUserNameByMid(data.uid)} 开播了!" +
                                        "\n标题: ${data.title}" +
                                        "\n开播时间: ${data.liveTime}" +
                                        "\n传送门: https://live.bilibili.com/${data.roomId}"
                                BotVariables.bot.groups.forEach { group ->
                                    runBlocking {
                                        if (BotVariables.cfg.pushGroups.contains(group.id)) {
                                            group.sendMessage(msg)
                                        }
                                        /** 防止消息发送失败 */
                                        delay(2_000)
                                    }
                                }
                            }
                            pushedList.add(roomId)
                        }
                    }
                }
            }
        }
    }
}