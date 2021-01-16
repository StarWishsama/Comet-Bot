package io.github.starwishsama.comet

import com.google.auto.service.AutoService
import io.github.starwishsama.comet.file.DataSetup
import io.github.starwishsama.comet.utils.FileUtil
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.console.plugin.version
import net.mamoe.mirai.event.events.BotActiveEvent
import net.mamoe.mirai.event.events.BotOfflineEvent
import net.mamoe.mirai.event.globalEventChannel
import java.time.LocalDateTime
import kotlin.time.ExperimentalTime

/**
 * Comet 的 Console 插件部分
 *
 * 注意: 使用 Console 启动是实验性功能!
 * 由于 Comet 的设计缺陷, 在 Console 有多个机器人的情况下可能无法正常工作.
 */
@AutoService(JvmPlugin::class)
object CometConsoleLoader : KotlinPlugin(
    JvmPluginDescription(
        id = "io.github.starwishsama.comet",
        version = BuildConfig.version,
    ) {
        name("Comet")
    }
) {

    @ExperimentalTime
    override fun onEnable() {
        BotVariables.filePath = this@CometConsoleLoader.dataFolder

        BotVariables.startTime = LocalDateTime.now()
        FileUtil.initLog()

        logger.info(
            """
        
           ______                     __ 
          / ____/___  ____ ___  ___  / /_
         / /   / __ \/ __ `__ \/ _ \/ __/
        / /___/ /_/ / / / / / /  __/ /_  
        \____/\____/_/ /_/ /_/\___/\__/  
    """
        )

        DataSetup.init()

        globalEventChannel().subscribeOnce<BotActiveEvent> {
            if (Bot.instances.isEmpty()) {
                logger.warning("找不到已登录的 Bot, 请登录后重启 Console 重试!")
                return@subscribeOnce
            }

            Bot.instances.forEach { bot ->
                Comet.invokePostTask(bot, logger)
            }

            logger.info("Comet 已启动! 版本号 $version")
        }

        globalEventChannel().subscribeAlways<BotOfflineEvent> {
            invokeWhenClose()
        }
    }

    override fun onDisable() {
        invokeWhenClose()
    }
}