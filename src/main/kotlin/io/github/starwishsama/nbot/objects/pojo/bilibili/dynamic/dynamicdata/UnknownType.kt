package io.github.starwishsama.nbot.objects.pojo.bilibili.dynamic.dynamicdata

import io.github.starwishsama.nbot.objects.TextPlusPicture
import io.github.starwishsama.nbot.objects.pojo.bilibili.dynamic.DynamicData

class UnknownType : DynamicData {
    override suspend fun getContact(): TextPlusPicture {
        return TextPlusPicture("无法解析此动态消息, 你还是另请高明吧")
    }
}