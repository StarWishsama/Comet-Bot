package io.github.starwishsama.comet.objects.push

data class YoutubeUser(
    override val id: String,
    override val userName: String
): PushUser(id, userName)