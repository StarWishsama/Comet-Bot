package io.github.starwishsama.nbot.commands.subcommands

import io.github.starwishsama.nbot.commands.CommandProps
import io.github.starwishsama.nbot.commands.interfaces.UniversalCommand
import io.github.starwishsama.nbot.enums.UserLevel
import io.github.starwishsama.nbot.objects.BotUser
import net.mamoe.mirai.message.ContactMessage
import net.mamoe.mirai.message.data.MessageChain

class ReminderCommand : UniversalCommand {
    override suspend fun execute(message: ContactMessage, args: List<String>, user: BotUser): MessageChain {
        TODO("Not yet implemented")
    }

    override fun getProps(): CommandProps = CommandProps("reminder", arrayListOf(), "nbot.commands.reminder", UserLevel.USER)

    override fun getHelp(): String = """
        /reminder 查看待办事项
        /reminder add [事项]
        /reminder rm [事项编号]
        /reminder [事项] [提醒时间]
    """.trimIndent()

}