package ren.natsuyuk1.comet.network.server

import io.ktor.server.application.*
import io.ktor.server.routing.*

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
    fun handle(call: ApplicationCall) {
    }
}
