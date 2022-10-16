package ren.natsuyuk1.comet.network.server

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ren.natsuyuk1.comet.objects.config.PushTemplate
import ren.natsuyuk1.comet.objects.config.PushTemplateConfig
import ren.natsuyuk1.comet.utils.ktor.asReadable

private val logger = mu.KotlinLogging.logger {}

fun Application.pushModule() {
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

            // TODO PushTemplateEvent
        } catch (e: Exception) {
            logger.warn(e) { "处理 WebHook 消息时发生错误" }
            call.respondText("Comet 发生内部错误", status = HttpStatusCode.InternalServerError)
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
            respond(HttpStatusCode.NotAcceptable)
            return null
        }

        if (token == null) {
            respond(HttpStatusCode.Unauthorized)
            return null
        }

        val ptc = PushTemplateConfig.data.find { it.token.toString() == token.replace("Bearer ", "") }
        return if (ptc == null) {
            respond(HttpStatusCode.NotFound)
            null
        } else {
            ptc
        }
    }
}
