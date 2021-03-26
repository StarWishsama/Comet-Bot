package io.github.starwishsama.comet.sessions.commands.guessnumber

import io.github.starwishsama.comet.commands.chats.GuessNumberCommand
import io.github.starwishsama.comet.sessions.Session
import io.github.starwishsama.comet.sessions.SessionTarget
import java.time.Duration
import java.time.LocalDateTime

open class GuessNumberSession(override val target: SessionTarget, val answer: Int) :
    Session(target, GuessNumberCommand(), false) {
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