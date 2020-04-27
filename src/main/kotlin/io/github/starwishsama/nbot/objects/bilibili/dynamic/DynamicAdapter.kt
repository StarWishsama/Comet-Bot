package io.github.starwishsama.nbot.objects.bilibili.dynamic

import io.github.starwishsama.nbot.objects.bilibili.dynamic.dynamicdata.*

object DynamicAdapter {
    fun getType(type: Int): Class<out DynamicData> {
        var dynamicType: Class<out DynamicData> = UnknownType::class.java
        when (type) {
            1 -> dynamicType = Repost::class.java
            2 -> dynamicType = TextWithPicture::class.java
            4 -> dynamicType = PlainText::class.java
            8 -> dynamicType = Video::class.java
            16 -> dynamicType = MiniVideo::class.java
            64 -> dynamicType = Article::class.java
            256 -> dynamicType = Music::class.java
            2048 -> dynamicType = ShareContext::class.java
            4200 -> dynamicType = LiveRoom::class.java
        }
        return dynamicType
    }
}