package io.github.starwishsama.comet.objects.wrapper

import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import io.github.starwishsama.comet.utils.network.NetUtil
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.uploadAsImage

open class MessageWrapper(var text: String?, val success: Boolean = true) {
    val pictureUrl: MutableList<String> = mutableListOf()
    var senderId: Long = 0
    var messageId: Long = 0

    private suspend fun getPictures(contact: Contact): List<Image> {
        val images = mutableListOf<Image>()

        pictureUrl.forEach {
            val uploadedImage = NetUtil.getHttpRequestStream(it)?.uploadAsImage(contact)
            if (uploadedImage != null) images.add(uploadedImage)
        }

        return images
    }

    @Throws(UnsupportedOperationException::class)
    fun plusImageUrl(url: String?): MessageWrapper {
        if (url == null) return this

        if (pictureUrl.size <= 9 && !pictureUrl.contains(url)) {
            pictureUrl.add(url)
            return this
        } else {
            throw UnsupportedOperationException("出于防刷屏考虑, 最多只能添加九张照片")
        }
    }

    suspend fun toMessageChain(contact: Contact): MessageChain {
        val textWrapper = text
        if (textWrapper != null) {
            val images = getPictures(contact)
            if (images.isNotEmpty()) {
                var result = textWrapper.convertToChain()

                images.forEach {
                    result += it
                }
                return result
            }

            return textWrapper.trim().convertToChain()
        }
        return EmptyMessageChain
    }

    override fun toString(): String {
        return "MessageWrapper {text=$text, pictureUrls=$pictureUrl, senderId=$senderId, messageId=$messageId}"
    }
}