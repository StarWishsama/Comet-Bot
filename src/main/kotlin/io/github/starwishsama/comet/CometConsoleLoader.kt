package io.github.starwishsama.comet

/**
 * Comet 的 Console 插件部分
 *
 * 注意: 使用 Console 启动是实验性功能!
 * 由于 Comet 的设计缺陷, 在 Console 有多个机器人的情况下可能无法正常工作.
@AutoService(JvmPlugin::class)
object CometConsoleLoader : KotlinPlugin(
    JvmPluginDescription(
        id = "io.github.starwishsama.comet",
        version = "0.6-M2",
    ) {
        name("Comet")
    }
) {

    @ExperimentalTime
    override fun onEnable() {

        logger.info("Comet 已启动, 正在初始化 ${Bot.botInstances} 个 Bot...")

        if (Bot.botInstances.isEmpty()) {
            logger.warning("找不到已登录的 Bot, 请登录后重启 Console 重试!")
            return
        }

        Bot.botInstances.forEach { bot ->
            Comet.invokePostTask(bot, logger)
        }
    }

    override fun onDisable() {
        invokeWhenClose()
    }
}*/