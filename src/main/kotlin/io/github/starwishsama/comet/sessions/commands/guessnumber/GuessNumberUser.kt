package io.github.starwishsama.comet.sessions.commands.guessnumber

import io.github.starwishsama.comet.sessions.SessionUser

class GuessNumberUser(override val userId: Long, val username: String) : SessionUser(userId) {
    var guessTime = 0
}