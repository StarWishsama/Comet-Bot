package io.github.starwishsama.comet.objects.wrapper

import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import io.github.starwishsama.comet.utils.network.NetUtil
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.uploadAsImage

open class MessageWrapper(var text: String?) {
    var picUrl: String? = null
    var senderId: Long = 0
    var messageId: Long = 0

    private suspend fun getPicture(contact: Contact): Image? {
        if (picUrl != null) {
            return picUrl?.let { NetUtil.getUrlInputStream(it)?.uploadAsImage(contact) }
        }
        return null
    }

    fun plusImageUrl(url: String?): MessageWrapper {
        this.picUrl = url
        return this
    }

    suspend fun toMessageChain(contact: Contact): MessageChain {
        val textWrapper = text
        if (textWrapper != null) {
            val image = getPicture(contact)
            if (image != null) {
                return textWrapper.convertToChain() + image
            }
            return textWrapper.convertToChain()
        }
        return EmptyMessageChain
    }

    override fun toString(): String {
        return "MessageWrapper {text=$text, pictureUrl=$picUrl}, senderId=${senderId}, messageId=${messageId}"
    }
}