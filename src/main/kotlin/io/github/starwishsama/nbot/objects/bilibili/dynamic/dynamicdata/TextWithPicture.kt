package io.github.starwishsama.nbot.objects.bilibili.dynamic.dynamicdata

import cn.hutool.http.HttpRequest
import com.google.gson.annotations.SerializedName
import io.github.starwishsama.nbot.objects.bilibili.dynamic.DynamicData
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.asMessageChain
import net.mamoe.mirai.message.data.toMessage
import net.mamoe.mirai.message.uploadAsImage

data class TextWithPicture(var item: ItemBean) : DynamicData {
    override suspend fun getMessageChain(contact: Contact): MessageChain {
        val text = "发布了动态:\n ${item.text}".toMessage().asMessageChain()

        if (!item.pictures.isNullOrEmpty()){
            val response = HttpRequest.get(item.pictures[0].imgUrl).timeout(150_000).executeAsync().bodyStream()
            return text.plus(response.uploadAsImage(contact))
        }

        return text
    }

    data class ItemBean(@SerializedName("description")
                        var text: String,
                        @SerializedName("pictures")
                        var pictures: List<Picture>){
        data class Picture(@SerializedName("img_src")
                           var imgUrl: String
        )
    }
}