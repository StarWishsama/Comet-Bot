/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package ren.natsuyuk1.comet.objects.minecraft

import com.fasterxml.jackson.module.kotlin.readValue
import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper

data class QueryInfo(
    val rawResponse: String,
    val queryType: QueryType,
    val usedTime: Long
) {
    private fun parseServerInfo(): MinecraftServerInfo {
        return when (queryType) {
            QueryType.JAVA -> CometVariables.mapper.readValue<MinecraftJavaInfo>(rawResponse)
            QueryType.BEDROCK -> MinecraftBedrockInfo.convert(rawResponse)
        }
    }

    fun convertToWrapper(): MessageWrapper {
        if (rawResponse.isEmpty()) {
            return MessageWrapper()
        }

        return parseServerInfo().getStatus(usedTime)
    }
}