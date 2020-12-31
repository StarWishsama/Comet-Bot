package io.github.starwishsama.comet.commands.chats

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.Versions
import io.github.starwishsama.comet.api.annotations.CometCommand
import io.github.starwishsama.comet.api.command.CommandExecutor
import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.api.command.interfaces.UnDisableableCommand
import io.github.starwishsama.comet.api.thirdparty.bilibili.BiliBiliMainApi
import io.github.starwishsama.comet.api.thirdparty.twitter.TwitterApi
import io.github.starwishsama.comet.api.thirdparty.youtube.YoutubeApi
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.exceptions.ApiException
import io.github.starwishsama.comet.file.DataSetup
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.pushers.*
import io.github.starwishsama.comet.sessions.SessionManager
import io.github.starwishsama.comet.utils.CometUtil
import io.github.starwishsama.comet.utils.CometUtil.sendMessage
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import io.github.starwishsama.comet.utils.StringUtil.isNumeric
import io.github.starwishsama.comet.utils.network.NetUtil
import io.github.starwishsama.comet.utils.network.RssUtil
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.asMessageChain
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import org.openqa.selenium.By
import org.openqa.selenium.Dimension
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.support.ui.ExpectedCondition
import org.openqa.selenium.support.ui.WebDriverWait
import java.io.IOException
import java.util.concurrent.ThreadPoolExecutor
import kotlin.time.ExperimentalTime

@CometCommand
class DebugCommand : ChatCommand, UnDisableableCommand {
    @ExperimentalTime
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (args.isNotEmpty() && CometUtil.isNoCoolDown(event.sender.id)) {
            when (args[0]) {
                "reload" -> {
                    if (user.isBotOwner()) {
                        return try {
                            DataSetup.reload()
                            "重载成功.".sendMessage()
                        } catch (e: IOException) {
                            "在重载时发生了异常.".sendMessage()
                        }
                    }
                }
                "session" -> {
                    if (user.isBotOwner()) {
                        val sb = StringBuilder("目前活跃的会话列表: \n").apply {
                            val sessions = SessionManager.getSessions()

                            if (sessions.isEmpty()) {
                                append("无")
                            } else {
                                for ((i, session) in sessions.withIndex()) {
                                    append(i + 1).append(" ").append(session.toString()).append("\n")
                                }
                            }
                        }
                        return sb.toString().trim().sendMessage()
                    }
                }
                "info" -> {
                    val ping = try {
                        NetUtil.checkPingValue()
                    } catch (t: IOException) {
                        -1L
                    }
                    return ("彗星 Bot ${Versions.version}\n" +
                            "今日もかわいい~\n" +
                            "已注册命令数: ${CommandExecutor.countCommands()}\n" +
                            CometUtil.getMemoryUsage() + "\n" +
                            "与服务器的延迟为 $ping ms\n" +
                            "构建时间: ${Versions.buildTime}"
                            ).sendMessage()
                }
                "hitokoto" -> return HitokotoUpdater.getHitokoto().convertToChain()
                "switch" -> {
                    BotVariables.switch = !BotVariables.switch
                    return "维护模式已${if (!BotVariables.switch) "开启" else "关闭"}".sendMessage()
                }
                "push" -> {
                    if (args.size > 1) {
                        return when (args[1].toLowerCase()) {
                            "twit", "twitter", "推特", "蓝鸟", "twi" -> {
                                TweetUpdateChecker.retrieve()
                                sendMessage("Twitter retriever has been triggered and run~")
                            }
                            "ytb", "y2b", "youtube", "油管" -> {
                                YoutubeStreamingChecker.retrieve()
                                sendMessage("Youtube retriever has been triggered and run~")
                            }
                            "bilibili", "bili", "哔哩哔哩", "b站" -> {
                                BiliDynamicChecker.retrieve()
                                sendMessage("Bilibili retriever has been triggered and run~")
                            }
                            "status" -> {
                                val ps = listOf(TweetUpdateChecker, BiliDynamicChecker, BiliLiveChecker)
                                buildString {
                                    ps.forEach {
                                        append(it::class.java.simpleName + "\n")
                                        append("上次推送了 ${it.pushCount} 次\n")
                                        append("上次推送于 ${BotVariables.yyMMddPattern.format(it.lastPushTime)}\n")
                                    }
                                    trim()
                                }.sendMessage()
                            }
                            else -> sendMessage("Unknown retriever type.")
                        }
                    }
                }
                "youtube" -> {
                    if (args.size > 1) {
                        return YoutubeApi.getLiveStatusByResult(YoutubeApi.getChannelVideos(args[1], 10)).toMessageChain(event.subject)
                    }
                }
                "rss" -> {
                    if (args.size > 1) {
                        return RssUtil.simplifyHTML(
                                RssUtil.getFromEntry(
                                        RssUtil.getEntryFromURL(args[1])
                                                ?: return "Can't retrieve page content".convertToChain()
                                )
                        ).convertToChain()
                    }
                }
                "executors" -> {
                    val executor = BotVariables.service as ThreadPoolExecutor

                    val queueSize: Int = executor.queue.size
                    val activeCount: Int = executor.activeCount
                    val completedTaskCount: Long = executor.completedTaskCount
                    val taskCount: Long = executor.taskCount

                    return """
                        当前排队线程数：$queueSize
                        当前活动线程数：$activeCount
                        执行完成线程数： $completedTaskCount
                        总线程数：$taskCount
                    """.trimIndent().convertToChain()
                }
                "pushpool" -> {
                    return BiliDynamicChecker.getPool().toString().sendMessage()
                }
                "twpic" -> {
                    if (args.isEmpty()) return "/debug twpic [Tweet ID]".convertToChain()
                    else {
                        if (args[1].isNumeric()) {
                            val tweet = TwitterApi.getTweetById(args[1].toLong())
                            if (tweet != null) {
                                val screenshot = NetUtil.getScreenshot(tweet.getTweetURL())
                                        ?: return "Can't take screenshot, See console for more info :(".convertToChain()
                                return screenshot.uploadAsImage(event.subject).asMessageChain()
                            } else {
                                return "Can't found tweet which id is ${args[0]}.".convertToChain()
                            }
                        } else {
                            return "NaN".convertToChain()
                        }
                    }
                }
                "bilipic" -> {
                    if (args.isEmpty()) return "/debug bilipic [Dynamic ID]".convertToChain()
                    else {
                        if (args[1].isNumeric()) {
                            val dynamic = BiliBiliMainApi.getDynamicById(args[1].toLong())


                            if (!NetUtil.driverUsable()) {
                                return "The driver doesn't enabled or unusable!".convertToChain()
                            }

                            try {
                                val screenshot = NetUtil.getScreenshot(
                                        "https://t.bilibili.com/${dynamic.data.card?.description?.dynamicId}"
                                ) {
                                    val wait = WebDriverWait(this, 50, 1)

                                    // 等待动态加载完毕再截图
                                    wait.until(ExpectedCondition { webDriver ->
                                        try {
                                            webDriver?.findElement(By.className("content-full"))
                                        } catch (e: Exception) {
                                            daemonLogger.warning("获取网页元素时出现异常", e)
                                        }
                                    })

                                    // 执行脚本获取合适的动态宽度
                                    val jsExecutor = (this as JavascriptExecutor)
                                    val width = jsExecutor.executeScript(
                                            """return document.getElementsByClassName("main-content")[1].offsetWidth""") as Int
                                    val height =
                                            jsExecutor.executeScript(
                                                    """return document.getElementsByClassName("main-content")[1].offsetHeight""") as Int

                                    // 调整窗口大小
                                    manage().window().size = Dimension(width, height)
                                }
                                        ?: return "Can't take screenshot, See console for more info :(".convertToChain()
                                return screenshot.uploadAsImage(event.subject).asMessageChain()
                            } catch (e: Exception) {
                                if (e is ApiException) {
                                    return "Can't found bili dynamic which id is ${args[0]}.".convertToChain()
                                }

                                daemonLogger.warning("Can't retrieve bilibili dynamic", e)
                            }
                        } else {
                            return "NaN".convertToChain()
                        }
                    }
                }
                else -> return "Bot > 命令不存在\n${getHelp()}".convertToChain()
            }
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps {
        return CommandProps("debug", mutableListOf(), "Debug", "nbot.commands.debug", UserLevel.ADMIN)
    }

    override fun getHelp(): String = "调试命令会随时变动, 请自行查阅代码"

    override val isHidden: Boolean
        get() = true

    override val canRegister: () -> Boolean
        get() = { BotVariables.cfg.debugMode }
}