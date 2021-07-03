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
import io.github.starwishsama.comet.objects.CometUser
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain

/**
 * 聊天命令接口
 * 支持QQ聊天任意环境下处理命令
 *
 * @author StarWishsama
 */
interface ChatCommand {
    /** 执行命令后的逻辑 */
    suspend fun execute(event: MessageEvent, args: List<String>, user: CometUser): MessageChain

    /** 命令属性 */
    fun getProps(): CommandProps

    /** 命令帮助文本 必填 */
    fun getHelp(): String

    /** 判断用户是否有权限使用该命令, 有必要时可以重载 */
    fun hasPermission(user: CometUser, e: MessageEvent): Boolean =
        user.compareLevel(getProps().level) || user.hasPermission(getProps().permission)

    val name: String
        get() = getProps().name

    val isHidden: Boolean
        get() = false

    val canRegister: () -> Boolean
        get() = { true }
}