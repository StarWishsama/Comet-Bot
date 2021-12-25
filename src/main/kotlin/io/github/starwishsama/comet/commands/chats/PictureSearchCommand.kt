/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.commands.chats

import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.api.command.interfaces.ConversationCommand
import io.github.starwishsama.comet.managers.ApiManager
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.objects.config.api.SauceNaoConfig
import io.github.starwishsama.comet.objects.enums.PicSearchApiType
import io.github.starwishsama.comet.objects.enums.UserLevel
import io.github.starwishsama.comet.sessions.Session
import io.github.starwishsama.comet.sessions.SessionHandler
import io.github.starwishsama.comet.sessions.SessionTarget
import io.github.starwishsama.comet.utils.CometUtil.toChain
import io.github.starwishsama.comet.utils.network.PictureSearchUtil
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.utils.MiraiExperimentalApi


object PictureSearchCommand : ChatCommand, ConversationCommand {
    @OptIn(MiraiExperimentalApi::class)
    override suspend fun execute(event: MessageEvent, args: List<String>, user: CometUser): MessageChain {
        if (args.isEmpty()) {
            val imageToSearch = event.message[Image]

            if (imageToSearch == null) {
                if (!SessionHandler.hasSessionByID(event.sender.id, this::class.java)) {
                    SessionHandler.insertSession(Session(SessionTarget(privateId = event.sender.id), this, true))
                }

                return toChain("请发送需要搜索的图片")
            }

            return handlePicSearch(imageToSearch.queryUrl()).toChain()
        } else if (args[0].contentEquals("source") && args.size > 1) {
            return try {
                val api = PicSearchApiType.valueOf(args[1].uppercase())
                CometVariables.cfg.pictureSearchApi = api
                toChain("已切换识图 API 为 ${api.name}", true)
            } catch (ignored: IllegalArgumentException) {
                toChain(
                    "该识图 API 不存在, 可用的 API 类型:\n ${
                        buildString {
                            PicSearchApiType.values().forEach {
                                append("${it.name} ${it.desc},")
                            }
                        }.removeSuffix(",")
                    }", true
                )
            }
        } else {
            return getHelp().toChain()
        }
    }

    override val props: CommandProps = CommandProps(
        "ps",
        arrayListOf("ytst", "st", "搜图", "以图搜图"),
        "以图搜图",

        UserLevel.USER
    )

    override fun getHelp(): String = """
        /ytst 以图搜图
        /ytst source [API名称] 修改搜图源
    """.trimIndent()

    @OptIn(MiraiExperimentalApi::class)
    override suspend fun handle(event: MessageEvent, user: CometUser, session: Session) {
        SessionHandler.removeSession(session)
        val image = event.message[Image]

        if (image != null) {
            event.subject.sendMessage("请稍等...")
            event.subject.sendMessage(handlePicSearch(image.queryUrl()))
        } else {
            event.subject.sendMessage("请发送图片! 输入 /ps 重新搜索.")
        }
    }

    private fun handlePicSearch(url: String): String {
        val defaultSimilarity = 52.5
        when (CometVariables.cfg.pictureSearchApi) {
            PicSearchApiType.SAUCENAO -> {
                if (ApiManager.getConfig<SauceNaoConfig>().token.isEmpty()) {
                    return "SauceNao 搜索未被正确配置, 请联系管理员."
                }

                val result = PictureSearchUtil.sauceNaoSearch(url)
                return when {
                    result.similarity >= defaultSimilarity -> {
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