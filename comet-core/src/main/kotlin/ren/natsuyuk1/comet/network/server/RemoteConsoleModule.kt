package ren.natsuyuk1.comet.network.server

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.SerializationException
import ren.natsuyuk1.comet.api.config.CometGlobalConfig
import ren.natsuyuk1.comet.api.database.AccountData
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.api.user.UserLevel
import ren.natsuyuk1.comet.consts.json
import ren.natsuyuk1.comet.network.server.request.CometLoginReq
import ren.natsuyuk1.comet.network.server.response.CometResponse
import ren.natsuyuk1.comet.network.server.response.respond
import ren.natsuyuk1.comet.utils.jvm.asEnumOrNull
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
        get("/promote/{id}/{platform}/{level?}") {
            RemoteConsoleHandler.promote(call)
        }

        post("/login") {
            RemoteConsoleHandler.login(call)
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
    suspend fun login(call: ApplicationCall) {
        if (!call.request.headers.verifyHeader()) {
            CometResponse(HttpStatusCode.Unauthorized, "No Permission").respond(call)
            return
        }

        logger.debug { "远端尝试登录账号" }

        if (call.request.contentType() != ContentType.Application.Json) {
            CometResponse(HttpStatusCode.BadRequest, "Unvalid request body").respond(call)
            return
        }

        val req = call.receiveText()

        try {
            val loginReq = json.decodeFromString(CometLoginReq.serializer(), req)

            logger.debug { "远端尝试登录账号信息: $loginReq" }

            val result =
                AccountData.login(loginReq.id, loginReq.password, loginReq.platform, loginReq.miraiLoginProtocol)

            CometResponse(result.status.statusCode, result.message).respond(call)
        } catch (e: SerializationException) {
            CometResponse(HttpStatusCode.BadRequest, "Unvalid request body").respond(call)
        }
    }

    suspend fun logout(call: ApplicationCall) {
        if (!call.request.headers.verifyHeader()) {
            CometResponse(HttpStatusCode.Unauthorized, "No Permission").respond(call)
            return
        }

        logger.debug { "远端尝试登出账号" }

        val id = call.parameters["id"]?.toLongOrNull()
        val platform = call.parameters["platform"]?.let { param -> param.asEnumOrNull<LoginPlatform>() }

        if (id == null || platform == null) {
            CometResponse(HttpStatusCode.BadRequest, "Missing required argument(s)").respond(call)
            return
        }

        logger.debug { "远端尝试登录账号信息: $id[$platform]" }

        val result = AccountData.logout(id, platform)

        CometResponse(result.status.statusCode, result.message).respond(call)
    }

    suspend fun promote(call: ApplicationCall) {
        if (!call.request.headers.verifyHeader()) {
            CometResponse(HttpStatusCode.Unauthorized, "No Permission").respond(call)
            return
        }

        logger.debug { "远端尝试提升用户账号权限" }

        val id = call.parameters["id"]?.toLongOrNull()
        val platform = call.parameters["platform"]?.asEnumOrNull<LoginPlatform>()

        if (id == null || platform == null) {
            CometResponse(HttpStatusCode.BadRequest, "Missing required argument(s)").respond(call)
            return
        }

        val user = CometUser.getUser(id, platform)

        if (user == null) {
            CometResponse(HttpStatusCode.NotFound, "找不到指定的用户").respond(call)
            return
        }

        val level = call.parameters["level"]?.asEnumOrNull<UserLevel>()

        if (level != null) {
            user.userLevel = level
        } else {
            val targetLevel = user.userLevel.ordinal + 1

            if (targetLevel >= UserLevel.values().size) {
                user.userLevel = UserLevel.USER
            } else {
                user.userLevel = UserLevel.values()[targetLevel]
            }
        }

        CometResponse(HttpStatusCode.OK, "成功将 $id[$platform] 的等级设为 ${user.userLevel}").respond(call)
    }

    suspend fun stop(call: ApplicationCall) {
        if (!call.request.headers.verifyHeader()) {
            CometResponse(HttpStatusCode.Unauthorized, "No Permission").respond(call)
            return
        }

        logger.debug { "远端尝试关闭 Comet" }

        call.respond(HttpStatusCode.OK)

        exitProcess(0)
    }
}
