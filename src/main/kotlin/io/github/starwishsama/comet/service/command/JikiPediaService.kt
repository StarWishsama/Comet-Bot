/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.service.command

import io.github.starwishsama.comet.api.thirdparty.jikipedia.JikiPediaApi
import io.github.starwishsama.comet.utils.CometUtil.toChain
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain

object JikiPediaService {
    fun searchJiki(event: MessageEvent, input: String): MessageChain {
        runBlocking { event.subject.sendMessage("请稍后...") }

        val result = JikiPediaApi.search(input)

        val wrapper = result.toMessageWrapper()

        return if (!wrapper.isUsable()) {
            "使用次数已达上限, 一会儿再试吧".toChain()
        } else if (wrapper.isEmpty()) {
            "找不到对应结果".toChain()
        } else {
            result.toMessageWrapper().toMessageChain(event.subject)
        }
    }
}