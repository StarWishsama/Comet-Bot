package io.github.starwishsama.comet.objects.wrapper

import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import io.github.starwishsama.comet.utils.network.NetUtil
import kotlinx.coroutines.delay
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage

open class MessageWrapper(var text: String?, val success: Boolean = true): Cloneable {
    val pictureUrl: MutableList<String> = mutableListOf()

    private suspend fun getPictures(contact: Contact): List<Image> {
        val images = mutableListOf<Image>()

        pictureUrl.forEach { url ->
            NetUtil.getInputStream(url)?.use {
                val uploadedImage = it.uploadAsImage(contact)
                images.add(uploadedImage)
                delay(1000)
            }
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

    suspend fun toMessageChain(contact: Contact, pictureAtTop: Boolean = false): MessageChain {
        val textWrapper = text?.trim()
        if (textWrapper != null) {
            val images = getPictures(contact)
            if (images.isNotEmpty()) {
                var result = textWrapper.convertToChain()

                if (pictureAtTop) {
                    images.forEach {
                        result = it + result
                    }
                } else {
                    images.forEach {
                        result += it
                    }
                }

                return result
            }

            return textWrapper.convertToChain()
        }
        return EmptyMessageChain
    }

    override fun toString(): String {
        return "MessageWrapper {text=$text, pictureUrls=$pictureUrl}"
    }
}