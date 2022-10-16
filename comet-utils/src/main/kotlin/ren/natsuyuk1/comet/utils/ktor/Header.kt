package ren.natsuyuk1.comet.utils.ktor

import io.ktor.http.*

fun Headers.asReadable(): String = entries()
    .joinToString(";") { (k, v) ->
        "$k=$v"
    }
