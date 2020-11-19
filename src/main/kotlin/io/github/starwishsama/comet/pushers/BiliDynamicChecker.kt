package io.github.starwishsama.comet.pushers

import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.BotVariables.perGroup
import io.github.starwishsama.comet.api.thirdparty.bilibili.MainApi
import io.github.starwishsama.comet.exceptions.ApiException
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import io.github.starwishsama.comet.utils.verboseS
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot
import java.util.concurrent.ScheduledFuture

object BiliDynamicChecker : CometPusher {
    private val pushedList = mutableSetOf<PushDynamicHistory>()
    override val delayTime: Long = 3
    override val internal: Long = 3
    override var future: ScheduledFuture<*>? = null
    override var bot: Bot? = null

    override fun retrieve() {
        var count = 0

        val collectedUsers = mutableSetOf<Long>()

        perGroup.parallelStream().forEach {
            if (it.biliPushEnabled) {
                collectedUsers.plusAssign(it.biliSubscribers)
            }
        }

        collectedUsers.parallelStream().forEach { uid ->
            val data: MessageWrapper? = runBlocking {
                try {
                    MainApi.getUserDynamicTimeline(uid)
                } catch (e: RuntimeException) {
                    if (e !is ApiException) {
                        daemonLogger.warning("在获取动态时出现了异常", e)
                    }
                    return@runBlocking null
                }
            }

            if (data != null && data.success) {
                if (pushedList.isEmpty()) {
                    pushedList.plusAssign(PushDynamicHistory(uid, data))
                    count++
                } else {
                    val target = pushedList.parallelStream().filter { it.uid == uid }.findFirst()

                    if (!target.isPresent) {
                        pushedList.plusAssign(PushDynamicHistory(uid, data))
                    } else {
                        val oldData =
                            pushedList.parallelStream().filter { it.uid == uid && data.text == it.pushContent.text }
                                .findFirst()

                        if (!oldData.isPresent && target.isPresent) {
                            target.get().pushContent = data
                            target.get().isPushed = false
                        }
                    }
                }
            }
        }

        push()
    }

    override fun push() {
        pushedList.parallelStream().forEach { pdh ->
            perGroup.parallelStream().forEach { cfg ->
                if (cfg.biliPushEnabled) {
                    cfg.biliSubscribers.parallelStream().forEach { uid ->
                        if (pdh.uid == uid) {
                            pdh.target.plusAssign(cfg.id)
                        }
                    }
                }
            }
        }

        val count = pushToGroups()
        if (count > 0) daemonLogger.verboseS("Push bili dynamic success, have pushed $count group(s)!")
    }

    private fun pushToGroups(): Int {
        var count = 0

        /** 遍历推送列表推送开播消息 */
        pushedList.parallelStream().forEach { pdh ->
            if (!pdh.isPushed) {
                pdh.target.forEach target@ { gid ->
                    try {
                        runBlocking {
                            val group = bot?.getGroup(gid)
                            group?.sendMessage(
                                    "${MainApi.getUserNameByMid(pdh.uid)} ".convertToChain() + pdh.pushContent.toMessageChain(
                                            group
                                    )
                            )
                            count++
                            delay(1_500)
                        }
                    } catch (e: RuntimeException) {
                        daemonLogger.warning("[推送] 将动态推送至 $gid 时发生意外", e)
                        return@target
                    }
                }

                pdh.isPushed = true
            }
        }

        return count
    }

    private data class PushDynamicHistory(
        val uid: Long,
        var pushContent: MessageWrapper,
        val target: MutableSet<Long> = mutableSetOf(),
        var isPushed: Boolean = false
    )
}
