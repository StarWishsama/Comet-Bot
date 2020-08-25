package io.github.starwishsama.comet.objects.pojo.bilibili.dynamic

import io.github.starwishsama.comet.objects.MessageWrapper

interface DynamicData {
    suspend fun getContact(): MessageWrapper
}