package ren.natsuyuk1.comet.consts

import ren.natsuyuk1.comet.api.command.AbstractCommandNode
import ren.natsuyuk1.comet.api.command.CommandNode
import ren.natsuyuk1.comet.api.config.CometGlobalConfig
import ren.natsuyuk1.comet.api.config.provider.PersistDataFile
import ren.natsuyuk1.comet.api.database.DatabaseConfig
import ren.natsuyuk1.comet.api.event.EventManagerConfig
import ren.natsuyuk1.comet.api.user.UserPermissionTable
import ren.natsuyuk1.comet.api.user.UserTable
import ren.natsuyuk1.comet.commands.*
import ren.natsuyuk1.comet.objects.arcaea.ArcaeaUserDataTable
import ren.natsuyuk1.comet.objects.config.CometServerConfig
import ren.natsuyuk1.comet.objects.config.TwitterConfig
import ren.natsuyuk1.comet.objects.github.data.GithubRepoData
import ren.natsuyuk1.comet.objects.keyword.KeyWordData
import ren.natsuyuk1.comet.objects.nowcmd.NowCmdConfigTable
import ren.natsuyuk1.comet.objects.pjsk.ProjectSekaiDataTable
import ren.natsuyuk1.comet.objects.pjsk.ProjectSekaiUserDataTable

val cometPersistDataFile: List<PersistDataFile<*>> =
    listOf(
        CometGlobalConfig,
        DatabaseConfig,
        GithubRepoData,
        EventManagerConfig,
        CometServerConfig,
        KeyWordData,
        TwitterConfig,
    )

val cometTables =
    arrayOf(
        UserTable,
        UserPermissionTable,
        ProjectSekaiDataTable,
        ProjectSekaiUserDataTable,
        NowCmdConfigTable,
        ArcaeaUserDataTable
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
        },
        CommandNode(INFO) { comet, sender, subject, wrapper, user ->
            InfoCommand(comet, sender, subject, wrapper, user)
        },
        CommandNode(
            GITHUB,
            listOf(
                GithubCommand.Subscribe.SUBSCRIBE,
                GithubCommand.UnSubscribe.UNSUBSCRIBE,
                GithubCommand.Info.INFO,
                GithubCommand.Setting.SETTING,
            )
        ) { comet, sender, subject, wrapper, user ->
            GithubCommand(comet, sender, subject, wrapper, user)
        },
        CommandNode(NOABBR) { comet, sender, subject, wrapper, user ->
            NoAbbrCommand(comet, sender, subject, wrapper, user)
        },
        CommandNode(
            BANGUMI,
            listOf(BangumiCommand.Schedule.SCHEDULE, BangumiCommand.Search.SEARCH)
        ) { comet, sender, subject, wrapper, user ->
            BangumiCommand(comet, sender, subject, wrapper, user)
        },
        CommandNode(
            NOW
        ) { comet, sender, subject, wrapper, user ->
            NowCommand(comet, sender, subject, wrapper, user)
        },
        CommandNode(
            ARCAEA,
            listOf(ArcaeaCommand.Bind.BIND, ArcaeaCommand.Info.INFO)
        ) { comet, sender, subject, wrapper, user ->
            ArcaeaCommand(comet, sender, subject, wrapper, user)
        },
        CommandNode(
            KEYWORD
        ) { comet, sender, subject, wrapper, user ->
            KeyWordCommand(comet, sender, subject, wrapper, user)
        },
        CommandNode(
            TWITTER,
            listOf(TwitterCommand.Tweet.TWEET, TwitterCommand.User.USER)
        ) { comet, sender, subject, wrapper, user ->
            TwitterCommand(comet, sender, subject, wrapper, user)
        },
        CommandNode(
            RSS,
            listOf(RSSCommand.Subscribe.SUBSCRIBE, RSSCommand.UnSubscribe.UNSUBSCRIBE, RSSCommand.List.LIST)
        ) { comet, sender, subject, wrapper, user ->
            RSSCommand(comet, sender, subject, wrapper, user)
        },
    )
