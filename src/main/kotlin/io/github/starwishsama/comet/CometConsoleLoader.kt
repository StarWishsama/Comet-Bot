package io.github.starwishsama.comet

import com.google.auto.service.AutoService
import io.github.starwishsama.comet.commands.CommandExecutor
import io.github.starwishsama.comet.file.DataSetup
import io.github.starwishsama.comet.listeners.ConvertLightAppListener
import io.github.starwishsama.comet.listeners.RepeatListener
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.isBotMuted
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import kotlin.time.ExperimentalTime

/**
 * Comet 的 Console 插件部分
 *
 * 注意: 使用 Console 启动是实验性功能!
 * 注意: 由于 Comet 的设计缺陷, 在 Console 有多个机器人的情况下可能无法正常工作.
 */
@AutoService(JvmPlugin::class)
object CometConsoleLoader : KotlinPlugin(
        JvmPluginDescription(
                id = "io.github.starwishsama.comet",
                version = "0.6-M1",
        )) {

    @ExperimentalTime
    override fun onEnable() {
        Bot.botInstances.forEach { bot ->
            /** 命令处理 */
            bot.subscribeMessages {
                always {
                    if (sender.id != 80000000L) {
                        if (this is GroupMessageEvent && group.isBotMuted) return@always

                        val result = CommandExecutor.dispatchCommand(this)
                        try {
                            if (result.msg !is EmptyMessageChain && result.msg.isNotEmpty()) {
                                reply(result.msg)
                            }
                        } catch (e: IllegalArgumentException) {
                            logger.warning("正在尝试发送空消息, 执行的命令为 $result")
                        }
                    }
                }
            }

            /** 监听器 */
            val listeners = arrayOf(ConvertLightAppListener, RepeatListener)

            listeners.forEach { it.register(bot) }
        }
    }

    override fun onDisable() {
        logger.info("[Bot] 正在关闭 Bot...")
        DataSetup.saveFiles()
        BotVariables.service.shutdown()
        BotVariables.rCon?.disconnect()
    }
}