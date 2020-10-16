package io.github.starwishsama.comet.api.command.interfaces

import io.github.starwishsama.comet.api.command.CommandProps

/**
 * 控制台命令接口
 * 支持控制台环境下处理命令
 *
 * @author StarWishsama
 */
interface ConsoleCommand {
    /** 执行命令后的逻辑 */
    suspend fun execute(args: List<String>): String

    /** 命令属性 */
    fun getProps(): CommandProps
}