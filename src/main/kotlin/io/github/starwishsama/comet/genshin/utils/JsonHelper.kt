package io.github.starwishsama.comet.genshin.utils

import kotlinx.serialization.json.Json

object JsonHelper {
    val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }
}