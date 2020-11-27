package io.github.starwishsama.comet

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import io.github.starwishsama.comet.objects.*
import io.github.starwishsama.comet.objects.draw.items.ArkNightOperator
import io.github.starwishsama.comet.objects.draw.items.PCRCharacter
import io.github.starwishsama.comet.objects.group.PerGroupConfig
import io.github.starwishsama.comet.objects.group.Shop
import io.github.starwishsama.comet.objects.pojo.Hitokoto
import io.github.starwishsama.comet.utils.getContext
import io.github.starwishsama.comet.utils.writeString
import net.kronos.rkon.core.Rcon
import net.mamoe.mirai.Bot
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
 * @author Nameless
 */

object BotVariables {
    lateinit var filePath: File
    const val version = "0.6-M2-d8b6e73"
    const val buildTime = "2020/11/27 12:59:40"

    /** 作为独立运行时使用的变量, 除 [Comet] 外禁止调用 */
    lateinit var bot: Bot

    fun isBotInitialized(): Boolean = ::bot.isInitialized

    lateinit var startTime: LocalDateTime
    var service: ScheduledExecutorService = Executors.newScheduledThreadPool(
            8,
            BasicThreadFactory.Builder()
                    .namingPattern("comet-service-%d")
                    .daemon(true)
                    .uncaughtExceptionHandler { thread, t ->
                        daemonLogger.warning("线程 ${thread.name} 在执行任务时发生了错误", t)
                    }.build()
    )
    val logger: PlatformLogger = PlatformLogger("CometBot", {
        log.writeString(log.getContext() + "$it\n")
        println(it)
    })
    val daemonLogger: PlatformLogger = PlatformLogger("CometService", {
        log.writeString(log.getContext() + "$it\n")
        println(it)
    })
    val consoleCommandLogger: PlatformLogger = PlatformLogger("CometConsole", {
        log.writeString(log.getContext() + "$it\n")
        println(it)
    })
    val gson: Gson = GsonBuilder().serializeNulls().setPrettyPrinting().create()
    var rCon: Rcon? = null
    lateinit var log: File

    var shop: MutableList<Shop> = LinkedList()
    val users: MutableList<BotUser> = LinkedList()
    var localMessage: MutableList<BotLocalization> = ArrayList()
    var cfg = CometConfig()
    var underCovers: MutableList<RandomResult> = LinkedList()
    var cache: JsonObject = JsonObject()
    val perGroup: MutableSet<PerGroupConfig> = HashSet()
    var hitokoto: Hitokoto? = null
    val rssItems: MutableList<RssItem> = mutableListOf()

    /** 明日方舟卡池数据 */
    var arkNight: MutableList<ArkNightOperator> = LinkedList()

    /** 公主链接卡池数据 */
    var pcr: MutableList<PCRCharacter> = LinkedList()

    var switch: Boolean = true

    val coolDown: MutableMap<Long, Long> = HashMap()

    val hmsPattern: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    val yyMMddPattern: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
}