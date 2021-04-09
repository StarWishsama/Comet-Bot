package io.github.starwishsama.comet.api.thirdparty.github.data.events

import io.github.starwishsama.comet.objects.wrapper.MessageWrapper

interface GithubEvent {
    fun toMessageWrapper(): MessageWrapper

    fun repoName(): String

    fun sendable(): Boolean
}