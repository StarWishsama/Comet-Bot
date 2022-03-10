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

import io.github.starwishsama.comet.api.command.interfaces.ConversationCommand
import java.time.LocalDateTime

/**
 * [Session]
 *
 * 会话，可通过 [ConversationCommand] 处理聊天消息
 *
 * @param target [SessionTarget] 会话需要监听的对象, 可以是群或人
 * @param creator [ConversationCommand] 处理会话的命令
 * @param silent 是否打断命令执行, 为真时不处理其他命令
 */
open class Session(
    open val target: SessionTarget,
    val creator: ConversationCommand,
    open val silent: Boolean = false,
) {
    val users: MutableSet<SessionUser> = mutableSetOf()
    val createdTime: LocalDateTime = LocalDateTime.now()
    var lastTriggerTime: LocalDateTime = createdTime

    fun update() {
        lastTriggerTime = LocalDateTime.now()
    }

    override fun toString(): String {
        return "Session#${hashCode()} {target=$target, silent=${silent}, usersCount=${users.size}}"
    }
}