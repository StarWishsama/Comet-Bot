/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.listeners.mirai

import io.github.starwishsama.comet.CometPlugin
import io.github.starwishsama.comet.CometVariables.comet
import io.github.starwishsama.comet.startup.CometRuntime
import net.mamoe.mirai.event.events.BotActiveEvent
import net.mamoe.mirai.event.globalEventChannel

object BotLoginListener {
    fun listen() {
        CometPlugin.globalEventChannel().subscribeAlways<BotActiveEvent> {
            if (comet.isInitialized()) {
                return@subscribeAlways
            } else {
                comet.setBot(this.bot)
                CometRuntime.setupBot(comet.getBot())
            }
        }
    }
}