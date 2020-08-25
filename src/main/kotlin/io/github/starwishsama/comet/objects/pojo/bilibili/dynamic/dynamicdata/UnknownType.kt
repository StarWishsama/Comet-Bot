package io.github.starwishsama.comet.objects.pojo.bilibili.dynamic.dynamicdata

import io.github.starwishsama.comet.objects.MessageWrapper
import io.github.starwishsama.comet.objects.pojo.bilibili.dynamic.DynamicData

class UnknownType : DynamicData {
    override suspend fun getContact(): MessageWrapper {
        return MessageWrapper("无法解析此动态消息, 你还是另请高明吧")
    }
}