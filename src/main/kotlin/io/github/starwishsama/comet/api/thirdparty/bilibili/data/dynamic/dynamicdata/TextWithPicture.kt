package io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.dynamicdata

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.DynamicData
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.user.UserProfile
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import io.github.starwishsama.comet.utils.NumberUtil.toLocalDateTime
import java.time.LocalDateTime

data class TextWithPicture(
        val item: Item,
        val user: User,
) : DynamicData {
    data class User(
            val uid: Long,
            @SerializedName("head_url")
            val headUrl: String,
            @SerializedName("name")
            val name: String,
            @SerializedName("vip")
            val vipInfo: UserProfile.VipInfo
    )

    data class Item(
            @SerializedName("id")
            val id: Long,
            @SerializedName("title")
            val title: String?,
            @SerializedName("description")
            val text: String?,
            @SerializedName("category")
            val category: String,
            @SerializedName("role")
            val role: JsonElement,
            @SerializedName("sources")
            val sources: JsonElement,
            @SerializedName("pictures")
            val pictures: List<Picture>,
            @SerializedName("pictures_count")
            val pictureCount: Int,
            @SerializedName("upload_time")
            val uploadTime: Long,
            @SerializedName("at_control")
            val atControl: String,
            @SerializedName("reply")
            val replyCount: Long,
            @SerializedName("settings")
            val settings: JsonObject,
            @SerializedName("is_fav")
            val isFavorite: Int,
    ) {
        data class Picture(
                @SerializedName("img_src")
                var imgUrl: String,
                @SerializedName("img_width")
                val imgWidth: Int,
                @SerializedName("img_height")
                val imgHeight: Int,
                @SerializedName("img_size")
                val imgSize: Int
        )
    }

    override suspend fun getContact(): MessageWrapper {
        val wrapped = MessageWrapper("发布了动态:\n ${item.text ?: "获取失败"}\n")

        if (!item.pictures.isNullOrEmpty()) {
            item.pictures.forEach {
                wrapped.plusImageUrl(it.imgUrl)
            }
        }

        return wrapped
    }

    override fun getSentTime(): LocalDateTime = item.uploadTime.toLocalDateTime()
}