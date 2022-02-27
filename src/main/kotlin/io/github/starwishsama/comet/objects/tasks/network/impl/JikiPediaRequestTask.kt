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

import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.api.thirdparty.jikipedia.JikiPediaApi
import io.github.starwishsama.comet.api.thirdparty.jikipedia.JikiPediaSearchResult
import io.github.starwishsama.comet.objects.tasks.network.INetworkRequestTask
import io.github.starwishsama.comet.objects.tasks.network.NetworkRequestTask
import io.github.starwishsama.comet.utils.CometUtil.toMessageChain
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.Contact
import java.io.IOException

class JikiPediaRequestTask(override val content: Contact, override val param: String) : NetworkRequestTask(),
    INetworkRequestTask<JikiPediaSearchResult> {
    override fun request(param: String): JikiPediaSearchResult {
        return try {
            JikiPediaApi.searchByKeyWord(param)
        } catch (e: IOException) {
            JikiPediaSearchResult.empty(403)
        }
    }

    override fun callback(result: Any?) {
        runBlocking {
            if (result is JikiPediaSearchResult) {
                content.sendMessage(result.toMessageWrapper().toMessageChain(content))
            }
        }
    }

    override fun onFailure(t: Throwable?) {
        runBlocking { content.sendMessage("在搜索时遇到了异常".toMessageChain()) }
        CometVariables.daemonLogger.warning("在搜索小鸡百科时遇到了异常", t)
    }
}