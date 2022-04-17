package io.github.starwishsama.comet.test.permission

import io.github.starwishsama.comet.commands.chats.AdminCommand
import io.github.starwishsama.comet.commands.chats.ArkNightCommand
import io.github.starwishsama.comet.commands.chats.BiliBiliCommand
import io.github.starwishsama.comet.commands.chats.CheckInCommand
import io.github.starwishsama.comet.commands.chats.DebugCommand
import io.github.starwishsama.comet.commands.chats.DiceCommand
import io.github.starwishsama.comet.commands.chats.DivineCommand
import io.github.starwishsama.comet.commands.chats.GaokaoCommand
import io.github.starwishsama.comet.commands.chats.GithubCommand
import io.github.starwishsama.comet.commands.chats.GroupConfigCommand
import io.github.starwishsama.comet.commands.chats.GuessNumberCommand
import io.github.starwishsama.comet.commands.chats.HelpCommand
import io.github.starwishsama.comet.commands.chats.InfoCommand
import io.github.starwishsama.comet.commands.chats.JikiPediaCommand
import io.github.starwishsama.comet.commands.chats.KeyWordCommand
import io.github.starwishsama.comet.commands.chats.KickCommand
import io.github.starwishsama.comet.commands.chats.MinecraftCommand
import io.github.starwishsama.comet.commands.chats.MusicCommand
import io.github.starwishsama.comet.commands.chats.MuteCommand
import io.github.starwishsama.comet.commands.chats.NoAbbrCommand
import io.github.starwishsama.comet.commands.chats.PictureSearchCommand
import io.github.starwishsama.comet.commands.chats.PusherCommand
import io.github.starwishsama.comet.commands.chats.R6SCommand
import io.github.starwishsama.comet.commands.chats.RConCommand
import io.github.starwishsama.comet.commands.chats.RSPCommand
import io.github.starwishsama.comet.commands.chats.RollCommand
import io.github.starwishsama.comet.commands.chats.TwitterCommand
import io.github.starwishsama.comet.commands.chats.UnMuteCommand
import io.github.starwishsama.comet.commands.chats.VersionCommand

internal class TestCommandPermission {
    private val commands = arrayOf(
        AdminCommand,
        ArkNightCommand,
        BiliBiliCommand,
        CheckInCommand,
        DebugCommand,
        DivineCommand,
        GaokaoCommand,
        GuessNumberCommand,
        HelpCommand,
        InfoCommand,
        MusicCommand,
        MuteCommand,
        UnMuteCommand,
        PictureSearchCommand,
        R6SCommand,
        RConCommand,
        KickCommand,
        TwitterCommand,
        VersionCommand,
        GroupConfigCommand,
        RSPCommand,
        RollCommand,
        MinecraftCommand,
        PusherCommand,
        GithubCommand,
        DiceCommand,
        NoAbbrCommand,
        JikiPediaCommand,
        KeyWordCommand
    )

    /**@Test
    fun testAllCommandPermission() {
        commands.forEach {
            val fakeUser = CometUser(1, UUID.randomUUID()).also { it.level = it.level }

            TODO()
        }
    }*/
}
