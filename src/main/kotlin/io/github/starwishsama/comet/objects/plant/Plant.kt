package io.github.starwishsama.comet.objects.plant

import io.github.starwishsama.comet.objects.CometUser
import java.util.*

abstract class Plant(
    var name: String,
    var exp: Int
) {
    lateinit var owner: UUID

    abstract fun skill()

    abstract fun upgrade()

    fun setOwner(user: CometUser) {
        owner = user.uuid
    }

    fun getOwner(): CometUser? {
        return CometUser.getUser(owner)
    }
}