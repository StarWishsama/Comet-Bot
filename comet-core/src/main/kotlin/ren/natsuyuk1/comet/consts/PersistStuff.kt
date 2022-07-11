package ren.natsuyuk1.comet.consts

import moe.sdl.yac.core.subcommands
import ren.natsuyuk1.comet.api.command.AbstractCommandNode
import ren.natsuyuk1.comet.api.command.CommandNode
import ren.natsuyuk1.comet.api.config.CometConfig
import ren.natsuyuk1.comet.api.config.provider.PersistDataFile
import ren.natsuyuk1.comet.api.database.DatabaseConfig
import ren.natsuyuk1.comet.api.user.UserPermissionTable
import ren.natsuyuk1.comet.api.user.UserTable
import ren.natsuyuk1.comet.commands.*
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
        CommandNode(HELP) { comet, sender, subject, wrapper, user ->
            HelpCommand(
                comet,
                sender,
                subject,
                wrapper,
                user
            )
        },
        CommandNode(SIGNIN) { comet, sender, subject, wrapper, user ->
            SignInCommand(
                comet,
                sender,
                subject,
                wrapper,
                user
            )
        },
        CommandNode(PROJECTSEKAI) { comet, sender, subject, wrapper, user ->
            ProjectSekaiCommand(comet, sender, subject, wrapper, user)
                .subcommands(
                    ProjectSekaiCommand.Bind(sender, subject, user),
                    ProjectSekaiCommand.Event(sender, subject, user),
                    ProjectSekaiCommand.Prediction(sender, subject)
                )
        },
    )
