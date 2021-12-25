/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.objects

import com.fasterxml.jackson.annotation.JsonIgnore
import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.managers.PermissionManager
import io.github.starwishsama.comet.objects.enums.UserLevel
import io.github.starwishsama.comet.objects.permission.CometPermission
import java.time.LocalDateTime
import java.util.*

data class CometUser(
    val id: Long,
    val uuid: UUID,
    var checkInDateTime: LocalDateTime = LocalDateTime.now().minusDays(1),
    var coin: Double = 0.0,
    var checkInCount: Int = 0,
    var r6sAccount: String = "",
    var level: UserLevel = UserLevel.USER,
    var triggerCommandTime: Long = -1,
    private val permissions: MutableSet<CometPermission> = mutableSetOf(),
) {
    fun addPoint(point: Number) {
        coin += point.toDouble()
    }

    fun consumePoint(point: Number) {
        coin -= point.toDouble()
    }

    fun plusDay() {
        checkInCount++
    }

    fun resetDay() {
        checkInCount = 1
    }

    fun hasPermission(nodeName: String): Boolean {
        val target = PermissionManager.getPermission(nodeName) ?: return true

        return target.defaultLevel < level || permissions.find { it.name == target.name } != null
    }

    fun getPermissions(): String = buildString {
        this@CometUser.permissions.forEach {
            append("${it.name} ")
        }
    }.trim()

    /**
     * 比较权限组
     * @return 自己的权限组是否大于等于需要比较的权限组
     */
    fun compareLevel(cmdLevel: UserLevel): Boolean {
        return this.level >= cmdLevel
    }

    @JsonIgnore
    fun isBotAdmin(): Boolean {
        return level >= UserLevel.ADMIN || isBotOwner()
    }

    @JsonIgnore
    fun isBotOwner(): Boolean {
        return level == UserLevel.OWNER
    }

    fun addPermission(nodeName: String) {
        permissions.add(PermissionManager.getPermission(nodeName) ?: return)
    }

    fun removePermission(nodeName: String) {
        permissions.remove(PermissionManager.getPermission(nodeName) ?: return)
    }

    /**
     * 判断是否签到过了
     *
     * @author StarWishsama
     * @return 是否签到
     */
    @JsonIgnore
    fun isChecked(): Boolean {
        val now = LocalDateTime.now()
        val period = checkInDateTime.toLocalDate().until(now.toLocalDate())

        return period.days == 0
    }

    /**
     * 判断是否处于冷却状态
     *
     * @param silent 检查时不更新调用时间
     * @param coolDown 冷却时长, 单位秒
     *
     * @return 是否处于冷却状态
     */
    fun checkCoolDown(silent: Boolean = false, coolDown: Int = CometVariables.cfg.coolDownTime): Boolean {
        val currentTime = System.currentTimeMillis()

        return if (triggerCommandTime < 0) {
            if (!silent) triggerCommandTime = currentTime
            true
        } else {
            val hasCoolDown = currentTime - triggerCommandTime >= coolDown * 1000
            if (!silent) triggerCommandTime = currentTime
            hasCoolDown
        }
    }

    companion object {
        fun quickRegister(id: Long): CometUser {
            CometVariables.cometUsers[id].apply {
                val register = CometUser(id, UUID.randomUUID())
                return this ?: register.also { CometVariables.cometUsers.putIfAbsent(id, register) }
            }
        }

        fun getUser(id: Long): CometUser? {
            return CometVariables.cometUsers[id]
        }

        fun getUserOrRegister(qq: Long): CometUser = getUser(qq) ?: quickRegister(qq)
    }
}