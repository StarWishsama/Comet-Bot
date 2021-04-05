package io.github.starwishsama.comet.service.server

import cn.hutool.core.net.URLDecoder
import cn.hutool.http.HttpStatus
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.api.thirdparty.github.data.events.PushEvent
import io.github.starwishsama.comet.logger.HinaLogLevel
import io.github.starwishsama.comet.logger.HinaLogger
import io.github.starwishsama.comet.objects.wrapper.WrapperElement
import io.github.starwishsama.comet.service.pusher.instances.GithubPusher
import io.github.starwishsama.comet.utils.FileUtil
import io.github.starwishsama.comet.utils.json.LocalDateTimeConverter
import io.github.starwishsama.comet.utils.json.WrapperConverter
import io.github.starwishsama.comet.utils.json.isUsable
import io.github.starwishsama.comet.utils.writeString
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

val logger: HinaLogger =
    HinaLogger("CometServer", logAction = { BotVariables.logAction(it) }, debugMode = BotVariables.cfg.debugMode)

fun Application.handleGithub(prefix: String) {
    install(ContentNegotiation) {
        jackson {
            // 美化输出
            enable(SerializationFeature.INDENT_OUTPUT)
            // 将单一值序列化成表
            enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            // 禁止将日期类序列化为时间戳
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            // 反序列化时忽略未知参数
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

            registerModules(
                JavaTimeModule().also {
                    it.addSerializer(
                        LocalDateTime::class.java,
                        LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"))
                    )
                    it.addSerializer(
                        LocalDate::class.java,
                        LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
                    )
                    it.addSerializer(
                        LocalTime::class.java,
                        LocalTimeSerializer(DateTimeFormatter.ofPattern("HH:mm:ss"))
                    )
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
            )
            dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        }
    }

    routing {
        post("/${prefix}") {
            logger.log(HinaLogLevel.Debug, "收到新事件", prefix = "WebHook")

            val request = call.receiveText()

            if (!request.startsWith("payload")) {
                BotVariables.netLogger.log(HinaLogLevel.Debug, "无效请求, 传入内容已保存", prefix = "WebHook")
                File(FileUtil.getErrorReportFolder(), "unknown-json.txt").also { it.createNewFile() }
                    .writeString(request)
                call.response.status(HttpStatusCode.NotFound)
                return@post
            }

            val payload = URLDecoder.decode(request.replace("payload=", ""), Charsets.UTF_8)

            val validate = call.receive<JsonNode>().isUsable()

            if (validate) {
                logger.log(HinaLogLevel.Warn, "解析请求失败, 回调的 JSON 不合法.\n${payload}", prefix = "WebHook")
                call.response.status(HttpStatusCode.NotFound)
                return@post
            }

            try {
                val info = call.receive<PushEvent>()
                GithubPusher.push(info)
                logger.log(HinaLogLevel.Debug, "推送 WebHook 消息成功", prefix = "WebHook")
            } catch (e: JsonParseException) {
                logger.log(HinaLogLevel.Debug, "推送 WebHook 消息失败, 不支持的事件类型", prefix = "WebHook")
            } catch (e: Exception) {
                logger.log(HinaLogLevel.Warn, "推送 WebHook 消息失败", e, prefix = "WebHook")
            }

            call.response.apply {
                status(HttpStatusCode.OK)
                header("content-type", "text/plain; charset=UTF-8")
            }

            call.respondText("Success")
        }
    }
}