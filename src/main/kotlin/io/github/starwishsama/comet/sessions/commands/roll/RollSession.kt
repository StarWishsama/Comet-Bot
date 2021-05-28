/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.sessions.commands.roll

import io.github.starwishsama.comet.commands.chats.RollCommand
import io.github.starwishsama.comet.sessions.Session
import io.github.starwishsama.comet.sessions.SessionTarget

class RollSession(
    override var target: SessionTarget,
    val rollItem: String,
    val stopAfterMinute: Int,
    val keyWord: String,
    val rollStarter: Long,
    val count: Int,
) : Session(target, RollCommand(), silent = true)