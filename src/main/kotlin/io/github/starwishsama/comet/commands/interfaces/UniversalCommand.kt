package io.github.starwishsama.comet.commands.interfaces

import io.github.starwishsama.comet.commands.CommandProps
import io.github.starwishsama.comet.objects.BotUser
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.MessageChain

/**
 * 通用命令接口
 * 支持QQ聊天任意环境下处理命令
 *
 * @author StarWishsama
 */
interface UniversalCommand {
    /** 执行命令后的逻辑 */
    suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain
    /** 命令属性 */
    fun getProps(): CommandProps
    /** 命令帮助文本 必写 不敢自己都看不懂哦 */
    fun getHelp() : String
}