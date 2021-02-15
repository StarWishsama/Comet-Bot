package io.github.starwishsama.comet.service.pusher

import io.github.starwishsama.comet.service.pusher.config.EmptyPusherConfig
import io.github.starwishsama.comet.service.pusher.config.PusherConfig
import net.mamoe.mirai.Bot
import java.time.LocalDateTime

/**
 * [CometPusher]
 */
abstract class CometPusher(val bot: Bot, val name: String) {
    open var config: PusherConfig = EmptyPusherConfig()

    var retrieveTime: Int = 0

    var pushTime: Int = 0

    var latestPushTime: LocalDateTime = LocalDateTime.now()

    abstract fun retrieve()

    abstract fun push()

    abstract fun save()

    fun execute() {
        retrieve()
        push()
    }

    abstract fun start()

    fun addPushTime(){
        pushTime += 1
    }

    fun resetPushTime() {
        pushTime = 0
    }

    fun addRetrieveTime() {
        retrieveTime += 1
    }

    fun resetRetrieveTime() {
        retrieveTime = 0
    }
}