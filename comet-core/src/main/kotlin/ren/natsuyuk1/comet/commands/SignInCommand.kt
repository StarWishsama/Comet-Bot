/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 */

package ren.natsuyuk1.comet.commands

import ren.natsuyuk1.comet.api.command.CometCommand
import ren.natsuyuk1.comet.api.command.CommandProperty
import ren.natsuyuk1.comet.api.command.PlatformCommandSender
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.utils.message.MessageWrapper

private val property by lazy {
    CommandProperty(
        "signin",
        listOf("qd", "签到", "sign"),
        "进行签到",
        """
        /sign 进行签到
        """.trimIndent()
    )
}

class SignInCommand(sender: PlatformCommandSender, raw: String, message: MessageWrapper, user: CometUser) :
    CometCommand(sender, raw, message, user, property) {

    override suspend fun run() {
        TODO("Not yet implemented")
    }
}
