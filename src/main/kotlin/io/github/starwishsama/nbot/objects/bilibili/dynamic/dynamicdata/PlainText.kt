package io.github.starwishsama.nbot.objects.bilibili.dynamic.dynamicdata

import io.github.starwishsama.nbot.objects.bilibili.dynamic.DynamicData
import io.github.starwishsama.nbot.objects.bilibili.user.UserProfile

data class PlainText(var item: ItemBean,
                     var user: UserProfile.Info) : DynamicData {
    data class ItemBean (var context: String)

    override suspend fun getContact(): List<String> {
        return arrayListOf("发布了动态: \n${item.context}\n")
    }
}