package io.github.starwishsama.comet.objects

import io.github.starwishsama.comet.utils.NetUtil
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.uploadAsImage

data class WrappedMessage(var text: String?) {
    var picture: String? = null
    suspend fun getPicture(contact: Contact): Image? {
        picture.let {
            if (it != null) {
                return NetUtil.getUrlInputStream(it).uploadAsImage(contact)
            }
        }
        return null
    }
}