/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.objects.tasks.network.impl

import io.github.starwishsama.comet.api.thirdparty.jikipedia.JikiPediaApi
import io.github.starwishsama.comet.api.thirdparty.jikipedia.JikiPediaSearchResult
import io.github.starwishsama.comet.objects.tasks.network.INetworkRequestTask
import io.github.starwishsama.comet.objects.tasks.network.NetworkRequestTask
import io.github.starwishsama.comet.utils.CometUtil.toChain
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.Contact

class JikiPediaRequestTask(override val content: Contact, override val param: String) : NetworkRequestTask(),
    INetworkRequestTask<JikiPediaSearchResult> {
    override fun request(param: String): JikiPediaSearchResult {
        return JikiPediaApi.search(param)
    }

    override fun callback(result: Any?) {
        runBlocking {
            if (result is JikiPediaSearchResult) {
                val wrapper = result.toMessageWrapper()

                val chain = if (!wrapper.isUsable()) {
                    "使用次数已达上限, 一会儿再试吧".toChain()
                } else if (wrapper.isEmpty()) {
                    "找不到对应结果".toChain()
                } else {
                    result.toMessageWrapper().toMessageChain(content)
                }

                content.sendMessage(chain)
            }
        }
    }
}