/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.startup

import io.github.starwishsama.comet.BuildConfig
import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.CometVariables.cfg
import io.github.starwishsama.comet.CometVariables.cometServiceServer
import io.github.starwishsama.comet.CometVariables.daemonLogger
import io.github.starwishsama.comet.CometVariables.logger
import io.github.starwishsama.comet.api.command.CommandManager
import io.github.starwishsama.comet.api.command.MessageHandler
import io.github.starwishsama.comet.api.thirdparty.bilibili.*
import io.github.starwishsama.comet.api.thirdparty.jikipedia.JikiPediaApi
import io.github.starwishsama.comet.api.thirdparty.noabbr.NoAbbrApi
import io.github.starwishsama.comet.api.thirdparty.rainbowsix.R6StatsApi
import io.github.starwishsama.comet.api.thirdparty.twitter.TwitterApi
import io.github.starwishsama.comet.commands.chats.*
import io.github.starwishsama.comet.commands.console.BroadcastCommand
import io.github.starwishsama.comet.commands.console.DebugCommand
import io.github.starwishsama.comet.file.DataSaveHelper
import io.github.starwishsama.comet.file.DataSetup
import io.github.starwishsama.comet.listeners.*
import io.github.starwishsama.comet.managers.NetworkRequestManager
import io.github.starwishsama.comet.objects.tasks.GroupFileAutoRemover
import io.github.starwishsama.comet.service.RetrofitLogger
import io.github.starwishsama.comet.service.gacha.GachaService
import io.github.starwishsama.comet.service.pusher.PusherManager
import io.github.starwishsama.comet.service.server.CometServiceServer
import io.github.starwishsama.comet.utils.RuntimeUtil
import io.github.starwishsama.comet.utils.StringUtil.getLastingTimeAsString
import io.github.starwishsama.comet.utils.TaskUtil
import io.github.starwishsama.comet.utils.network.NetUtil
import kotlinx.coroutines.runBlocking
import net.kronos.rkon.core.Rcon
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.Command
import okhttp3.OkHttpClient
import java.net.InetSocketAddress
import java.net.Proxy
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

object CometRuntime {
    fun postSetup() {
        CometVariables.startTime = LocalDateTime.now()

        logger.info(
            """
        
           ______                     __ 
          / ____/___  ____ ___  ___  / /_
         / /   / __ \/ __ `__ \/ _ \/ __/
        / /___/ /_/ / / / / / /  __/ /_  
        \____/\____/_/ /_/ /_/\___/\__/  


    """
        )

        CometVariables.client = OkHttpClient().newBuilder()
            .callTimeout(3, TimeUnit.SECONDS)
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(3, TimeUnit.SECONDS)
            .writeTimeout(3, TimeUnit.SECONDS)
            .followRedirects(true)
            .hostnameVerifier { _, _ -> true }
            .also {
                if (cfg.proxySwitch) {
                    if (NetUtil.checkProxyUsable()) {
                        it.proxy(Proxy(cfg.proxyType, InetSocketAddress(cfg.proxyUrl, cfg.proxyPort)))
                    }
                }
            }
            .addInterceptor(RetrofitLogger())
            .build()

        DataSetup.init()

        CommandManager.setupCommands(
            arrayOf(
                AdminCommand,
                ArkNightCommand,
                BiliBiliCommand,
                CheckInCommand,
                ClockInCommand,
                io.github.starwishsama.comet.commands.chats.DebugCommand,
                DivineCommand,
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
                KeyWordCommand,
            )
        )

        val consoleCommand = listOf<Command>(
            DebugCommand,
            io.github.starwishsama.comet.commands.console.AdminCommand,
            BroadcastCommand
        )

        consoleCommand.forEach(net.mamoe.mirai.console.command.CommandManager::registerCommand)

        logger.info("[命令] 已注册 " + CommandManager.countCommands() + " 个命令")
    }

    fun shutdownTask() {
        logger.info("[Bot] 正在关闭 Bot...")
        DataSetup.saveAllResources()
        PusherManager.savePushers()
        cometServiceServer?.stop()
        TaskUtil.dispatcher.close()

        if (!TaskUtil.service.isShutdown) {
            TaskUtil.service.shutdown()
        }

        CometVariables.rCon?.disconnect()
        CometVariables.miraiLoggerAppender.close()
        CometVariables.loggerAppender.close()
    }

    fun setupBot(bot: Bot) {
        MessageHandler.startHandler(bot)

        /** 监听器 */
        val listeners = arrayOf(
            BiliBiliShareListener,
            RepeatListener,
            BotGroupStatusListener,
            AutoReplyListener,
            GroupMemberChangedListener,
            GroupRequestListener
        )

        listeners.forEach { it.register(bot) }

        DataSetup.initPerGroupSetting(bot)

        setupRCon()

        runScheduleTasks()

        PusherManager.initPushers(bot)

        startupServer()

        TaskUtil.scheduleAtFixedRate(5, 5, TimeUnit.SECONDS) {
            runBlocking { NetworkRequestManager.schedule() }
        }

        logger.info("彗星 Bot 启动成功, 版本 ${BuildConfig.version}, 耗时 ${CometVariables.startTime.getLastingTimeAsString()}")

        TaskUtil.schedule { GachaService.loadAllPools() }
    }

    fun setupRCon() {
        val address = cfg.rConUrl
        val password = cfg.rConPassword
        if (address != null && password != null && CometVariables.rCon == null) {
            CometVariables.rCon = Rcon(address, cfg.rConPort, password.toByteArray())
        }
    }

    private fun startupServer() {
        if (!cfg.webHookSwitch) {
            return
        }

        try {
            val customSuffix = cfg.webHookAddress.replace("http://", "").replace("https://", "").split("/")
            cometServiceServer = CometServiceServer(cfg.webHookPort, customSuffix.last())
        } catch (e: Exception) {
            daemonLogger.warning("Comet 服务端启动失败", e)
        }
    }

    private fun runScheduleTasks() {
        TaskUtil.schedule { DataSaveHelper.checkOldFiles() }

        val apis =
            arrayOf(DynamicApi, JikiPediaApi, LiveApi, NoAbbrApi, R6StatsApi, SearchApi, TwitterApi, UserApi, VideoApi)

        /** 定时任务 */
        DataSaveHelper.scheduleBackup()
        DataSaveHelper.scheduleSave()

        apis.forEach {
            if (it.duration > 0) {
                TaskUtil.scheduleAtFixedRate(it.duration.toLong(), it.duration.toLong(), TimeUnit.HOURS) {
                    it.resetTime()
                }
            }
        }

        TaskUtil.scheduleAtFixedRate(1, 1, TimeUnit.HOURS) {
            GroupFileAutoRemover.execute()
        }

        TaskUtil.scheduleAtFixedRate(1, 1, TimeUnit.HOURS) {
            RuntimeUtil.forceGC()
        }
    }
}
