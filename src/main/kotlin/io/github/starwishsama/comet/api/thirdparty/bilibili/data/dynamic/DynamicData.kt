package io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.dynamicdata.*
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.user.UserProfile
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper

interface DynamicData {
    suspend fun getContact(): MessageWrapper

    suspend fun compare(other: Any?): Boolean {
        if (other == null) return false
        if (other !is DynamicData) return false

        return getContact().text == other.getContact().text
    }
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
            else -> UnknownType::class.java
        }
    }
}

data class Dynamic(
    @SerializedName("desc")
    val description: DynamicDesc,
    @SerializedName("card")
    val card: String,
    @SerializedName("extend_json")
    val extendJson: String,
    @SerializedName("extra")
    val extraJson: JsonObject?,
    @SerializedName("display")
    val displayJson: DynamicDisplay
) {
    data class DynamicDesc(
        val uid: Int,
        val type: Int,
        val rid: Long,
        val acl: Int,
        @SerializedName("view")
        val viewCount: Int,
        @SerializedName("repost")
        val repostCount: Int,
        @SerializedName("like")
        val likeCount: Int,
        @SerializedName("is_liked")
        val liveStatus: Int,
        @SerializedName("dynamic_id")
        val dynamicId: Long,
        @SerializedName("timestamp")
        val timeStamp: Long,

        /** 转发的动态ID, 无转发为0 */
        @SerializedName("pre_dy_id")
        val repostDynamicId: Long,

        /** 转发的起始动态ID, 无转发为0 */
        @SerializedName("orig_dy_id")
        val originalDynamicId: Long,

        @SerializedName("orig_type")
        val originalType: Int,

        @SerializedName("user_profile")
        val userProfile: UserProfile,

        @SerializedName("uid_type")
        val uidType: Int,

        @SerializedName("status")
        val dynamicStatus: Int,

        @SerializedName("dynamic_id_str")
        val dynamicIdAsString: String,

        @SerializedName("pre_dy_id_str")
        val previousDynamicIdAsString: String,

        @SerializedName("orig_dy_id_str")
        val originalDynamicIdAsString: String,

        @SerializedName("rid_str")
        val ridAsString: String,

        val origin: JsonObject
    )

    data class DynamicDisplay(
        val origin: JsonObject?,
        val relation: JsonObject?,
        @SerializedName("comment_info")
        val hotComment: HotComments?
    ) {
        data class HotComments(
            val comments: HotComment
        ) {
            data class HotComment(
                val uid: Int,
                @SerializedName("name")
                val name: String,
                val content: String
            )
        }
    }
}

data class DynamicCard(
    /** 发送者信息 */
    val user: DynamicUser,
    /** 动态信息 */
    val item: DynamicItem,
    /** 视频 AV 号 */
    val aid: Int,
    val attribute: Int?,
    val cid: Int?,

    // 以下仅限视频类动态可以获取

    /* 视频是否受版权保护 */
    @SerializedName("copyright")
    val copyRight: Int?,
    /** 视频上传时间戳 */
    @SerializedName("ctime")
    val uploadTime: Long?,
    /** 视频描述 */
    @SerializedName("desc")
    val videoDesc: String?,

    val dimension: JsonObject?,

    val duration: Int?,

    @SerializedName("dynamic")
    val tags: String?,

    @SerializedName("jump_url")
    val jumpUrl: String?,

    val owner: JsonObject,

    /** 视频封面 */
    @SerializedName("pic")
    val videoPic: String?,

    /** 视频标题 */
    val videoTitle: String?
) {
    data class DynamicUser(
        /** 用户 UID */
        val uid: Int,

        /** 用户名 */
        @SerializedName("uname")
        val userName: String,

        @SerializedName("face")
        val avatarImg: String
    )

    data class DynamicItem(
        @SerializedName("rp_id")
        val rpId: Int?,

        @SerializedName("uid")
        val uid: Int,

        /** 动态内容 */
        val content: String,

        /** 转发的起始动态ID, 无转发为0 */
        @SerializedName("orig_dy_id")
        val originalDynamicId: Long,

        /** 转发的动态ID, 无转发为0 */
        @SerializedName("pre_dy_id")
        val repostDynamicId: Long,

        /** 发送动态的时间戳 */
        @SerializedName("timestamp")
        val sentTime: Long,

        /** 动态回复数 */
        @SerializedName("reply")
        val replyCount: Int
    )
}