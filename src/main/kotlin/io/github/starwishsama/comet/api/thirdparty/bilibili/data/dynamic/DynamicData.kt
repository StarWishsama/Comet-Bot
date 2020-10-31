package io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic

import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.dynamicdata.*
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper

interface DynamicData {
    suspend fun getContact(): MessageWrapper

    suspend fun compare(other: Any?): Boolean {
        if (other == null) return false
        if (other !is DynamicData) return false

        return getContact().text == other.getContact().text
    }
}

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