package io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.dynamicdata

import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.DynamicData
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.user.UserProfile
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper

data class PlainText(var item: ItemBean,
                     var user: UserProfile.Info) : DynamicData {
    data class ItemBean(var context: String)

    override suspend fun getContact(): MessageWrapper {
        return MessageWrapper("发布了动态: \n${item.context}\n")
    }
}