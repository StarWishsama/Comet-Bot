package io.github.starwishsama.comet.api.thirdparty.github

import io.github.starwishsama.comet.BotVariables.mapper

object GithubEventHandler {
    fun process(raw: String, handle: () -> Unit) {
        val node = mapper.readTree(raw)

        when {
            node["zen"] != null -> {
                handle()
            }
        }
    }
}