package io.github.starwishsama.nbot.objects.bilibili.dynamic.dynamicdata

import io.github.starwishsama.nbot.objects.bilibili.dynamic.DynamicData
import io.github.starwishsama.nbot.objects.bilibili.user.UserProfile
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.asMessageChain
import net.mamoe.mirai.message.data.toMessage

data class PlainText(var item: ItemBean,
                     var user: UserProfile.Info) : DynamicData {
    data class ItemBean (var context: String)

    override suspend fun getMessageChain(contact: Contact): MessageChain = "发布了动态: \n${item.context}".toMessage().asMessageChain()
}