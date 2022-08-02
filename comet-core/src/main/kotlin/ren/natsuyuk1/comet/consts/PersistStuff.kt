package ren.natsuyuk1.comet.consts

import moe.sdl.yac.core.subcommands
import ren.natsuyuk1.comet.api.command.AbstractCommandNode
import ren.natsuyuk1.comet.api.command.CommandNode
import ren.natsuyuk1.comet.api.config.provider.PersistDataFile
import ren.natsuyuk1.comet.api.database.DatabaseConfig
import ren.natsuyuk1.comet.api.user.UserPermissionTable
import ren.natsuyuk1.comet.api.user.UserTable
import ren.natsuyuk1.comet.commands.*
import ren.natsuyuk1.comet.objects.pjsk.ProjectSekaiDataTable
import ren.natsuyuk1.comet.objects.pjsk.ProjectSekaiUserDataTable

val cometConfigs: List<PersistDataFile<*>> =
    listOf(
        DatabaseConfig
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
            HelpCommand(comet, sender, subject, wrapper, user)
        },
        CommandNode(SIGNIN) { comet, sender, subject, wrapper, user ->
            SignInCommand(comet, sender, subject, wrapper, user)
        },
        CommandNode(
            PROJECTSEKAI,
            listOf(
                ProjectSekaiCommand.Bind.BIND,
                ProjectSekaiCommand.Event.EVENT,
                ProjectSekaiCommand.Prediction.PREDICTION,
                ProjectSekaiCommand.Info.INFO
            )
        ) { comet, sender, subject, wrapper, user ->
            ProjectSekaiCommand(comet, sender, subject, wrapper, user)
                .subcommands(
                    ProjectSekaiCommand.Bind(subject, sender, user),
                    ProjectSekaiCommand.Event(subject, sender, user),
                    ProjectSekaiCommand.Prediction(subject, sender, user),
                    ProjectSekaiCommand.Info(subject, sender, user),
                )
        },
        CommandNode(VERSION) { comet, sender, subject, wrapper, user ->
            VersionCommand(comet, sender, subject, wrapper, user)
        },
        CommandNode(JIKI) { comet, sender, subject, wrapper, user ->
            JikiPediaCommand(comet, sender, subject, wrapper, user)
        },
        CommandNode(
            BILIBILI,
            listOf(BiliBiliCommand.User.USER, BiliBiliCommand.Dynamic.DYNAMIC, BiliBiliCommand.Video.VIDEO)
        ) { comet, sender, subject, wrapper, user ->
            BiliBiliCommand(comet, sender, subject, wrapper, user)
                .subcommands(
                    BiliBiliCommand.User(subject, sender, user),
                    BiliBiliCommand.Dynamic(subject, sender, user),
                    BiliBiliCommand.Video(subject, sender, user)
                )
        },
        CommandNode(INFO) { comet, sender, subject, wrapper, user ->
            InfoCommand(comet, sender, subject, wrapper, user)
        },
        CommandNode(
            GITHUB,
            listOf(GithubCommand.Subscribe.SUBSCRIBE, GithubCommand.UnSubscribe.UNSUBSCRIBE, GithubCommand.Info.INFO)
        ) { comet, sender, subject, wrapper, user ->
            GithubCommand(comet, sender, subject, wrapper, user)
                .subcommands(
                    GithubCommand.Subscribe(subject, sender, user),
                    GithubCommand.UnSubscribe(subject, sender, user),
                    GithubCommand.Info(comet, subject, sender, user)
                )
        },
        CommandNode(NOABBR) { comet, sender, subject, wrapper, user ->
            NoAbbrCommand(comet, sender, subject, wrapper, user)
        }
    )
