package io.github.starwishsama.comet.sessions.commands.guessnumber

import io.github.starwishsama.comet.sessions.SessionUser

class GuessNumberUser(override val id: Long, val username: String) : SessionUser(id) {
    @Volatile
    var guessTime = 0
}