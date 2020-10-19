package io.github.starwishsama.comet.api.command.interfaces

import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.objects.BotUser
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.MessageChain

/**
 * 聊天命令接口
 * 支持QQ聊天任意环境下处理命令
 *
 * @author StarWishsama
 */
interface ChatCommand {
    /** 执行命令后的逻辑 */
    suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain

    /** 命令属性 */
    fun getProps(): CommandProps

    /** 命令帮助文本 必填 */
    fun getHelp(): String

    fun hasPermission(user: BotUser, e: MessageEvent): Boolean =
            user.compareLevel(getProps().level) || user.hasPermission(getProps().permission)


    val name: String
        get() = getProps().name

    val isHidden: Boolean
        get() = false

    val canRegister: () -> Boolean
        get() = { true }
}