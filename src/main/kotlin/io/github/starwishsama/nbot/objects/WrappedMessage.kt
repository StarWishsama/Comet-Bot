package io.github.starwishsama.nbot.objects

import io.github.starwishsama.nbot.utils.BotUtil
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.uploadAsImage

data class WrappedMessage(var text: String?) {
    var picture: String? = null
    suspend fun getPicture(contact: Contact) : Image? {
        picture.let {
            if (it != null) {
                return BotUtil.getImageStream(it).uploadAsImage(contact)
            }
        }
        return null
    }
}