package ren.natsuyuk1.comet.utils.json

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

inline fun <reified T> String.serializeTo(json: Json): T = json.decodeFromString(this)
