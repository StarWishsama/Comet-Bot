package io.github.starwishsama.comet.objects

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import java.time.LocalDateTime

data class BotUser(
    @JsonProperty("userQQ")
    val id: Long,
    var lastCheckInTime: LocalDateTime = LocalDateTime.MIN,
    var checkInPoint: Double = 0.0,
    var checkInTime: Int = 0,
    var bindServerAccount: String = "",
    var r6sAccount: String = "",
    var level: UserLevel = UserLevel.USER,
    var commandTime: Int = 100,
    var checkInGroup: Long = 0,
    var lastExecuteTime: Long = -1,
    private val permissions: MutableList<String> = mutableListOf(),
    val savedContents: MutableList<MessageWrapper> = mutableListOf()
) {
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

    fun addTime(time: Int, bypass: Boolean = false) {
        if (level == UserLevel.USER && (commandTime <= 1000 || bypass)) {
            commandTime += time
        }
    }

    fun costPoint(point: Double) {
        checkInPoint -= point
    }

    fun plusDay() {
        checkInTime++
    }

    fun resetDay(){
        checkInTime = 1
    }

    fun hasPermission(permission: String): Boolean {
        return permissions.contains(permission) || isBotOwner()
    }

    fun getPermissions(): String = buildString {
        this@BotUser.permissions.forEach {
            append("$it ")
        }
    }.trim()

    /**
     * 比较权限组
     * @return 自己的权限组是否大于等于需要比较的权限组
     */
    fun compareLevel(cmdLevel: UserLevel): Boolean {
        return this.level >= cmdLevel
    }

    fun isBotAdmin(): Boolean {
        return level >= UserLevel.ADMIN || isBotOwner()
    }

    fun isBotOwner(): Boolean {
        return level == UserLevel.OWNER
    }

    fun addPermission(permission: String) {
        permissions.plusAssign(permission)
    }

    /**
     * 判断是否签到过了
     *
     * @author NamelessSAMA
     * @return 是否签到
     */
    fun isChecked(): Boolean {
        val now = LocalDateTime.now()
        val period = lastCheckInTime.toLocalDate().until(now.toLocalDate())

        return period.days == 0
    }

    companion object {
        fun isBotOwner(id: Long): Boolean {
            return getUser(id)?.level == UserLevel.OWNER
        }

        fun quickRegister(id: Long): BotUser {
            val user = BotUser(id)
            return BotVariables.users.putIfAbsent(id, user) ?: user
        }

        fun getUser(id: Long): BotUser? {
            return BotVariables.users[id]
        }

        fun getUserOrRegister(qq: Long): BotUser = getUser(qq) ?: quickRegister(qq)
    }
}