package io.github.starwishsama.comet.api.bilibili.data.dynamic

import io.github.starwishsama.comet.objects.MessageWrapper

interface DynamicData {
    suspend fun getContact(): MessageWrapper
}