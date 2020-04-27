package io.github.starwishsama.nbot.objects

import io.github.starwishsama.nbot.BotConstants
import io.github.starwishsama.nbot.enums.UserLevel
import io.github.starwishsama.nbot.util.BotUtil.getLevel
import java.time.LocalDateTime
import kotlin.collections.ArrayList


data class BotUser(var userQQ: Long,
                   var lastCheckInTime : LocalDateTime = LocalDateTime.now(),
                   var checkInPoint : Double = 0.0,
                   var checkInTime : Int = 0,
                   var bindServerAccount: String? = null,
                   var msgVL : Int = 0,
                   var r6sAccount: String? = null,
                   var level: UserLevel = UserLevel.USER,
                   var commandTime : Int = 100,
                   var checkInGroup: Long = 0,
                   var permissions: List<String> = ArrayList(),
                   var biliSubs: List<String> = ArrayList()) {
    fun decreaseTime() {
        if (level <= UserLevel.VIP) {
            commandTime--
        }
    }

    fun decreaseTime(time: Int) {
        if (level <= UserLevel.VIP) {
            commandTime -= time
        }
    }

    fun addPoint(point: Double) {
        checkInPoint += point
    }

    fun addTime(time: Int) {
        if (level == UserLevel.USER) {
            commandTime += time
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

    fun hasPermission(permission: String): Boolean {
        return !permissions.isNullOrEmpty() && permissions.contains(permission)
    }

    /**
     * 比较权限组
     * @return 自己的权限组是否大于需要比较的权限组
     */
    fun compareLevel(cmdLevel: UserLevel): Boolean{
        return this.level >= cmdLevel
    }

    companion object {
        fun isUserExist(qq: Long): Boolean {
            return getUser(qq) != null
        }

        fun isBotAdmin(id: Long): Boolean {
            return getLevel(id) >= UserLevel.ADMIN
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