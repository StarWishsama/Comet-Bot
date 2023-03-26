/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 MIT 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the MIT License which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/dev/LICENSE
 */

package ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.kit33

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.message.buildMessageWrapper
import ren.natsuyuk1.comet.utils.math.NumberUtil.getBetterNumber
import ren.natsuyuk1.comet.utils.math.NumberUtil.toInstant
import ren.natsuyuk1.comet.utils.string.StringUtil.isNumeric

@Serializable
data class PJSKEventPredictionInfo(
    val status: String,
    val data: JsonObject,
    val rank: JsonObject,
    val message: String,
    val event: PredictEventInfo,
) {
    @Serializable
    data class PredictEventInfo(
        val id: Int,
        val name: String,
        val startAt: Long,
        val aggregateAt: Long,
    )
}

fun PJSKEventPredictionInfo.toMessageWrapper(isFinal: Boolean = false): MessageWrapper =
    buildMessageWrapper {
        val timestamp = data.jsonObject["ts"]?.jsonPrimitive?.longOrNull

        appendText("活动 ${event.name} PT预测\n")

        if (isFinal) {
            rank.forEach { k, v ->
                if (k.isNumeric()) appendTextln("$k => ${v.jsonPrimitive.content.toLong().getBetterNumber()}")
            }
        } else {
            data.forEach { k, v ->
                if (k.isNumeric()) appendTextln("$k => ${v.jsonPrimitive.content.toLong().getBetterNumber()}")
            }
        }

        appendTextln("由于服务器限制，预测误差极大，请谨慎参考!")
        appendLine()
        appendTextln("数据来源于 33Kit")
        appendText("上次更新于 ${timestamp?.toInstant(true)}")
    }
