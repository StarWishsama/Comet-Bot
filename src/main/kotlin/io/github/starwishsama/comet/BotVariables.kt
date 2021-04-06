package io.github.starwishsama.comet

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.github.starwishsama.comet.i18n.LocalizationManager
import io.github.starwishsama.comet.logger.HinaLogger
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.objects.config.CometConfig
import io.github.starwishsama.comet.objects.gacha.items.ArkNightOperator
import io.github.starwishsama.comet.objects.gacha.items.PCRCharacter
import io.github.starwishsama.comet.objects.pojo.Hitokoto
import io.github.starwishsama.comet.objects.shop.Shop
import io.github.starwishsama.comet.objects.wrapper.WrapperElement
import io.github.starwishsama.comet.service.server.WebHookServer
import io.github.starwishsama.comet.utils.LoggerAppender
import io.github.starwishsama.comet.utils.json.LocalDateTimeConverter
import io.github.starwishsama.comet.utils.json.WrapperConverter
import net.kronos.rkon.core.Rcon
import net.mamoe.mirai.utils.MiraiInternalApi
import okhttp3.OkHttpClient
import org.apache.commons.lang3.concurrent.BasicThreadFactory
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.*

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

    var cometServer: WebHookServer? = null

    val service = ScheduledThreadPoolExecutor(
        10,
        BasicThreadFactory.Builder()
            .namingPattern("comet-service-%d")
            .daemon(true)
            .uncaughtExceptionHandler { thread, t ->
                daemonLogger.warning("线程 ${thread.name} 在执行任务时发生了错误", t)
            }.build()
    ).also { it.maximumPoolSize = 30 }

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
        // 美化输出
        .enable(SerializationFeature.INDENT_OUTPUT)
        // 将单一值序列化成表
        .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        // 禁止将日期类序列化为时间戳
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        // 反序列化时忽略未知参数
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .registerModules(
            JavaTimeModule().also {
                it.addSerializer(LocalDateTime::class.java, LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")))
                it.addSerializer(LocalDate::class.java, LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy/MM/dd")))
                it.addSerializer(LocalTime::class.java, LocalTimeSerializer(DateTimeFormatter.ofPattern("HH:mm:ss")))
                it.addDeserializer(LocalDate::class.java, LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy/MM/dd")))
                it.addDeserializer(LocalTime::class.java, LocalTimeDeserializer(DateTimeFormatter.ofPattern("HH:mm:ss")))
            },
            KotlinModule(nullIsSameAsDefault = true, nullToEmptyCollection = true, nullToEmptyMap = true),
            SimpleModule().also {
                it.addDeserializer(LocalDateTime::class.java, LocalDateTimeConverter)
                it.addDeserializer(WrapperElement::class.java, WrapperConverter)
            }
        )
        .setDateFormat(SimpleDateFormat("yyyy/MM/dd HH:mm:ss"))

    var rCon: Rcon? = null

    val shop: MutableList<Shop> = LinkedList()
    val users: MutableMap<Long, BotUser> = hashMapOf()
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

    lateinit var client: OkHttpClient
}