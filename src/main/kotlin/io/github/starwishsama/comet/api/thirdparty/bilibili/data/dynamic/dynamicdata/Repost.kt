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
import io.github.starwishsama.comet.CometVariables.mapper
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.DynamicData
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.DynamicTypeSelector
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.user.UserProfile
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import io.github.starwishsama.comet.objects.wrapper.Picture
import io.github.starwishsama.comet.utils.FileUtil
import io.github.starwishsama.comet.utils.NumberUtil.toLocalDateTime
import java.time.LocalDateTime
import java.util.stream.Collectors

data class Repost(
    @JsonProperty("origin")
    var originDynamic: String,
    @JsonProperty("origin_extend_json")
    var originExtend: String?,
    @JsonProperty("origin_user")
    var originUser: UserProfile?,
    val item: ItemBean?,
    @JsonProperty("user")
    val profile: UserProfile.Info
) : DynamicData {
    data class ItemBean(
        @JsonProperty("content")
        val content: String,
        @JsonProperty("orig_dy_id")
        val originDynamicId: Long,
        @JsonProperty("pre_dy_id")
        val previousDynamicId: Long,
        @JsonProperty("timestamp")
        val sentTime: Long,
        @JsonProperty("orig_type")
        val originType: Int
    ) {
        fun getSentTime(): LocalDateTime = sentTime.toLocalDateTime()
    }

    override fun asMessageWrapper(): MessageWrapper {
        val originalDynamic = item?.originType?.let { getOriginalDynamic(originDynamic, it) }
            ?: return MessageWrapper().addText("\"源动态已被删除\"")
        val repostPicture =
            originalDynamic.getMessageContent().parallelStream().filter { it is Picture }.collect(Collectors.toList())
        val msg = MessageWrapper().addText(
            "转发了 ${if (item.content.isEmpty()) "源动态已被删除" else "${originUser?.info?.userName} 的动态:"} \n${item.content}"
                    + "\n\uD83D\uDD58 ${hmsPattern.format(item.getSentTime())}\n" + "原动态信息: \n${originalDynamic.getAllText()}"
        )

        if (repostPicture.isNotEmpty()) {
            repostPicture.forEach {
                if (!repostPicture.isNullOrEmpty()) {
                    msg.addElement(it)
                }
            }
        }

        return msg
    }

    override fun getSentTime(): LocalDateTime = item?.getSentTime() ?: LocalDateTime.MIN

    private fun getOriginalDynamic(contact: String, type: Int): MessageWrapper {
        val dynamicType = DynamicTypeSelector.getType(type)

        try {
            if (dynamicType != UnknownType::class.java) {
                val info = mapper.readValue(contact, dynamicType)
                if (info != null) {
                    return info.asMessageWrapper()
                }
            }
            return MessageWrapper().addText("无法解析此动态消息, 你还是另请高明吧")
        } catch (e: Exception) {
            FileUtil.createErrorReportFile(
                "在解析动态时出现了异常",
                "bilibili",
                e,
                contact,
                "Excepted type: $dynamicType"
            )
            return MessageWrapper().addText("在获取时遇到了错误")
        }
    }
}