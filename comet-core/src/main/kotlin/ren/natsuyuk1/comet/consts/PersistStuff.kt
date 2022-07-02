package ren.natsuyuk1.comet.consts

import org.jetbrains.exposed.sql.Table
import ren.natsuyuk1.comet.api.command.AbstractCommandNode
import ren.natsuyuk1.comet.api.command.CommandNode
import ren.natsuyuk1.comet.api.command.PlatformCommandSender
import ren.natsuyuk1.comet.api.config.CometConfig
import ren.natsuyuk1.comet.api.config.provider.PersistDataFile
import ren.natsuyuk1.comet.api.database.DatabaseConfig
import ren.natsuyuk1.comet.api.user.UserPermissionTable
import ren.natsuyuk1.comet.api.user.UserTable
import ren.natsuyuk1.comet.commands.HELP
import ren.natsuyuk1.comet.commands.HelpCommand
import ren.natsuyuk1.comet.commands.SIGNIN
import ren.natsuyuk1.comet.commands.SignInCommand
import ren.natsuyuk1.comet.objects.pjsk.ProjectSekaiDataTable
import ren.natsuyuk1.comet.objects.pjsk.ProjectSekaiUserDataTable

val cometConfigs: List<PersistDataFile<*>> =
    listOf(
        CometConfig,
        DatabaseConfig,
    )

val cometTables: List<Table> =
    listOf(
        UserTable,
        UserPermissionTable,
        ProjectSekaiDataTable,
        ProjectSekaiUserDataTable,
    )

val defaultCommands: List<AbstractCommandNode<*>> =
    listOf(
        CommandNode(HELP) { sender, raw, wrapper, user ->
            HelpCommand(
                sender as PlatformCommandSender,
                raw,
                wrapper,
                user
            )
        },
        CommandNode(SIGNIN) { sender, raw, wrapper, user ->
            SignInCommand(
                sender as PlatformCommandSender,
                raw,
                wrapper,
                user
            )
        }
    )
