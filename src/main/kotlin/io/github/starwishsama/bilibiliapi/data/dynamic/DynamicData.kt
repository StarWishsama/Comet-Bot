package io.github.starwishsama.bilibiliapi.data.dynamic

import io.github.starwishsama.comet.objects.MessageWrapper

interface DynamicData {
    suspend fun getContact(): MessageWrapper
}