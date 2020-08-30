package io.github.starwishsama.bilibiliapi.data.dynamic.dynamicdata

import io.github.starwishsama.bilibiliapi.data.dynamic.DynamicData
import io.github.starwishsama.comet.objects.MessageWrapper

class UnknownType : DynamicData {
    override suspend fun getContact(): MessageWrapper {
        return MessageWrapper("无法解析此动态消息, 你还是另请高明吧")
    }
}