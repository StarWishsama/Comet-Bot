package io.github.starwishsama.comet.utils.json

import com.fasterxml.jackson.databind.JsonNode

fun JsonNode.isUsable(): Boolean {
    return isNull
}