/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

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
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.objects.config.CometConfig
import io.github.starwishsama.comet.objects.gacha.items.ArkNightOperator
import io.github.starwishsama.comet.objects.wrapper.WrapperElement
import io.github.starwishsama.comet.service.server.WebHookServer
import io.github.starwishsama.comet.utils.FileUtil
import io.github.starwishsama.comet.utils.LoggerAppender
import io.github.starwishsama.comet.utils.json.LocalDateTimeConverter
import io.github.starwishsama.comet.utils.json.WrapperConverter
import net.kronos.rkon.core.Rcon
import net.mamoe.mirai.utils.MiraiInternalApi
import okhttp3.OkHttpClient
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap

/**
 * Comet (几乎) 所有数据的存放类
 * 可以直接访问数据
 *
 * @author StarWishsama
 */

@OptIn(MiraiInternalApi::class)
object CometVariables {
    internal val filePath: File by lazy {
        FileUtil.getJarLocation()
    }

    val comet: Comet = Comet()

    internal lateinit var loggerAppender: LoggerAppender

    internal lateinit var miraiLoggerAppender: LoggerAppender

    internal lateinit var miraiNetLoggerAppender: LoggerAppender

    internal lateinit var startTime: LocalDateTime

    internal var cfg = CometConfig()

    internal var cometServer: WebHookServer? = null

    internal val logAction: (String) -> Unit = {
        CometApplication.console.printAbove(it)
        if (::loggerAppender.isInitialized) {
            loggerAppender.appendLog(it)
        }
    }

    val logger: HinaLogger = HinaLogger("Comet", logAction = { logAction(it) }, debugMode = cfg.debugMode)

    internal val netLogger: HinaLogger by lazy {
        HinaLogger("CometNet", logAction = { logAction(it) }, debugMode = cfg.debugMode)
    }

    internal val daemonLogger: HinaLogger by lazy {
        HinaLogger("CometService", logAction = { logAction(it) }, debugMode = cfg.debugMode)
    }

    internal val consoleCommandLogger: HinaLogger by lazy {
        HinaLogger("CometConsole", logAction = { logAction(it) }, debugMode = cfg.debugMode)
    }

    internal val miraiLogger: HinaLogger by lazy {
        HinaLogger("mirai", logAction = {
            CometApplication.console.printAbove(it)
            if (::miraiLoggerAppender.isInitialized) {
                miraiLoggerAppender.appendLog(it)
            }
        }, debugMode = cfg.debugMode)
    }

    internal val miraiNetLogger: HinaLogger by lazy {
        HinaLogger("miraiNet", logAction = {
            CometApplication.console.printAbove(it)
            if (::miraiNetLoggerAppender.isInitialized) {
                miraiNetLoggerAppender.appendLog(it)
            }
        }, debugMode = cfg.debugMode)
    }

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
                it.addSerializer(
                    LocalDateTime::class.java,
                    LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"))
                )
                it.addSerializer(LocalDate::class.java, LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy/MM/dd")))
                it.addSerializer(LocalTime::class.java, LocalTimeSerializer(DateTimeFormatter.ofPattern("HH:mm:ss")))
                it.addDeserializer(
                    LocalDate::class.java,
                    LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
                )
                it.addDeserializer(
                    LocalTime::class.java,
                    LocalTimeDeserializer(DateTimeFormatter.ofPattern("HH:mm:ss"))
                )
            },
            KotlinModule(nullIsSameAsDefault = true, nullToEmptyCollection = true, nullToEmptyMap = true),
            SimpleModule().also {
                it.addDeserializer(LocalDateTime::class.java, LocalDateTimeConverter)
                it.addDeserializer(WrapperElement::class.java, WrapperConverter)
            }
        )
        .setDateFormat(SimpleDateFormat("yyyy/MM/dd HH:mm:ss"))

    internal var rCon: Rcon? = null

    internal val cometUsers: MutableMap<Long, CometUser> = ConcurrentHashMap()
    internal lateinit var localizationManager: LocalizationManager

    /** 明日方舟卡池数据 */
    internal val arkNight: MutableList<ArkNightOperator> = mutableListOf()

    @Volatile
    internal var switch: Boolean = true

    internal val hmsPattern: DateTimeFormatter by lazy {
        DateTimeFormatter.ofPattern("HH:mm:ss")
    }

    internal val yyMMddPattern: DateTimeFormatter by lazy {
        DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
    }

    internal lateinit var client: OkHttpClient
}