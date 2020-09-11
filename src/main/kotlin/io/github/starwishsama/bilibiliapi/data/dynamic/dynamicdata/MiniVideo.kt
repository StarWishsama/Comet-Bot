package io.github.starwishsama.bilibiliapi.data.dynamic.dynamicdata

import com.google.gson.annotations.SerializedName
import io.github.starwishsama.bilibiliapi.data.dynamic.DynamicData
import io.github.starwishsama.bilibiliapi.data.user.UserProfile
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper

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

    override suspend fun getContact(): MessageWrapper {
        val wrapped = MessageWrapper("发了一个小视频: ${item?.description}\n")

        item?.cover?.originImgURL.let {
            if (it != null) {
                wrapped.picUrl = it
            }
        }

        return wrapped
    }
}