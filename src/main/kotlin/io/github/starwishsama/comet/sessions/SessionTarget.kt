/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.sessions

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Member

data class SessionTarget(
    val groupId: Long = 0,
    val targetId: Long = 0
)

fun Contact.toSessionTarget(): SessionTarget {
    return when (this) {
        is Member -> SessionTarget(this.group.id, this.id)
        else -> SessionTarget(targetId = this.id)
    }
}