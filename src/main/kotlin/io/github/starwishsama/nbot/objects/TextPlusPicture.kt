package io.github.starwishsama.nbot.objects

import io.github.starwishsama.nbot.utils.NetUtil
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.uploadAsImage

data class TextPlusPicture(var text: String?) {
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