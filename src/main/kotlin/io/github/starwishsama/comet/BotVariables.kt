package io.github.starwishsama.comet

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.github.starwishsama.comet.objects.BotLocalization
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.objects.RssItem
import io.github.starwishsama.comet.objects.config.CometConfig
import io.github.starwishsama.comet.objects.gacha.items.ArkNightOperator
import io.github.starwishsama.comet.objects.gacha.items.PCRCharacter
import io.github.starwishsama.comet.objects.pojo.Hitokoto
import io.github.starwishsama.comet.objects.shop.Shop
import io.github.starwishsama.comet.utils.LoggerAppender
import net.kronos.rkon.core.Rcon
import net.mamoe.mirai.Bot
import net.mamoe.mirai.utils.MiraiInternalApi
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.PlatformLogger
import org.apache.commons.lang3.concurrent.BasicThreadFactory
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

/**
 * 机器人(几乎)所有数据的存放类
 * 可以直接访问数据
 *
 * FIXME: 不应该在初始化时 init 这么多变量, 应当分担到各自所需类中 (即懒处理它们)
 *
 * @author Nameless
 */

@OptIn(MiraiInternalApi::class)
object BotVariables {
    lateinit var filePath: File

    /** 作为独立运行时使用的变量, 除 [Comet] 外禁止调用 */
    lateinit var bot: Bot

    lateinit var loggerAppender: LoggerAppender

    fun isBotInitialized(): Boolean = ::bot.isInitialized

    lateinit var startTime: LocalDateTime

    val service: ScheduledExecutorService = Executors.newScheduledThreadPool(
            8,
            BasicThreadFactory.Builder()
                    .namingPattern("comet-service-%d")
                    .daemon(true)
                    .uncaughtExceptionHandler { thread, t ->
                        daemonLogger.warning("线程 ${thread.name} 在执行任务时发生了错误", t)
                    }.build()
    )

    val logger: MiraiLogger = PlatformLogger("CometBot") {
        Comet.console.printAbove(it)
        if (::loggerAppender.isInitialized) {
            loggerAppender.appendLog(it)
        }
    }

    val daemonLogger: MiraiLogger = PlatformLogger("CometService") {
        Comet.console.printAbove(it)
        if (::loggerAppender.isInitialized) {
            loggerAppender.appendLog(it)
        }
    }

    val consoleCommandLogger: MiraiLogger = PlatformLogger("CometConsole") {
        Comet.console.printAbove(it)
        if (::loggerAppender.isInitialized) {
            loggerAppender.appendLog(it)
        }
    }

    val nullableGson: Gson = GsonBuilder().serializeNulls().setPrettyPrinting().setExclusionStrategies(
        object : ExclusionStrategy {
            override fun shouldSkipField(f: FieldAttributes?): Boolean {
                return f?.name?.startsWith("_") == true
            }

            override fun shouldSkipClass(clazz: Class<*>?): Boolean {
                return false
            }
        }
    ).create()
    val gson: Gson = GsonBuilder().setPrettyPrinting().setLenient().setExclusionStrategies(
        object : ExclusionStrategy {
            override fun shouldSkipField(f: FieldAttributes?): Boolean {
                return f?.name?.startsWith("U") == true
            }

            override fun shouldSkipClass(clazz: Class<*>?): Boolean {
                return false
            }
        }
    ).create()
    var rCon: Rcon? = null
    lateinit var log: File

    var shop: MutableList<Shop> = LinkedList()
    val users: MutableList<BotUser> = LinkedList()
    var localMessage: MutableList<BotLocalization> = ArrayList()
    var cfg = CometConfig()
    var hitokoto: Hitokoto? = null
    lateinit var rssItems: MutableList<RssItem>

    /** 明日方舟卡池数据 */
    var arkNight: MutableList<ArkNightOperator> = mutableListOf()

    /** 公主链接卡池数据 */
    var pcr: MutableList<PCRCharacter> = mutableListOf()

    var switch: Boolean = true

    val coolDown: MutableMap<Long, Long> = HashMap()

    val hmsPattern: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    val yyMMddPattern: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")

    var hiddenOperators = mutableListOf<String>()
}