package io.github.starwishsama.comet.pushers

import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.BotVariables.perGroup
import io.github.starwishsama.comet.api.thirdparty.bilibili.BiliBiliMainApi
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.Dynamic
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.convertDynamic
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.convertToDynamicData
import io.github.starwishsama.comet.exceptions.ApiException
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import io.github.starwishsama.comet.utils.StringUtil.getLastingTime
import io.github.starwishsama.comet.utils.verboseS
import kotlinx.coroutines.*
import net.mamoe.mirai.Bot
import java.time.LocalDateTime
import java.util.concurrent.ScheduledFuture
import kotlin.time.ExperimentalTime

object BiliDynamicChecker : CometPusher {
    private val pushPool = mutableSetOf<PushDynamicHistory>()
    override val delayTime: Long = 3
    override val internal: Long = 3
    override var future: ScheduledFuture<*>? = null
    override var bot: Bot? = null
    override var pushCount: Int = 0
    override var lastPushTime: LocalDateTime = LocalDateTime.now()

    override fun retrieve() {
        pushCount = 0

        pushPool.forEach { it.target.clear() }

        val collectedUID = mutableSetOf<Long>().apply {
            perGroup.forEach {
                if (it.biliPushEnabled) {
                    addAll(it.biliSubscribers)
                }
            }
        }

        collectedUID.forEach { uid ->
            val dynamic: Dynamic = try {
                BiliBiliMainApi.getUserDynamicTimeline(uid)
            } catch (e: RuntimeException) {
                if (e !is ApiException) {
                    daemonLogger.warning("在获取动态时出现了异常", e)
                }
                null
            } ?: return@forEach

            val data = dynamic.convertDynamic()

            if (data.success) {
                val sentTime = dynamic.convertToDynamicData()?.getSentTime() ?: return@forEach

                // 检查是否火星了
                if (isOutdated(sentTime)) {
                    return@forEach
                }

                if (pushPool.isEmpty()) {
                    pushPool.plusAssign(
                        PushDynamicHistory(
                            uid = uid,
                            pushContent = data,
                            sentTime = sentTime
                        )
                    )
                    pushCount++
                } else {
                    val target = pushPool.parallelStream().filter { it.uid == uid }.findFirst()

                    if (target.isPresent) {
                        target.get().apply {
                            if (data.uniqueId != pushContent.uniqueId && data.text != pushContent.text) {
                                pushContent = data
                                isPushed = false
                                this.sentTime = sentTime
                            }
                        }
                    } else {
                        pushPool.plusAssign(
                            PushDynamicHistory(
                                uid = uid,
                                pushContent = data,
                                sentTime = sentTime
                            )
                        )
                    }
                }
            }
        }

        pushCount.apply {
            if (this > 0) daemonLogger.verboseS("Collected bili dynamic success, have retrieved $pushCount dynamic(s)!")
        }

        push()
    }

    override fun push() {
        perGroup.forEach { cfg ->
            cfg.biliSubscribers.forEach { uid ->
                getHistoryByUID(uid).forEach bili@ { pdh ->
                    if (pdh.target.contains(cfg.id) && !cfg.biliPushEnabled) {
                        pdh.target.remove(cfg.id)
                        return@bili
                    }

                    if (!pdh.target.contains(cfg.id) && pdh.uid == uid) {
                        pdh.target.add(cfg.id)
                    }
                }
            }
        }

        pushToGroups().apply {
            if (this > 0) daemonLogger.verboseS("Push bili dynamic success, have pushed $this group(s)!")
        }

        lastPushTime = LocalDateTime.now()
    }

    private fun pushToGroups(): Int {
        var count = 0

        /** 遍历推送列表推送开播消息 */
        pushPool.forEach { pdh ->
            if (!pdh.isPushed) {
                pdh.target.forEach push@{ gid ->
                    try {
                        runBlocking {
                            val group = bot?.getGroup(gid)
                            group?.sendMessage(
                                "${BiliBiliMainApi.getUserNameByMid(pdh.uid)} ".convertToChain() + pdh.pushContent.toMessageChain(
                                    group
                                )
                            )
                            count++
                            delay(1_500)
                        }
                    } catch (e: RuntimeException) {
                        daemonLogger.warning("[推送] 将动态推送至 $gid 时发生意外", e)
                        return@push
                    }
                }

                pdh.isPushed = true
            }
        }

        return count
    }

    data class PushDynamicHistory(
        val uid: Long,
        var pushContent: MessageWrapper,
        val target: MutableSet<Long> = mutableSetOf(),
        var sentTime: LocalDateTime,
        var isPushed: Boolean = false
    )

    @OptIn(ExperimentalTime::class)
    private fun isOutdated(sentTime: LocalDateTime?): Boolean {
        if (sentTime == null) return false
        if (sentTime == LocalDateTime.MIN) return false

        return sentTime.getLastingTime().inMinutes >= 30.0
    }

    fun getPool(): MutableSet<PushDynamicHistory> {
        return pushPool
    }

    private fun getHistoryByUID(uid: Long): List<PushDynamicHistory> = pushPool.filter { it.uid == uid }
}