package io.github.starwishsama.comet.listeners

import net.mamoe.mirai.event.Event
import kotlin.reflect.KClass

interface NListener {
    val eventToListen: List<KClass<out Event>>

    fun listen(event: Event)

    fun getName(): String
}