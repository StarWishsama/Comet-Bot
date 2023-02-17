package ren.natsuyuk1.comet.commands

import moe.sdl.yac.parameters.options.flag
import moe.sdl.yac.parameters.options.option
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.command.CometCommand
import ren.natsuyuk1.comet.api.command.CommandProperty
import ren.natsuyuk1.comet.api.command.PlatformCommandSender
import ren.natsuyuk1.comet.api.command.asGroup
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.message.Text
import ren.natsuyuk1.comet.api.message.buildMessageWrapper
import ren.natsuyuk1.comet.api.session.Session
import ren.natsuyuk1.comet.api.session.expire
import ren.natsuyuk1.comet.api.session.register
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.api.user.UserLevel
import ren.natsuyuk1.comet.objects.keyword.KeyWordData
import ren.natsuyuk1.comet.util.groupAdminChecker
import ren.natsuyuk1.comet.util.toMessageWrapper
import ren.natsuyuk1.comet.utils.string.StringUtil.limit

val KEYWORD = CommandProperty(
    "keyword",
    listOf("kw", "关键词"),
    "关键词回复",
    "/keyword 关键词回复",
    permissionLevel = UserLevel.ADMIN,
    extraPermissionChecker = groupAdminChecker,
)

class KeyWordAddSession(
    contact: PlatformCommandSender,
    val subject: PlatformCommandSender,
    user: CometUser,
    val keyword: String,
    private val regex: Boolean,
) : Session(contact, user) {
    override suspend fun process(message: MessageWrapper) {
        KeyWordData.addKeyWord(subject.id, subject.platform, KeyWordData.GroupInstance.KeyWord(keyword, message, regex))
        subject.sendMessage("成功添加该关键字 ($keyword)".toMessageWrapper())
        expire()
    }
}

class KeyWordCommand(
    comet: Comet,
    override val sender: PlatformCommandSender,
    override val subject: PlatformCommandSender,
    val message: MessageWrapper,
    val user: CometUser,
) : CometCommand(comet, sender, subject, message, user, KEYWORD) {
    private val add by option("--add", "-a", help = "新增关键词")
    private val remove by option("--remove", "-rm", help = "删除关键词")
    private val list by option("--list", "-l", help = "查看本群已有的关键词").flag(default = true)
    private val regex by option("--regex", "-r", help = "是否为正则表达式").flag()

    override suspend fun run() {
        if (subject.asGroup() == null) {
            subject.sendMessage("该命令只能在群聊中使用!".toMessageWrapper())
            return
        }

        when {
            add != null -> {
                if (message.getMessageContent().any { it !is Text }) {
                    subject.sendMessage("关键词只可以是文字!".toMessageWrapper())
                    return
                }

                if (KeyWordData.exists(subject.id, subject.platform, add!!)) {
                    subject.sendMessage("这个关键词已经添加过了, 如果需要修改请先删除.".toMessageWrapper())
                } else {
                    KeyWordAddSession(sender, subject, user, add!!, regex).register()
                    subject.sendMessage("接下来, 请发送该关键词应回复的内容".toMessageWrapper())
                }
            }

            remove != null -> {
                if (!KeyWordData.exists(subject.id, subject.platform, remove!!)) {
                    subject.sendMessage("该群聊还未添加过此关键词.".toMessageWrapper())
                } else {
                    KeyWordData.removeKeyWord(subject.id, subject.platform, remove!!)
                    subject.sendMessage("删除关键词成功".toMessageWrapper())
                }
            }

            list -> {
                val keywords = KeyWordData.find(subject.id, subject.platform)

                if (keywords == null) {
                    subject.sendMessage("该群聊还没有添加过关键词哦".toMessageWrapper())
                    return
                }

                subject.sendMessage(
                    buildMessageWrapper {
                        appendTextln("已添加的关键词 >>")

                        appendText(
                            buildString {
                                keywords.words.forEach {
                                    append("${it.pattern} => ${it.reply.encodeToString().limit(20)}")
                                    appendLine()
                                }
                            }.trim(),
                        )
                    },
                )
            }

            else -> {
                subject.sendMessage(
                    """
                用法: keyword [选项]

                /keyword 关键词回复

                选项:
                 -a, --add 文本      新增关键词
                 -rm, --remove 文本  删除关键词
                 -l, --list 文本     查看本群已有的关键词
                 -r, --regex       是否为正则表达式
                 -h, --help        显示帮助信息
                    """.trimIndent().toMessageWrapper(),
                )
            }
        }
    }
}
