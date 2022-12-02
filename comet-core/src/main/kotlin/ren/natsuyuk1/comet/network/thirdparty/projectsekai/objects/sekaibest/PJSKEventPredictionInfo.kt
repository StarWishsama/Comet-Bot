/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 MIT 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the MIT License which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/dev/LICENSE
 */

package ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.sekaibest

import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.message.buildMessageWrapper
import ren.natsuyuk1.comet.utils.datetime.format
import ren.natsuyuk1.comet.utils.math.NumberUtil.getBetterNumber
import ren.natsuyuk1.comet.utils.string.StringUtil.isNumeric
import ren.natsuyuk1.comet.utils.time.yyMMddWithTimePattern

@kotlinx.serialization.Serializable
data class PJSKEventPredictionInfo(
    val status: String,
    val data: JsonObject,
    val message: String
)

fun PJSKEventPredictionInfo.toMessageWrapper(updateTime: Instant? = null): MessageWrapper =
    buildMessageWrapper {
        val eventName = data["eventName"]?.jsonPrimitive?.content

        appendText("活动 $eventName PT预测\n")

        data.forEach { k, v ->
            if (k.isNumeric()) appendTextln("$k => ${v.jsonPrimitive.content.toLong().getBetterNumber()}")
        }

        appendText("上次更新于 ${updateTime?.format(yyMMddWithTimePattern)}")
    }
