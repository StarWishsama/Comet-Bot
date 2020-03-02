package io.github.starwishsama.nbot.objects

import io.github.starwishsama.nbot.enums.UserLevel
import java.util.*

class BotUser() {
    var userId = 0L;
    lateinit var userUUID: UUID
    var lastCheckInTime = Calendar.getInstance()
    var checkInPoint = 0.0
    var checkInTime = 0
    var bindUserName: String? = null
    var msgVL = 0
    var r6sAccount: String? = null
    var level = UserLevel.USER
    var commandTime = 20

    constructor(userId: Long) : this() {
        this.userId = userId
    }

    fun refreshTime(){
        commandTime = 20
    }

    fun addCommandTime(){
        commandTime++
    }
}