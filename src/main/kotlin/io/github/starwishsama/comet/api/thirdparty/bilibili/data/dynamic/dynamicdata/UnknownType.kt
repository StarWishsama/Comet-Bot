package io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.dynamicdata

import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.DynamicData
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import java.time.LocalDateTime

class UnknownType : DynamicData {
    override suspend fun getContact(): MessageWrapper {
        return MessageWrapper("无法解析此动态消息, 你还是另请高明吧")
    }

    override fun getSentTime(): LocalDateTime = LocalDateTime.MIN
}