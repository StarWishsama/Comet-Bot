package io.github.starwishsama.comet

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.starwishsama.comet.i18n.LocalizationManager
import io.github.starwishsama.comet.logger.HinaLogger
import io.github.starwishsama.comet.logger.RetrofitLogger
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.objects.config.CometConfig
import io.github.starwishsama.comet.objects.gacha.items.ArkNightOperator
import io.github.starwishsama.comet.objects.gacha.items.PCRCharacter
import io.github.starwishsama.comet.objects.pojo.Hitokoto
import io.github.starwishsama.comet.objects.shop.Shop
import io.github.starwishsama.comet.service.webhook.WebHookServer
import io.github.starwishsama.comet.utils.LoggerAppender
import io.github.starwishsama.comet.utils.network.NetUtil
import net.kronos.rkon.core.Rcon
import net.mamoe.mirai.utils.MiraiInternalApi
import okhttp3.OkHttpClient
import org.apache.commons.lang3.concurrent.BasicThreadFactory
import java.io.File
import java.net.InetSocketAddress
import java.net.Proxy
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

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

    val comet: Comet = Comet()

    lateinit var loggerAppender: LoggerAppender

    lateinit var startTime: LocalDateTime

    var cfg = CometConfig()

    var webhookServer: WebHookServer? = null

    val service: ScheduledExecutorService = Executors.newScheduledThreadPool(
        Runtime.getRuntime().availableProcessors(),
            BasicThreadFactory.Builder()
                    .namingPattern("comet-service-%d")
                    .daemon(true)
                    .uncaughtExceptionHandler { thread, t ->
                        daemonLogger.warning("线程 ${thread.name} 在执行任务时发生了错误", t)
                    }.build()
    )

    internal val logAction: (String) -> Unit = {
        CometApplication.console.printAbove(it)
        if (::loggerAppender.isInitialized) {
            loggerAppender.appendLog(it)
        }
    }

    val logger: HinaLogger = HinaLogger("Comet", logAction = { logAction(it) }, debugMode = cfg.debugMode)

    val netLogger: HinaLogger = HinaLogger("CometNet", logAction = { logAction(it) }, debugMode = cfg.debugMode)

    val daemonLogger: HinaLogger = HinaLogger("CometService", logAction = { logAction(it) }, debugMode = cfg.debugMode)

    val consoleCommandLogger: HinaLogger = HinaLogger("CometConsole", logAction = { logAction(it) }, debugMode = cfg.debugMode)

    val mapper: ObjectMapper = ObjectMapper()
        .findAndRegisterModules()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .registerKotlinModule()

    var rCon: Rcon? = null
    lateinit var log: File

    val shop: MutableList<Shop> = LinkedList()
    val users: MutableList<BotUser> = LinkedList()
    lateinit var localizationManager: LocalizationManager
    var hitokoto: Hitokoto? = null

    /** 明日方舟卡池数据 */
    val arkNight: MutableList<ArkNightOperator> = mutableListOf()

    /** 公主链接卡池数据 */
    val pcr: MutableList<PCRCharacter> = mutableListOf()

    @Volatile
    var switch: Boolean = true

    val hmsPattern: DateTimeFormatter by lazy {
        DateTimeFormatter.ofPattern("HH:mm:ss")
    }

    val yyMMddPattern: DateTimeFormatter by lazy {
        DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
    }

    val client = OkHttpClient().newBuilder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .followRedirects(true)
        .readTimeout(5, TimeUnit.SECONDS)
        .hostnameVerifier { _, _ -> true }
        .also {
            if (cfg.proxySwitch) {
                if (NetUtil.checkProxyUsable()) {
                    it.proxy(Proxy(cfg.proxyType, InetSocketAddress(cfg.proxyUrl, cfg.proxyPort)))
                }
            }
        }
        .addInterceptor(RetrofitLogger())
        .build()
}