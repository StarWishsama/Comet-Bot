package io.github.starwishsama.nbot.objects.bilibili.dynamic.dynamicdata

import cn.hutool.http.HttpRequest
import com.google.gson.annotations.SerializedName
import io.github.starwishsama.nbot.objects.bilibili.dynamic.DynamicData
import io.github.starwishsama.nbot.objects.bilibili.user.UserProfile
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.asMessageChain
import net.mamoe.mirai.message.data.toMessage
import net.mamoe.mirai.message.uploadAsImage

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

    override suspend fun getMessageChain(contact: Contact): MessageChain {
        val text = "发了一个小视频: ${item?.description}".toMessage().asMessageChain()
        if (item?.cover?.originImgURL != null){
            val response = HttpRequest.get(item?.cover?.originImgURL).timeout(150_000).executeAsync().bodyStream()
            return text.plus(response.uploadAsImage(contact))
        }
        return text
    }
}