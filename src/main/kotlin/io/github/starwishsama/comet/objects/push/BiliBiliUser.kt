package io.github.starwishsama.comet.objects.push

class BiliBiliUser(
    id: String,
    userName: String,
    var roomID: Long,
): PushUser(id, userName)