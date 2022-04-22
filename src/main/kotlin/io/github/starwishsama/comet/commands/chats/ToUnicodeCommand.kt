package io.github.starwishsama.comet.commands.chats

import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.objects.enums.UserLevel
import io.github.starwishsama.comet.utils.CometUtil.toMessageChain
import io.github.starwishsama.comet.utils.StringUtil.toHex
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain

object ToUnicodeCommand : ChatCommand {
    private const val HEX_LINE_SIZE = 16

    override suspend fun execute(event: MessageEvent, args: List<String>, user: CometUser): MessageChain {
        val content = args.firstOrNull() ?: return "未输入要转换的内容".toMessageChain()
        if (content.length >= 30) return "输入字符过多".toMessageChain()
        return "转换结果:\n${content.toHex(HEX_LINE_SIZE, padding = true).uppercase()}".toMessageChain()
    }

    override val props: CommandProps =
        CommandProps(
            "tounicode",
            aliases = arrayListOf("touni"),
            "将输入内容转换为 Unicode Hex",
            UserLevel.USER
        )

    override fun getHelp(): String = """
        /tounicode <字符串> 查看 Unicode Hex
    """.trimIndent()
}
