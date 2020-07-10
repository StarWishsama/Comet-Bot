package io.github.starwishsama.comet.objects.pojo.bilibili.dynamic

import io.github.starwishsama.comet.objects.WrappedMessage

interface DynamicData {
    suspend fun getContact(): WrappedMessage
}