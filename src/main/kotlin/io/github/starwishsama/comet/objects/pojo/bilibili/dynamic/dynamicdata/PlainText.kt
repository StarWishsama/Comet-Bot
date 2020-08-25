package io.github.starwishsama.comet.objects.pojo.bilibili.dynamic.dynamicdata

import io.github.starwishsama.comet.objects.MessageWrapper
import io.github.starwishsama.comet.objects.pojo.bilibili.dynamic.DynamicData
import io.github.starwishsama.comet.objects.pojo.bilibili.user.UserProfile

data class PlainText(var item: ItemBean,
                     var user: UserProfile.Info) : DynamicData {
    data class ItemBean(var context: String)

    override suspend fun getContact(): MessageWrapper {
        return MessageWrapper("发布了动态: \n${item.context}\n")
    }
}