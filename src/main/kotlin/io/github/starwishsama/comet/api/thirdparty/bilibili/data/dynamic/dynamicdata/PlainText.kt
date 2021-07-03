/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.dynamicdata

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.starwishsama.comet.CometVariables.hmsPattern
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.DynamicData
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.user.UserProfile
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import io.github.starwishsama.comet.utils.NumberUtil.toLocalDateTime
import java.time.LocalDateTime

data class PlainText(
    val user: UserProfile.Info,
    val item: ItemBean
) : DynamicData {
    data class ItemBean(
        @JsonProperty("rp_id")
        val rpId: Long,
        @JsonProperty("uid")
        val senderUid: Long,
        @JsonProperty("content")
        val context: String?,
        @JsonProperty("orig_dy_id")
        val originalDynamicId: Long,
        @JsonProperty("pre_dy_id")
        val previousDynamicId: Long,
        @JsonProperty("timestamp")
        val sentTimestamp: Long,
        @JsonProperty("reply")
        val replyCount: Int
    )

    override fun getContact(): MessageWrapper {
        return MessageWrapper().addText(
            "发布了动态: \n" +
                    "${item.context ?: "获取失败"}\n\n" +
                    "🕘 ${hmsPattern.format(item.sentTimestamp.toLocalDateTime())}"
        )
    }

    override fun getSentTime(): LocalDateTime = item.sentTimestamp.toLocalDateTime()
}