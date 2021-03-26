package io.github.starwishsama.comet.sessions.commands.roll

import io.github.starwishsama.comet.commands.chats.RollCommand
import io.github.starwishsama.comet.sessions.Session
import io.github.starwishsama.comet.sessions.SessionTarget

class RollSession(
    override var target: SessionTarget,
    val rollItem: String,
    val stopAfterMinute: Int,
    val keyWord: String,
    val rollStarter: Long,
    val count: Int,
) : Session(target, RollCommand(), silent = true)