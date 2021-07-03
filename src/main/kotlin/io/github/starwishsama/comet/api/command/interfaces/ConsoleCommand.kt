/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

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

    fun getHelp(): String
}