package io.github.starwishsama.nbot.objects.bilibili.dynamic.dynamicdata

import io.github.starwishsama.nbot.objects.bilibili.dynamic.DynamicData
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.asMessageChain
import net.mamoe.mirai.message.data.toMessage

class UnknownType : DynamicData {
    override suspend fun getContact(): List<String> {
        return arrayListOf("无法解析此动态消息, 你还是另请高明吧")
    }
}