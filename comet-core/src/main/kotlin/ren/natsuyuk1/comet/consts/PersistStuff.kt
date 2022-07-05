package ren.natsuyuk1.comet.consts

import ren.natsuyuk1.comet.api.command.AbstractCommandNode
import ren.natsuyuk1.comet.api.command.CommandNode
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

val cometTables =
    arrayOf(
        UserTable,
        UserPermissionTable,
        ProjectSekaiDataTable,
        ProjectSekaiUserDataTable,
    )

val defaultCommands: List<AbstractCommandNode<*>> =
    listOf(
        CommandNode(HELP) { sender, wrapper, user -> HelpCommand(sender, wrapper, user) },
        CommandNode(SIGNIN) { sender, wrapper, user -> SignInCommand(sender, wrapper, user) }
    )
