package io.github.starwishsama.comet.objects

import com.google.gson.annotations.SerializedName
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.utils.BotUtil.getLevel
import java.time.LocalDateTime

class BotUser(@SerializedName("userQQ") var id: Long) {
    var lastCheckInTime: LocalDateTime = LocalDateTime.now().minusDays(1)
    var checkInPoint: Double = 0.0
    var checkInTime: Int = 0
    var bindServerAccount: String? = null
    var r6sAccount: String? = null
    var level: UserLevel = UserLevel.USER
    var commandTime: Int = 100
    var checkInGroup: Long = 0
    private var permissions: List<String> = ArrayList()

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
        return permissions.contains(permission) || isBotOwner()
    }

    fun getPermissions() : String {
        var permissions = ""
        this.permissions.forEach {
            permissions = "$permissions$it "
        }

        return permissions.trim()
    }

    /**
     * 比较权限组
     * @return 自己的权限组是否大于需要比较的权限组
     */
    fun compareLevel(cmdLevel: UserLevel): Boolean {
        return this.level >= cmdLevel
    }

    fun isBotAdmin(): Boolean {
        return level >= UserLevel.ADMIN
    }

    fun isBotOwner(): Boolean {
        return level == UserLevel.OWNER || BotVariables.cfg.ownerId == id
    }

    fun addPermission(permission: String) {
        permissions = permissions + permission
    }

    companion object {
        fun isBotAdmin(id: Long): Boolean {
            return getLevel(id) >= UserLevel.ADMIN
        }

        fun isBotOwner(id: Long): Boolean {
            return getLevel(id) == UserLevel.OWNER || BotVariables.cfg.ownerId == id
        }

        fun quickRegister(id: Long): BotUser {
            val user = BotUser(id)
            BotVariables.users.plusAssign(user)
            return user
        }

        fun getUser(qq: Long): BotUser? {
            for (user in BotVariables.users) {
                if (user.id == qq) {
                    return user
                }
            }
            return null
        }
    }
}