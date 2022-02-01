/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.sessions.commands.guessnumber

import io.github.starwishsama.comet.commands.chats.GuessNumberCommand
import io.github.starwishsama.comet.sessions.Session
import io.github.starwishsama.comet.sessions.SessionTarget
import java.time.Duration
import java.time.LocalDateTime

open class GuessNumberSession(override val target: SessionTarget, val answer: Int) :
    Session(target, GuessNumberCommand, false) {
    var tryTimes = 0
    lateinit var usedTime: Duration
    lateinit var lastAnswerTime: LocalDateTime

    fun getGuessNumberUser(id: Long): GuessNumberUser? {
        users.forEach {
            if (it is GuessNumberUser && it.id == id) {
                return it
            }
        }
        return null
    }
}