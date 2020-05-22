package io.github.starwishsama.nbot.sessions.commands.guessnumber

import io.github.starwishsama.nbot.commands.subcommands.GuessNumberCommand
import io.github.starwishsama.nbot.sessions.Session
import java.time.Duration
import java.time.LocalDateTime

open class GuessNumberSession(override var groupId: Long, val answer: Int): Session(groupId, GuessNumberCommand()) {
    val startTime: LocalDateTime = LocalDateTime.now()
    lateinit var usedTime: Duration
    lateinit var lastAnswerTime: LocalDateTime

    fun getGNUser(id: Long) : GuessNumberUser? {
        users.forEach {
            if (it is GuessNumberUser && it.userId == id) {
                return it
            }
        }
        return null
    }
}