package io.github.starwishsama.bilibiliapi.data.dynamic

import io.github.starwishsama.comet.objects.wrapper.MessageWrapper

interface DynamicData {
    suspend fun getContact(): MessageWrapper
}