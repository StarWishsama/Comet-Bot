package io.github.starwishsama.comet.objects.pojo.bilibili.dynamic.dynamicdata

import io.github.starwishsama.comet.objects.WrappedMessage
import io.github.starwishsama.comet.objects.pojo.bilibili.dynamic.DynamicData

class UnknownType : DynamicData {
    override suspend fun getContact(): WrappedMessage {
        return WrappedMessage("无法解析此动态消息, 你还是另请高明吧")
    }
}