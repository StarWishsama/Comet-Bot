package io.github.starwishsama.nbot.sessions.commands.guessnumber

import io.github.starwishsama.nbot.sessions.SessionUser

class GuessNumberUser(override var userId: Long, var username: String): SessionUser(userId){
    var guessTime = 0
}