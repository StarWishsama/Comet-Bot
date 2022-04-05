package io.github.starwishsama.comet.test.permission

import io.github.starwishsama.comet.commands.chats.*

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
