package ren.natsuyuk1.comet.network.server.response

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import ren.natsuyuk1.comet.consts.json
import ren.natsuyuk1.comet.utils.json.serializers.HttpStatusCodeSerializer

@Serializable
data class CometResponse(
    @Serializable(with = HttpStatusCodeSerializer::class)
    val code: HttpStatusCode,
    val message: String,
)

fun CometResponse.toJson(): String = json.encodeToString(this)

suspend fun CometResponse.respond(call: ApplicationCall) {
    call.respondText(toJson(), status = code)
}
