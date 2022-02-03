package io.github.starwishsama.comet.commands.chats

import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.objects.enums.UserLevel
import io.github.starwishsama.comet.service.command.PlantService
import io.github.starwishsama.comet.utils.CometUtil.toChain

import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain

object PlantCommand: ChatCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: CometUser): MessageChain {
        if (args.isEmpty()) {
            return getHelp().toChain()
        }

        return when (args[0]) {
            "enter" -> PlantService.handlePlant(event, args, user)
            "info", "cx" -> PlantService.queryPlantStatus(user)
            "rename", "mm" -> PlantService.renamePlant(user)
            "claim", "lq" -> PlantService.claimPlant(user)
            else -> getHelp().toChain()
        }
    }

    override val props: CommandProps
        get() = CommandProps("plant", listOf("绿植", "lz", "pt"), "绿植系统", UserLevel.USER)

    override fun getHelp(): String =
        """
        /plant claim(lq) 领取一棵绿植
        /plant info(cx) 查询绿植状态  
        /plant rename(mm) 命名绿植
        /plant enter 进入种植模式 
        """.trimIndent()
}