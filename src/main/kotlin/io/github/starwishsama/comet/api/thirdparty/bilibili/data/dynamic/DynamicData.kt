/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BotVariables.mapper
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.dynamicdata.*
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.user.UserProfile
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import kotlinx.coroutines.runBlocking
import okhttp3.internal.toLongOrDefault
import java.time.LocalDateTime

interface DynamicData {
    fun getContact(): MessageWrapper

    fun compare(other: Any?): Boolean {
        if (other == null) return false
        if (other !is DynamicData) return false

        return getContact().compare(other.getContact()) && getSentTime() == other.getSentTime()
    }

    fun getSentTime(): LocalDateTime
}

object DynamicTypeSelector {
    fun getType(type: Int): Class<out DynamicData> {
        return when (type) {
            1 -> Repost::class.java
            2 -> TextWithPicture::class.java
            4 -> PlainText::class.java
            8 -> Video::class.java
            16 -> MiniVideo::class.java
            64 -> Article::class.java
            256 -> Music::class.java
            2048 -> ShareContext::class.java
            4200 -> LiveRoom::class.java
            4308 -> LiveBroadcast::class.java
            else -> UnknownType::class.java
        }
    }
}

data class Dynamic(
    val code: Int,
    @JsonProperty("msg")
    val msg: String,
    @JsonProperty("message")
    val message: String,
    @JsonProperty("data")
    val data: Data
) {
    /**
     * 获取该动态响应体的 ID
     * 如果获取的为时间线动态, 则会尝试获取第一条的 ID
     */
    fun getDynamicID(): Long {
        return when {
            data.card != null -> {
                data.card.description.dynamicIdAsString.toLongOrDefault(-1)
            }
            data.cards != null -> {
                data.cards[0].description.dynamicIdAsString.toLongOrDefault(-1)
            }
            else -> {
                -1
            }
        }
    }

    data class Data(
        /** /space_history Only */
        @JsonProperty("has_more")
        val hasMoreInfo: Int?,
        /** 获取单个动态时可用 */
        @JsonProperty("card")
        val card: Card?,
        /** 获取动态列表时可用 /space_history Only */
        @JsonProperty("cards")
        val cards: List<Card>?,
    ) {
        @Suppress("unused")
        data class Attentions(
            @JsonProperty("uids")
            val uidList: List<Long>
        )
    }
}

data class Card(
    @JsonProperty("desc")
    val description: DynamicDesc,
    @JsonProperty("card")
    val card: String,
    //@JsonProperty("extend_json")
    //val extendJson: String,
    //@JsonProperty("extra")
    //val extraJson: JsonNode?,
    //@JsonProperty("display")
    //val displayJson: DynamicDisplay
) {
    data class DynamicDesc(
        val uid: Int,
        val type: Int,
        //val rid: Long,
        //val acl: Int,
        @JsonProperty("view")
        val viewCount: Int,
        @JsonProperty("repost")
        val repostCount: Int,
        @JsonProperty("like")
        val likeCount: Int,
        @JsonProperty("is_liked")
        val liveStatus: Int,
        @JsonProperty("dynamic_id")
        val dynamicId: Long,
        @JsonProperty("timestamp")
        val timeStamp: Long,

        /** 转发的动态ID, 无转发为0 */
        @JsonProperty("pre_dy_id")
        val repostDynamicId: Long,

        /** 转发的起始动态ID, 无转发为0 */
        @JsonProperty("orig_dy_id")
        val originalDynamicId: Long,

        @JsonProperty("orig_type")
        val originalType: Int,

        @JsonProperty("user_profile")
        val userProfile: UserProfile,

        //@JsonProperty("uid_type")
        //val uidType: Int,

        //@JsonProperty("status")
        //val dynamicStatus: Int,

        @JsonProperty("dynamic_id_str")
        val dynamicIdAsString: String,

        @JsonProperty("pre_dy_id_str")
        val previousDynamicIdAsString: String,

        @JsonProperty("orig_dy_id_str")
        val originalDynamicIdAsString: String,

        //@JsonProperty("rid_str")
        //val ridAsString: String,

        //val origin: JsonNode
    )

    @Suppress("unused")
    data class DynamicDisplay(
        val origin: JsonNode?,
        val relation: JsonNode?,
        @JsonProperty("comment_info")
        val hotComment: HotComments?
    ) {
        data class HotComments(
            val comments: HotComment
        ) {
            data class HotComment(
                val uid: Int,
                @JsonProperty("name")
                val name: String,
                val content: String
            )
        }
    }
}

fun Dynamic.convertToDynamicData(): DynamicData? {
    lateinit var card: Card
    val dynamicJson: String = when {
        data.cards != null -> {
            val cardIndex = data.cards[0]
            card = cardIndex
            cardIndex.card
        }
        data.card != null -> {
            card = data.card
            data.card.card
        }
        else -> {
            return null
        }
    }

    val singleDynamicObject = mapper.readTree(dynamicJson)
    if (!singleDynamicObject.isNull) {
        val dynamicType = DynamicTypeSelector.getType(card.description.type)
        if (dynamicType != UnknownType::class) {
            return BotVariables.mapper.readValue(card.card, dynamicType)
        }
    }

    return null
}

fun Dynamic.convertToWrapper(): MessageWrapper {
    return try {
        val data = convertToDynamicData()
        runBlocking { data?.getContact() ?: MessageWrapper().addText("错误: 不支持的动态类型").setUsable(false) }
    } catch (e: Exception) {
        if (e is ArrayIndexOutOfBoundsException) {
            MessageWrapper().addText("动态列表为空").setUsable(false)
        } else {
            MessageWrapper().addText("解析动态失败").setUsable(false)
        }
    }
}