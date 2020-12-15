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

        val collectedUID = mutableSetOf<Long>()

        collectedUID.apply {
            perGroup.forEach {
                if (it.biliPushEnabled) {
                    plusAssign(it.biliSubscribers)
                }
            }
        }

        collectedUID.forEach { uid ->
            runBlocking {

                val dynamic: Dynamic? =
                    withContext(Dispatchers.IO) {
                        try {
                            MainApi.getUserDynamicTimeline(uid)
                        } catch (e: RuntimeException) {
                            if (e !is ApiException) {
                                daemonLogger.warning("在获取动态时出现了异常", e)
                            }
                            null
                        }
                    }

                val data = dynamic?.convertDynamic()

                if (dynamic != null && data != null && data.success) {
                    val sentTime = dynamic.convertToDynamicData()?.getSentTime() ?: return@runBlocking

                    // 检查是否火星了
                    if (isOutdated(sentTime)) return@runBlocking

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

                        if (!target.isPresent) {
                            pushPool.plusAssign(
                                PushDynamicHistory(
                                    uid = uid,
                                    pushContent = data,
                                    sentTime = sentTime
                                )
                            )
                            return@runBlocking
                        }

                        target.ifPresent {
                            if (data.text != it.pushContent.text) {
                                it.pushContent = data
                                it.isPushed = false
                                it.sentTime = sentTime
                            }
                        }
                    }
                }
            }
        }

        collectedUID.clear()
    }

    override fun push() {
        pushPool.parallelStream().forEach { pdh ->
            perGroup.parallelStream().forEach { cfg ->
                if (cfg.biliPushEnabled) {
                    cfg.biliSubscribers.parallelStream().forEach { uid ->
                        if (pdh.uid == uid && !pdh.target.contains(cfg.id)) {
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
        pushPool.parallelStream().forEach { pdh ->
            if (!pdh.isPushed) {
                pdh.target.forEach target@{ gid ->
                    try {
                        runBlocking {
                            val group = bot?.getGroup(gid)
                            group?.sendMessage(
                                "${MainApi.getUserNameByMid(pdh.uid)} ".convertToChain() + pdh.pushContent.toMessageChain(
                                    group
                                )
                            )
                            count++
                            delay(2_000)
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

    data class PushDynamicHistory(
        val uid: Long,
        var pushContent: MessageWrapper,
        val target: MutableSet<Long> = mutableSetOf(),
        var sentTime: LocalDateTime,
        var isPushed: Boolean = false
    )

    @OptIn(ExperimentalTime::class)
    private fun isOutdated(sentTime: LocalDateTime?): Boolean {
        if (sentTime == null) return true
        if (sentTime == LocalDateTime.MIN) return false

        return sentTime.getLastingTime().inMinutes >= 30
    }

    fun getPool(): MutableSet<PushDynamicHistory> {
        return pushPool
    }
}