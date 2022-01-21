/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package ren.natsuyuk1.comet.sessions

import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import java.time.LocalDateTime

open class Session(
    open val target: SessionTarget,
    val creator: ChatCommand,
    open val silent: Boolean = false,
) {
    val users: MutableSet<SessionUser> = mutableSetOf()
    val createdTime: LocalDateTime = LocalDateTime.now()

    override fun toString(): String {
        return "Session#${hashCode()} {target=$target, silent=${silent}, usersCount=${users.size}}"
    }
}