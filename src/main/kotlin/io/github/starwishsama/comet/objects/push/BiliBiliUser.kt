package io.github.starwishsama.comet.objects.push

data class BiliBiliUser(
    override val id: String,
    val roomID: Long,
    override val userName: String
): PushUser(id, userName)