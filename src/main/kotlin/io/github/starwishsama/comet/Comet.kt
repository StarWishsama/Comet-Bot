/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet

import net.mamoe.mirai.Bot

class Comet {
    private lateinit var bot: Bot

    fun getBot() = bot

    fun setBot(bot: Bot) {
        this.bot = bot
    }

    fun isInitialized(): Boolean {
        return ::bot.isInitialized
    }
}