package ren.natsuyuk1.comet.commands

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import moe.sdl.yac.parameters.options.flag
import moe.sdl.yac.parameters.options.option
import org.slf4j.LoggerFactory
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.command.CometCommand
import ren.natsuyuk1.comet.api.command.CommandProperty
import ren.natsuyuk1.comet.api.command.PlatformCommandSender
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.api.user.UserLevel
import ren.natsuyuk1.comet.api.wrapper.WrapperLoader
import ren.natsuyuk1.comet.config.branch
import ren.natsuyuk1.comet.config.hash
import ren.natsuyuk1.comet.config.version
import ren.natsuyuk1.comet.consts.coreUpTimer
import ren.natsuyuk1.comet.util.toMessageWrapper
import ren.natsuyuk1.comet.utils.datetime.toFriendly
import ren.natsuyuk1.comet.utils.systeminfo.RuntimeUtil

val DEBUG = CommandProperty(
    "debug",
    description = "调试用命令",
    helpText = "调试命令随时更改, 无帮助文本",
    permissionLevel = UserLevel.OWNER,
    executeConsumePoint = 0
)

class DebugCommand(
    comet: Comet,
    sender: PlatformCommandSender,
    subject: PlatformCommandSender,
    message: MessageWrapper,
    user: CometUser,
) : CometCommand(comet, sender, subject, message, user, DEBUG) {
    private val logLevel by option("--loglevel", "-ll")
    private val memory by option("--memory").flag()
    private val info by option("--info", "-i").flag()
    private val maintain by option("--maintain", "-m").flag()

    override suspend fun run() {
        when {
            maintain -> {
                comet.maintainenceMode = !comet.maintainenceMode
                subject.sendMessage("Maintainence mode: ${comet.maintainenceMode}".toMessageWrapper())
            }

            logLevel != null -> {
                val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
                rootLogger.level = Level.toLevel(logLevel, Level.INFO)

                subject.sendMessage("Root Logger level has changed to ${rootLogger.level}".toMessageWrapper())
            }

            memory -> {
                subject.sendMessage(RuntimeUtil.getMemoryInfo().toMessageWrapper())
            }

            info -> {
                subject.sendMessage(
                    buildString {
                        append(
                            """
                    ☄ Comet Bot - $version [$branch-$hash]   
                    已运行了 ${coreUpTimer.measureDuration().toFriendly()}
                    Made with ❤
                            """.trimIndent()
                        )
                        appendLine()
                        appendLine()
                        append("已加载的服务 >")
                        appendLine()
                        append(WrapperLoader.getServicesInfo())
                        appendLine()
                        append("JVM Version: ${RuntimeUtil.jvmVersion}")
                        appendLine()
                        append("Running on ${RuntimeUtil.getOsInfo()}")
                    }.toMessageWrapper()
                )
            }
        }
    }
}
