package ren.natsuyuk1.comet.network.server

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import ren.natsuyuk1.comet.api.config.CometGlobalConfig
import ren.natsuyuk1.comet.api.database.AccountData
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.network.server.response.CometResponse
import ren.natsuyuk1.comet.network.server.response.respond
import java.util.*
import kotlin.system.exitProcess

private val logger = mu.KotlinLogging.logger {}

fun Application.remoteConsoleModule() {
    routing {
        handleRemoteControl()
    }
}

internal fun Routing.handleRemoteControl() {
    route("/console") {
        get("/promote/{id}/{platform}") {
            RemoteConsoleHandler.promote(call)
        }

        post("/login") {
        }

        get("/logout/{id}/{platform}") {
            RemoteConsoleHandler.logout(call)
        }

        get("/stop") {
            RemoteConsoleHandler.stop(call)
        }
    }
}

private fun Headers.verifyHeader(): Boolean {
    val tokenHeader = this["Authorization"] ?: return false
    val token = UUID.fromString(tokenHeader.replace("Bearer ", ""))

    return CometGlobalConfig.data.accessToken == token
}

object RemoteConsoleHandler {
    suspend fun logout(call: ApplicationCall) {
        if (!call.request.headers.verifyHeader()) {
            CometResponse(HttpStatusCode.Unauthorized, "No Permission").respond(call)
            return
        }

        val id = call.parameters["id"]?.toLongOrNull()
        val platform = call.parameters["platform"]?.let { it1 -> LoginPlatform.valueOf(it1) }

        if (id == null || platform == null) {
            CometResponse(HttpStatusCode.BadRequest, "Missing required argument(s)").respond(call)
            return
        }

        CometResponse(HttpStatusCode.OK, AccountData.logout(id, platform))
    }

    suspend fun promote(call: ApplicationCall) {
        if (!call.request.headers.verifyHeader()) {
            CometResponse(HttpStatusCode.Unauthorized, "No Permission").respond(call)
            return
        }
    }

    suspend fun stop(call: ApplicationCall) {
        if (!call.request.headers.verifyHeader()) {
            CometResponse(HttpStatusCode.Unauthorized, "No Permission").respond(call)
            return
        }

        exitProcess(0)
    }
}
