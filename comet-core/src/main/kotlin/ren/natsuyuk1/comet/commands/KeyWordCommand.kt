package ren.natsuyuk1.comet.commands

import moe.sdl.yac.parameters.options.flag
import moe.sdl.yac.parameters.options.option
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.command.CometCommand
import ren.natsuyuk1.comet.api.command.CommandProperty
import ren.natsuyuk1.comet.api.command.PlatformCommandSender
import ren.natsuyuk1.comet.api.command.asGroup
import ren.natsuyuk1.comet.api.session.Session
import ren.natsuyuk1.comet.api.session.expire
import ren.natsuyuk1.comet.api.session.register
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.objects.keyword.KeyWordData
import ren.natsuyuk1.comet.util.toMessageWrapper
import ren.natsuyuk1.comet.utils.message.MessageWrapper
import ren.natsuyuk1.comet.utils.message.buildMessageWrapper
import ren.natsuyuk1.comet.utils.string.StringUtil.limit

val KEYWORD = CommandProperty(
    "keyword",
    listOf("kw", "关键词"),
    "关键词回复",
    "/keyword 关键词回复"
)

class KeyWordAddSession(
    contact: PlatformCommandSender,
    val subject: PlatformCommandSender,
    user: CometUser,
    val keyword: String,
    private val regex: Boolean,
) : Session(contact, user) {
    override fun handle(message: MessageWrapper) {
        KeyWordData.addKeyWord(subject.id, subject.platform, KeyWordData.Data.KeyWord(keyword, message, regex))
        subject.sendMessage("成功添加该关键字 ($keyword)".toMessageWrapper())
        expire()
    }
}

class KeyWordCommand(
    comet: Comet,
    override val sender: PlatformCommandSender,
    override val subject: PlatformCommandSender,
    message: MessageWrapper,
    override val user: CometUser
) : CometCommand(comet, sender, subject, message, user, KEYWORD) {
    private val add by option("--add", "-a", help = "新增关键词")
    private val remove by option("--remove", "-rm", help = "删除关键词")
    private val list by option("--list", "-l", help = "查看本群已有的关键词")
    private val regex by option("--regex", "-r", help = "是否为正则表达式").flag()

    override suspend fun run() {
        if (subject.asGroup() == null) {
            subject.sendMessage("该命令只能在群聊中使用!".toMessageWrapper())
            return
        }


        when {
            add != null -> {
                if (KeyWordData.exists(subject.id, subject.platform, add!!)) {
                    subject.sendMessage("这个关键词已经添加过了, 如果需要修改请先删除.".toMessageWrapper())
                } else {
                    subject.sendMessage("接下来, 请发送该关键词应回复的内容".toMessageWrapper())
                    KeyWordAddSession(sender, subject, user, add!!, regex).register()
                }
            }

            remove != null -> {
                if (KeyWordData.exists(subject.id, subject.platform, remove!!)) {
                    subject.sendMessage("该群聊还未添加过此关键词.".toMessageWrapper())
                } else {
                    KeyWordData.removeKeyWord(subject.id, subject.platform, remove!!)
                    subject.sendMessage("删除关键词成功".toMessageWrapper())
                }
            }

            list != null -> {
                val keywords = KeyWordData.find(subject.id, subject.platform)

                if (keywords == null) {
                    subject.sendMessage("该群聊还没有添加过关键词哦".toMessageWrapper())
                    return
                }

                subject.sendMessage(buildMessageWrapper {
                    appendText("已添加的关键词 >>", true)

                    appendText(buildString {
                        keywords.words.forEach {
                            append("${it.pattern} => ${it.reply.parseToString().limit(20)}")
                            appendLine()
                        }
                    }.trim())
                })
            }
        }
    }
}
