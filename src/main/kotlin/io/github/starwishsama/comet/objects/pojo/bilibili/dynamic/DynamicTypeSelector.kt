package io.github.starwishsama.comet.objects.pojo.bilibili.dynamic

import io.github.starwishsama.comet.objects.pojo.bilibili.dynamic.dynamicdata.*

object DynamicTypeSelector {
    fun getType(type: Int): Class<out DynamicData> {
        return when (type) {
            1 -> Repost::class.java
            2 -> TextWithPicture::class.java
            4 -> PlainText::class.java
            8 -> Video::class.java
            16 -> MiniVideo::class.java
            64 -> Article::class.java
            256 -> Music::class.java
            2048 -> ShareContext::class.java
            4200 -> LiveRoom::class.java
            else -> UnknownType::class.java
        }
    }
}