/*
 * Copyright (c) 2019-2022 StarWishsama.
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
import io.github.starwishsama.comet.api.thirdparty.bilibili.DynamicApi
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.Card
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.DynamicData
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.user.UserProfile
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import io.github.starwishsama.comet.utils.NumberUtil.toLocalDateTime

data class PlainText(
    val user: UserProfile.Info,
    val item: ItemBean
) : DynamicData() {
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

    override fun asMessageWrapper(): MessageWrapper {
        val cache = DynamicApi.getDynamicByData(this)
        val context = item.context
        val card = cache?.data?.findFirstCard()

        return if (cache != null && context != null && card != null) {
            val wrapper = MessageWrapper().addText("${user.userName} 发布了动态: \n")

            parseBiliBiliEmoji(context, wrapper, card)

            wrapper.addText("\n🕘 ${hmsPattern.format(item.sentTimestamp.toLocalDateTime())}")

            wrapper
        } else {
            MessageWrapper().addText(
                "${user.userName} 发布了动态: \n" +
                        "${context ?: "获取失败"}\n\n" +
                        "🕘 ${hmsPattern.format(item.sentTimestamp.toLocalDateTime())}"
            )
        }
    }

    /**
     * 解析哔哩哔哩表情
     *
     * @param context 动态内容
     * @param wrapper [MessageWrapper]
     * @param card 动态卡片
     *
     */
    private fun parseBiliBiliEmoji(context: String, wrapper: MessageWrapper, card: Card) {
        var cacheString = context

        if (card.display.has("emoji_info")
            && !card.display["emoji_info"].isNull
            && card.display["emoji_info"].has("emoji_details")
            && !card.display["emoji_info"]["emoji_details"].isNull
        ) {
            card.display["emoji_info"]["emoji_details"].forEach {
                val displayName = it["emoji_name"].asText()
                val emojiImage = it["url"].asText()

                cacheString.split(displayName).also { list ->
                    list.forEach { s ->
                        wrapper.addText(s)
                        if (list.last() != s) {
                            wrapper.addPictureByURL(emojiImage)
                        }
                    }
                }

                cacheString = cacheString.replace("[$displayName]".toRegex(), "")
            }
        }
    }
}