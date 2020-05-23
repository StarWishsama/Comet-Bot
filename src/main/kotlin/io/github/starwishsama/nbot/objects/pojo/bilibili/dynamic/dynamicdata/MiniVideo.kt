package io.github.starwishsama.nbot.objects.pojo.bilibili.dynamic.dynamicdata

import com.google.gson.annotations.SerializedName
import io.github.starwishsama.nbot.objects.WrappedMessage
import io.github.starwishsama.nbot.objects.pojo.bilibili.dynamic.DynamicData
import io.github.starwishsama.nbot.objects.pojo.bilibili.user.UserProfile

class MiniVideo : DynamicData {
    var item: Item? = null
    var user: AuthorProfile? = null

    class AuthorProfile : UserProfile.Info() {
        @SerializedName("name")
        override var userName: String = ""

        @SerializedName("head_url")
        override var avatarImgURL: String = ""

    }

    class Item {
        var id: Long = 0
        var description: String? = null
        var cover: Cover? = null

        class Cover {
            @SerializedName("default")
            var defaultImgURL: String? = null

            @SerializedName("unclipped")
            var originImgURL: String? = null
        }
    }

    override suspend fun getContact(): WrappedMessage {
        val wrapped = WrappedMessage("发了一个小视频: ${item?.description}\n")

        item?.cover?.originImgURL.let {
            if (it != null) {
                wrapped.picture = it
            }
        }

        return wrapped
    }
}