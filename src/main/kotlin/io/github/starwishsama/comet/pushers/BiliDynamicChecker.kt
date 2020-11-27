package io.github.starwishsama.comet.pushers

import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.BotVariables.perGroup
import io.github.starwishsama.comet.api.thirdparty.bilibili.MainApi
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.Dynamic
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.convertDynamic
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.convertToDynamicData
import io.github.starwishsama.comet.exceptions.ApiException
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import io.github.starwishsama.comet.utils.StringUtil.getLastingTime
import io.github.starwishsama.comet.utils.verboseS
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot
import java.time.LocalDateTime
import java.util.concurrent.ScheduledFuture
import kotlin.time.ExperimentalTime

object BiliDynamicChecker : CometPusher {
    private val pushedList = mutableSetOf<PushDynamicHistory>()
    override val delayTime: Long = 3
    override val internal: Long = 3
    override var future: ScheduledFuture<*>? = null
    override var bot: Bot? = null
    override var pushCount: Int = 0
    override var lastPushTime: LocalDateTime = LocalDateTime.now()

    override fun retrieve() {
        val collectedUsers = mutableSetOf<Long>()

        perGroup.parallelStream().forEach {
            if (it.biliPushEnabled) {
                collectedUsers.plusAssign(it.biliSubscribers)
            }
        }

        collectedUsers.parallelStream().forEach { uid ->
            val dynamic: Dynamic? = runBlocking {
                try {
                    MainApi.getUserDynamicTimeline(uid)
                } catch (e: RuntimeException) {
                    if (e !is ApiException) {
                        daemonLogger.warning("在获取动态时出现了异常", e)
                    }
                    return@runBlocking null
                }
            }

            val data = runBlocking { dynamic?.convertDynamic() }

            if (dynamic != null && data != null && data.success) {
                val sentTime = dynamic.convertToDynamicData()?.getSentTime() ?: return@forEach
                val pushDynamic = PushDynamicHistory(uid = uid, pushContent = data, sentTime = sentTime)

                // 检查是否火星了
                if (isOutdated(pushDynamic)) return@forEach

                if (pushedList.isEmpty()) {
                    pushedList.plusAssign(pushDynamic)
                    pushCount++
                } else {
                    val target = pushedList.parallelStream().filter { it.uid == uid }.findFirst()

                    if (!target.isPresent) {
                        pushedList.plusAssign(pushDynamic)
                    } else {
                        val oldData =
                            pushedList.parallelStream().filter { it.uid == uid && data.text == it.pushContent.text }
                                .findFirst()

                        if (!oldData.isPresent && target.isPresent) {
                            target.get().pushContent = data
                            target.get().isPushed = false
                            target.get().sentTime = sentTime
                        }
                    }
                }
            }
        }

        if (pushCount > 0) {
            daemonLogger.verboseS("Collected bili dynamic success, have collected $pushCount dynamic!")
            pushCount = 0
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
                        daemonLogger.warning("[推送] 将动态推送至 $gid 时发生意外 ${e.stackTraceToString()}")
                        return@target
                    }
                }

                pdh.isPushed = true
            }
        }

        lastPushTime = LocalDateTime.now()

        return count
    }

    private data class PushDynamicHistory(
        val uid: Long,
        var pushContent: MessageWrapper,
        val target: MutableSet<Long> = mutableSetOf(),
        var sentTime: LocalDateTime,
        var isPushed: Boolean = false
    )

    @OptIn(ExperimentalTime::class)
    private fun isOutdated(history: PushDynamicHistory?): Boolean {
        if (history == null) return true
        if (history.sentTime == LocalDateTime.MIN) return false

        return history.sentTime.getLastingTime().inMinutes >= 30
    }
}
