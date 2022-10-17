package ren.natsuyuk1.comet.network.server

import com.jayway.jsonpath.JsonPath
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ren.natsuyuk1.comet.api.event.broadcast
import ren.natsuyuk1.comet.event.pusher.pushtemplate.PushTemplateReceiveEvent
import ren.natsuyuk1.comet.network.server.response.CometResponse
import ren.natsuyuk1.comet.network.server.response.respond
import ren.natsuyuk1.comet.objects.config.PushTemplate
import ren.natsuyuk1.comet.objects.config.PushTemplateConfig
import ren.natsuyuk1.comet.utils.json.JsonPathMap
import ren.natsuyuk1.comet.utils.ktor.asReadable
import java.util.*

private val logger = mu.KotlinLogging.logger {}

fun Application.pushTemplateModule() {
    routing {
        handlePush()
    }
}

internal fun Routing.handlePush() {
    post(path = "/push") {
        PushHandler.handle(call)
    }
}

object PushHandler {
    suspend fun handle(call: ApplicationCall) {
        logger.debug { "有新连接 ${call.request.httpMethod} - ${call.request.uri}" }
        logger.debug {
            "Request Headers: ${call.request.headers.asReadable()}"
        }

        try {
            val ptc = call.validateRequest() ?: return

            val body = call.receiveText()

            try {
                val jp = JsonPath.parse(body)
                PushTemplateReceiveEvent(
                    JsonPathMap(jp),
                    ptc
                ).broadcast()

                CometResponse(HttpStatusCode.OK.value, "Comet 已收到推送内容并推送").respond(call)
            } catch (e: Exception) {
                logger.warn(e) { "无法解析远端传入的 json, 原内容 $body" }
            }
        } catch (e: Exception) {
            logger.warn(e) { "处理 WebHook 消息时发生错误" }
            CometResponse(HttpStatusCode.InternalServerError.value, "Comet 发生内部错误").respond(call)
        }
    }

    private suspend fun ApplicationCall.validateRequest(): PushTemplate? {
        if (request.httpMethod != HttpMethod.Post) {
            respond(HttpStatusCode.BadRequest)
            return null
        }

        val token = request.headers["Authorization"]
        val json = request.headers["Content-Type"] == ContentType.Application.Json.toString()

        if (!json) {
            CometResponse(HttpStatusCode.NotAcceptable.value, "不是合法的请求类型").respond(this)
            return null
        }

        if (token == null) {
            CometResponse(HttpStatusCode.Unauthorized.value, "无权限").respond(this)
            return null
        }

        val uuidToken = UUID.fromString(token.replace("Bearer ", ""))

        val ptc = PushTemplateConfig.data.templates.find { it.token == uuidToken }
        return if (ptc == null) {
            CometResponse(HttpStatusCode.NotFound.value, "不存在对应目标").respond(this)
            null
        } else {
            ptc
        }
    }
}
