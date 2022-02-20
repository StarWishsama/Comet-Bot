/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.objects.tasks.network.impl

import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.api.thirdparty.noabbr.NoAbbrApi
import io.github.starwishsama.comet.api.thirdparty.noabbr.data.AbbrSearchResponse
import io.github.starwishsama.comet.i18n.LocalizationManager
import io.github.starwishsama.comet.objects.tasks.network.INetworkRequestTask
import io.github.starwishsama.comet.objects.tasks.network.NetworkRequestTask
import io.github.starwishsama.comet.utils.CometUtil.toChain
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.Contact
import java.io.IOException

class NoAbbrRequestTask(override val content: Contact, override val param: String) : NetworkRequestTask(),
    INetworkRequestTask<AbbrSearchResponse> {
    override fun request(param: String): AbbrSearchResponse {
        return try {
            NoAbbrApi.guessMeaning(param)
        } catch (e: IOException) {
            AbbrSearchResponse.empty
        }
    }

    override fun callback(result: Any?) {
        runBlocking {
            if (result is AbbrSearchResponse && !result.isEmpty()) {
                content.sendMessage(result.toMessageWrapper().toMessageChain(content))
            } else {
                content.sendMessage(LocalizationManager.getLocalizationText("message.network-error").toChain())
            }
        }
    }

    override fun onFailure(t: Throwable?) {
        runBlocking { content.sendMessage("在搜索缩写时遇到了异常".toChain()) }
        CometVariables.daemonLogger.warning("在搜索缩写时遇到了异常", t)
    }
}