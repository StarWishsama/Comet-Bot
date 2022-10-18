package ren.natsuyuk1.comet.network.server.response

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import ren.natsuyuk1.comet.consts.json

@Serializable
data class CometResponse(
    val code: Int,
    val message: String
)

fun CometResponse.toJson(): String = json.encodeToString(this)

suspend fun CometResponse.respond(call: ApplicationCall) {
    call.respondText(toJson(), status = HttpStatusCode.fromValue(code))
}
