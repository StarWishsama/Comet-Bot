package io.github.starwishsama.nbot.commands.interfaces

import io.github.starwishsama.nbot.commands.CommandProps

/**
 * 控制台命令接口
 * 支持控制台环境下处理命令
 *
 * @author StarWishsama
 */
interface ConsoleCommand : UniversalCommand {
    /** 执行命令后的逻辑 */
    suspend fun execute(args: List<String>): String

    /** 命令属性 */
    override fun getProps(): CommandProps

    /** 命令帮助文本 必写 不敢自己都看不懂哦 */
    override fun getHelp(): String
}