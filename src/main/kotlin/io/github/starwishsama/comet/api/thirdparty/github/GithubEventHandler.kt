package io.github.starwishsama.comet.api.thirdparty.github

import com.fasterxml.jackson.module.kotlin.readValue
import io.github.starwishsama.comet.BotVariables.mapper
import io.github.starwishsama.comet.api.thirdparty.github.data.events.GithubEvent
import io.github.starwishsama.comet.api.thirdparty.github.data.events.IssueEvent
import io.github.starwishsama.comet.api.thirdparty.github.data.events.PingEvent
import io.github.starwishsama.comet.api.thirdparty.github.data.events.PushEvent

object GithubEventHandler {
    fun process(raw: String): GithubEvent {
        val node = mapper.readTree(raw)

        return when {
            node["zen"] != null -> {
                mapper.readValue<PingEvent>(raw)
            }
            node["issue"] != null -> {
                mapper.readValue<IssueEvent>(raw)
            }
            else -> {
                mapper.readValue<PushEvent>(raw)
            }
        }
    }
}