package io.github.starwishsama.comet.commands.chats

import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import io.github.starwishsama.comet.objects.wrapper.XmlElement
import io.github.starwishsama.comet.utils.CometUtil.toChain
import io.github.starwishsama.comet.utils.StringUtil.isNumeric
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.MessageChain

class NoteCommand: ChatCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        return if (args.isEmpty()) {
            getHelp().toChain()
        } else {
            when (args[0]) {
                "list", "列表", "所有", "all" -> listNotes(user, event.subject)
                "display", "展示" -> displayNote(user, args[1], event.subject)
                "remove", "sc", "rm", "删除" -> handleRemove(user, args[1])
                else -> getHelp().toChain()
            }
        }
    }

    override fun getProps(): CommandProps =
        CommandProps(
            "note",
            listOf("保存消息", "bcxx", "备忘", "bw"),
            "管理你的已保存信息",
            "nbot.commands.note",
            UserLevel.USER
        )

    override fun getHelp(): String =
        """
        /note list 展示所有保存消息
        /note display [序号] 展示指定消息
        /note remove [序号/all] 删除指定消息或全部消息   
        """.trimIndent()

    private fun listNotes(user: BotUser, subject: Contact): MessageChain {
        val notes = user.savedContents

        if (notes.isEmpty()) {
            return "你还没有保存过信息呢, 试着引用回复你要保存的消息并@我保存吧~".toChain()
        }

        val display = MessageWrapper().apply {
            for (index in notes.indices) {
                val indexNote = notes[index]
                addText("| ${index}:\n")
                indexNote.getMessageContent().forEach {
                    if (it is XmlElement) {
                        addText("卡片消息只能单独查看")
                    } else {
                        addElement(it)
                    }
                }
                addText("\n")
            }
        }

        return display.toMessageChain(subject)
    }

    private fun handleRemove(user: BotUser, index: String): MessageChain {
        val notes = user.savedContents

        if (notes.isEmpty()) {
            return "你还没有保存过信息呢, 试着引用回复你要保存的消息并@我保存吧~".toChain()
        }


        if (index.isNumeric() || index == "all") {
            if (index == "all") {
                notes.clear()
                return "已清空所有已保存信息!".toChain()
            } else {
                val location = index.toIntOrNull() ?: return "请输入有效数字!".toChain()
                if (location >= notes.size) return "找不到你要删除的信息!".toChain()

                notes.removeAt(location)
                return "删除指定消息成功!".toChain()
            }
        } else {
            return getHelp().toChain()
        }
    }

    private fun displayNote(user: BotUser, index: String, subject: Contact): MessageChain {
        val notes = user.savedContents

        if (notes.isEmpty()) {
            return "你还没有保存过信息呢, 试着引用回复你要保存的消息并@我保存吧~".toChain()
        }

        if (index.isNumeric() || index == "all") {
            val location = index.toIntOrNull() ?: return "请输入有效数字!".toChain()
            if (location > notes.size) return "找不到你要删除的信息!".toChain()
            val note = notes[location]

            runBlocking { subject.sendMessage(At(user.id) + "以下是你要查看的消息:") }
            return note.toMessageChain(subject)
        } else {
            return getHelp().toChain()
        }
    }
}