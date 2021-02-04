package io.github.starwishsama.comet.objects.push

class BiliBiliUser(
    id: String,
    userName: String,
    val roomID: Long,
): PushUser(id, userName)