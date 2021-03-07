package io.github.starwishsama.comet.commands.chats

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.api.annotations.CometCommand
import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.api.command.interfaces.SuspendCommand
import io.github.starwishsama.comet.enums.PicSearchApiType
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.sessions.Session
import io.github.starwishsama.comet.sessions.SessionManager
import io.github.starwishsama.comet.utils.CometUtil.toChain
import io.github.starwishsama.comet.utils.network.PictureSearchUtil
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.utils.MiraiExperimentalApi
import java.util.*

@CometCommand
class PictureSearchCommand : ChatCommand, SuspendCommand {

    @OptIn(MiraiExperimentalApi::class)
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (args.isEmpty()) {
            if (!SessionManager.isValidSessionById(event.sender.id)) {
                SessionManager.addSession(Session(this, user.id))
            }

            val img = event.message[Image] ?: return toChain("请发送需要搜索的图片")

            return handlePicSearch(img.queryUrl()).toChain()
        } else if (args[0].contentEquals("source") && args.size > 1) {
            return try {
                val api = PicSearchApiType.valueOf(args[1].toUpperCase(Locale.ROOT))
                BotVariables.cfg.pictureSearchApi = api
                toChain("已切换识图 API 为 ${api.name}", true)
            } catch (e: Throwable) {
                var type = ""
                PicSearchApiType.values().forEach {
                    type = type + it.name + " " + it.desc + "\n"
                }
                toChain("该识图 API 不存在, 可用的 API 类型:\n ${type.trim()}", true)
            }
        } else {
            return getHelp().toChain()
        }
    }

    override fun getProps(): CommandProps = CommandProps(
        "ps",
        arrayListOf("ytst", "st", "搜图", "以图搜图"),
        "以图搜图",
        "nbot.commands.picturesearch",
        UserLevel.USER
    )

    override fun getHelp(): String = """
        ======= 命令帮助 =======
        /ytst 以图搜图
        /ytst source [API名称] 修改搜图源
    """.trimIndent()

    @OptIn(MiraiExperimentalApi::class)
    override fun handleInput(event: MessageEvent, user: BotUser, session: Session) {
        SessionManager.expireSession(session)
        val image = event.message[Image]
        runBlocking {
            if (image != null) {
                event.subject.sendMessage("请稍等...")
                event.subject.sendMessage(handlePicSearch(image.queryUrl()))
            } else {
                event.subject.sendMessage("请发送图片!")
            }
        }
    }

    private fun handlePicSearch(url: String): String {
        when (BotVariables.cfg.pictureSearchApi) {
            PicSearchApiType.SAUCENAO -> {
                val result = PictureSearchUtil.sauceNaoSearch(url)
                return when {
                    result.similarity >= 52.5 -> {
                        "相似度:${result.similarity}%\n原图链接:${result.originalUrl}\n"
                    }
                    result.similarity == -1.0 -> {
                        "在识图时发生了问题, 请联系管理员"
                    }
                    else -> {
                        "相似度过低 (${result.similarity}%), 请尝试更换图片重试"
                    }
                }
            }
            PicSearchApiType.ASCII2D -> {
                val result = PictureSearchUtil.ascii2dSearch(url)
                return if (result.isNotEmpty()) {
                    "已找到可能相似的图片\n图片来源${result.originalUrl}\n打开 ascii2d 页面查看更多\n${result.openUrl}"
                } else {
                    "找不到相似的图片"
                }
            }
            PicSearchApiType.BAIDU -> {
                return ("点击下方链接查看\n" +
                        "https://graph.baidu.com/details?isfromtusoupc=1&tn=pc&carousel=0&promotion_name=pc_image_shituindex&extUiData%5bisLogoShow%5d=1&image=${url}")
            }
        }
    }
}