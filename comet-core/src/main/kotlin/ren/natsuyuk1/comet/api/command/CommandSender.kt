/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 */

package ren.natsuyuk1.comet.api.command

import ren.natsuyuk1.comet.api.message.MessageWrapper

/**
 * [CommandSender]
 *
 *
 */
interface CommandSender {
    fun sendMessage(message: MessageWrapper)
}

/**
 * [PlatformCommandSender]
 *
 * 不同平台实现的 `sendMessage`
 * 代表来自不同平台的用户
 */
abstract class PlatformCommandSender : CommandSender {
    abstract override fun sendMessage(message: MessageWrapper)
}

/**
 * [ConsoleCommandSender]
 *
 * 代表来自终端的命令发送者
 */
class ConsoleCommandSender : CommandSender {
    override fun sendMessage(message: MessageWrapper) {
        TODO("Not yet implemented")
    }
}
