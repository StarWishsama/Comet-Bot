package io.github.starwishsama.nbot.objects

import io.github.starwishsama.nbot.BotConstants
import io.github.starwishsama.nbot.enums.UserLevel
import io.github.starwishsama.nbot.util.BotUtils.getLevel
import java.time.LocalDateTime
import java.util.*


data class BotUser(var userQQ: Long,
                   var lastCheckInTime : LocalDateTime = LocalDateTime.now(),
                   var checkInPoint : Double = 0.0,
                   var checkInTime : Int = 0,
                   var bindServerAccount: String? = null,
                   var msgVL : Int = 0,
                   var r6sAccount: String? = null,
                   var level: UserLevel = UserLevel.USER,
                   var randomTime : Int = 20,
                   var checkInGroup: Long = 0,
                   var biliSubs: List<Long> = ArrayList()) {
    fun decreaseTime() {
        if (level == UserLevel.USER) {
            randomTime--
        }
    }

    fun updateTime() {
        if (level == UserLevel.USER && randomTime < 20) {
            randomTime++
        }
    }

    fun addPoint(point: Double) {
        checkInPoint += point
    }

    fun addTime(time: Int) {
        if (level == UserLevel.USER && randomTime < 20) {
            randomTime += time
        }
    }

    fun cost(point: Double) {
        checkInPoint -= point
    }

    fun plusDay(){
        checkInTime++
    }

    fun resetDay(){
        checkInTime = 1
    }

    companion object {
        fun isUserExist(qq: Long): Boolean {
            return getUser(qq) != null
        }

        fun isBotAdmin(id: Long): Boolean {
            return getLevel(id).ordinal > 1
        }

        fun isBotOwner(id: Long): Boolean {
            return getLevel(id) == UserLevel.OWNER || BotConstants.cfg.ownerId == id
        }

        fun quickRegister(id: Long): BotUser {
            val user = BotUser(id)
            BotConstants.users = BotConstants.users.plusElement(user)
            return user
        }

        fun getUser(qq: Long): BotUser? {
            for (user in BotConstants.users) {
                if (user.userQQ == qq) {
                    return user
                }
            }
            return null
        }
    }
}