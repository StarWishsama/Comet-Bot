package io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.dynamicdata

import com.google.gson.annotations.SerializedName
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.DynamicData
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper

class MiniVideo : DynamicData {
    var item: Item? = null
    var user: AuthorProfile? = null

    data class AuthorProfile(
            val uid: Int,
            @SerializedName("name")
            val userName: String?,
            @SerializedName("head_url")
            val avatarImgURL: String?
    )

    data class Item(
            var id: Long,
            var description: String?,
            var cover: Cover?,
            @SerializedName("timestamp")
            val sentTimestamp: Long
    ) {
        data class Cover(
                @SerializedName("default")
                val defaultImgURL: String?,
                @SerializedName("unclipped")
                val originImgURL: String?
        )
    }

    override suspend fun getContact(): MessageWrapper {
        val wrapped = MessageWrapper("发了一个小视频: ${item?.description ?: "获取失败"}\n")

        item?.cover?.originImgURL.let {
            if (it != null) {
                try {
                    wrapped.plusImageUrl(it)
                } catch (e: UnsupportedOperationException) {
                    return@let
                }
            }
        }

        return wrapped
    }
}