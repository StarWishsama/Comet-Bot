/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package ren.natsuyuk1.comet.api.command.interfaces

import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.sessions.Session
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.MessageReceipt

/**
 * 用于标记不可被禁用的命令
 */
interface UnDisableableCommand

/**
 * 回调式命令
 *
 * 某些命令需要操作发送后的消息
 */
interface CallbackCommand {
    fun handleReceipt(receipt: MessageReceipt<Contact>)
}

/**
 * 交互式命令
 *
 * 支持接受输入内容并处理.
 *
 * 需要创建一个 [Session] 以触发监听
 */
interface ConversationCommand {
    suspend fun handle(event: MessageEvent, user: CometUser, session: Session)
}