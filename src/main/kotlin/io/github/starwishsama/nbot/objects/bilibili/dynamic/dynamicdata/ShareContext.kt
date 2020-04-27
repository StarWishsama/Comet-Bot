package io.github.starwishsama.nbot.objects.bilibili.dynamic.dynamicdata

import cn.hutool.http.HttpRequest
import io.github.starwishsama.nbot.objects.bilibili.dynamic.DynamicData
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.asMessageChain
import net.mamoe.mirai.message.data.toMessage
import net.mamoe.mirai.message.uploadAsImage

data class ShareContext(var vest: VestBean, var sketch: SketchBean)  : DynamicData {
    override suspend fun getMessageChain(contact: Contact): MessageChain {
        val text = "分享了 ${vest.context}".toMessage().asMessageChain()
        if (!sketch.cover_url.isNullOrEmpty()){
            val response = HttpRequest.get(sketch.cover_url).timeout(150_000).executeAsync().bodyStream()
            return text.plus(response.uploadAsImage(contact))
        }
        return text
    }

    data class VestBean(var uid: Long, var context: String)
    data class SketchBean(var title: String?, var desc_text: String?, var cover_url: String?, var target_url: String?)
}