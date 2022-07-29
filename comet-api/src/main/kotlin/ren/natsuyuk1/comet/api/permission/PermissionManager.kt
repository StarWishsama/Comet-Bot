/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 */

package ren.natsuyuk1.comet.api.permission

import org.jetbrains.exposed.sql.transactions.transaction
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.api.user.UserLevel

object PermissionManager {
    private val permissions = mutableListOf<CometPermission>()

    fun register(permission: CometPermission) {
        if (permissions.contains(permission)) {
            return
        }

        permissions.add(permission)
    }

    fun register(node: String, level: UserLevel) {
        register(CometPermission(node, level))
    }

    fun checkWildCardPermission(user: CometUser, permission: CometPermission): Boolean {
        val nodePart = permission.nodeName.split(".")
        val wildcard = nodePart.lastIndexOf("*")

        if (wildcard == -1) {
            return user.permissions.contains(permission.nodeName)
        } else {
            permissions.forEach { p ->
                val targetPart = p.nodeName.split(".")
                val sameNodePart = nodePart.subList(0, wildcard)

                for (i in sameNodePart.indices) {
                    if (sameNodePart[i] != targetPart[i]) {
                        return false
                    }
                }
            }

            return true
        }
    }

    fun getPermission(node: String): CometPermission? = permissions.find { it.nodeName == node }
}

fun CometUser.hasPermission(node: String): Boolean {
    return transaction {
        val target = PermissionManager.getPermission(node) ?: return@transaction true

        return@transaction userLevel >= target.level
            || permissions.contains(target.nodeName)
            || PermissionManager.checkWildCardPermission(this@hasPermission, target)
    }
}
