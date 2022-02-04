package io.github.starwishsama.comet.objects.plant

import io.github.starwishsama.comet.objects.CometUser
import java.util.*

internal const val BASE_EXP = 30
internal const val EXP_SCALE = 1.5

abstract class Plant(
    var name: String,
    var exp: Int
) {
    lateinit var owner: UUID

    abstract fun skill()

    abstract fun upgrade()

    fun getOwner(): CometUser? {
        return CometUser.getUser(owner)
    }

    fun getLevel(): Int {
        return 1
    }
}