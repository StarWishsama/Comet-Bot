package ren.natsuyuk1.comet.consts

import ren.natsuyuk1.comet.api.command.AbstractCommandNode
import ren.natsuyuk1.comet.api.command.CommandNode
import ren.natsuyuk1.comet.api.config.CometGlobalConfig
import ren.natsuyuk1.comet.api.config.provider.PersistDataFile
import ren.natsuyuk1.comet.api.database.DatabaseConfig
import ren.natsuyuk1.comet.api.event.EventManagerConfig
import ren.natsuyuk1.comet.api.task.CronTasks
import ren.natsuyuk1.comet.api.user.UserPermissionTable
import ren.natsuyuk1.comet.api.user.UserTable
import ren.natsuyuk1.comet.commands.*
import ren.natsuyuk1.comet.objects.apex.ApexLegendDataTable
import ren.natsuyuk1.comet.objects.arcaea.ArcaeaUserDataTable
import ren.natsuyuk1.comet.objects.command.now.NowCmdConfigTable
import ren.natsuyuk1.comet.objects.command.picturesearch.PictureSearchConfigTable
import ren.natsuyuk1.comet.objects.config.*
import ren.natsuyuk1.comet.objects.github.data.GitHubRepoData
import ren.natsuyuk1.comet.objects.keyword.KeyWordData
import ren.natsuyuk1.comet.objects.pjsk.ProjectSekaiDataTable
import ren.natsuyuk1.comet.objects.pjsk.ProjectSekaiUserDataTable
import ren.natsuyuk1.comet.objects.pjsk.local.ProjectSekaiLocalFileTable
import ren.natsuyuk1.comet.pusher.CometPusherContextTable
import ren.natsuyuk1.comet.service.RateLimitData

val cometPersistDataFile: List<PersistDataFile<*>> =
    listOf(
        CometGlobalConfig,
        DatabaseConfig,
        GitHubRepoData,
        EventManagerConfig,
        CometServerConfig,
        KeyWordData,
        TwitterConfig,
        IpdbConfig,
        PushTemplateConfig,
        FeatureConfig,
        RateLimitData,
    )

val cometTables =
    arrayOf(
        UserTable,
        UserPermissionTable,
        ProjectSekaiDataTable,
        ProjectSekaiUserDataTable,
        NowCmdConfigTable,
        ArcaeaUserDataTable,
        CometPusherContextTable,
        ApexLegendDataTable,
        PictureSearchConfigTable,
        CronTasks,
        ProjectSekaiLocalFileTable,
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
                ProjectSekaiCommand.Info.INFO,
                ProjectSekaiCommand.Chart.CHART,
                ProjectSekaiCommand.Music.MUSIC,
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
            RAND
        ) { comet, sender, subject, wrapper, user ->
            RandCommand(comet, sender, subject, wrapper, user)
        },
        CommandNode(
            ARCAEA,
            listOf(ArcaeaCommand.Bind.BIND, ArcaeaCommand.Info.INFO, ArcaeaCommand.Best30.BEST30)
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
        CommandNode(
            APEX,
            listOf(ApexCommand.Bind.BIND, ApexCommand.Info.INFO)
        ) { comet, sender, subject, wrapper, user ->
            ApexCommand(comet, sender, subject, wrapper, user)
        },
        CommandNode(
            PICTURESEARCH,
            listOf(PictureSearchCommand.Source.SOURCE)
        ) { comet, sender, subject, wrapper, user ->
            PictureSearchCommand(comet, sender, subject, wrapper, user)
        },
        CommandNode(
            IP,
        ) { comet, sender, subject, wrapper, user ->
            IPCommand(comet, sender, subject, wrapper, user)
        },
        CommandNode(
            PUSH_TEMPLATE,
            listOf(
                PushTemplateCommand.New.NEW,
                PushTemplateCommand.List.LIST,
                PushTemplateCommand.Subscribe.SUBSCRIBE,
                PushTemplateCommand.UnSubscribe.UNSUBSCRIBE,
                PushTemplateCommand.Remove.REMOVE
            )
        ) { comet, sender, subject, wrapper, user ->
            PushTemplateCommand(comet, sender, subject, wrapper, user)
        },
        CommandNode(
            MUTE
        ) { comet, sender, subject, wrapper, user ->
            MuteCommand(comet, sender, subject, wrapper, user)
        },
        CommandNode(
            DEBUG
        ) { comet, sender, subject, wrapper, user ->
            DebugCommand(comet, sender, subject, wrapper, user)
        },
        CommandNode(
            MINECRAFT
        ) { comet, sender, subject, wrapper, user ->
            MinecraftCommand(comet, sender, subject, wrapper, user)
        },
        CommandNode(
            COIN
        ) { comet, sender, subject, wrapper, user ->
            CoinCommand(comet, sender, subject, wrapper, user)
        },
    )
