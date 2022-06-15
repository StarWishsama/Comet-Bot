/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 */

package ren.natsuyuk1.comet.api.permission

import ren.natsuyuk1.comet.user.CometUser
import ren.natsuyuk1.comet.user.UserLevel

object PermissionManager {
    val permissions = mutableListOf<CometPermission>()

    fun register(permission: CometPermission) {
        if (permissions.contains(permission)) {
            return
        }

        permissions.add(permission)
    }

    fun register(node: String, level: UserLevel) {
        register(CometPermission(node, level))
    }

    fun getPermission(node: String): CometPermission? = permissions.find { it.nodeName == node }
}

fun CometUser.hasPermission(node: String): Boolean {
    val target = PermissionManager.getPermission(node) ?: return true

    return permissions.contains(target.nodeName) || userLevel >= target.level
}
