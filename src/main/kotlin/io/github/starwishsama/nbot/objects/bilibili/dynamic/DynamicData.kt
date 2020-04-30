package io.github.starwishsama.nbot.objects.bilibili.dynamic

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.MessageChain

interface DynamicData {
    suspend fun getContact(): List<String>
}