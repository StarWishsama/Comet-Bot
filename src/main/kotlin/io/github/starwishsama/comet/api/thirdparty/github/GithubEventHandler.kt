package io.github.starwishsama.comet.api.thirdparty.github

import com.fasterxml.jackson.module.kotlin.readValue
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BotVariables.mapper
import io.github.starwishsama.comet.api.thirdparty.github.data.events.GithubEvent
import io.github.starwishsama.comet.api.thirdparty.github.data.events.IssueEvent
import io.github.starwishsama.comet.api.thirdparty.github.data.events.PingEvent
import io.github.starwishsama.comet.api.thirdparty.github.data.events.PushEvent
import io.github.starwishsama.comet.exceptions.ApiException
import io.github.starwishsama.comet.logger.HinaLogLevel

object GithubEventHandler {
    fun process(raw: String, type: String): GithubEvent? {
        return when (type) {
            "ping" -> {
                mapper.readValue<PingEvent>(raw)
            }
            "issues" -> {
                mapper.readValue<IssueEvent>(raw)
            }
            "push" -> {
                mapper.readValue<PushEvent>(raw)
            }
            else -> {
                BotVariables.netLogger.log(HinaLogLevel.Debug, "推送 WebHook 消息失败, 不支持的事件类型", prefix = "WebHook")
                null
            }
        }
    }
}