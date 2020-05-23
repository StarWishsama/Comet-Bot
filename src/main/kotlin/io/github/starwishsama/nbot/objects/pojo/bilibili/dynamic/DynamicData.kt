package io.github.starwishsama.nbot.objects.pojo.bilibili.dynamic

import io.github.starwishsama.nbot.objects.WrappedMessage

interface DynamicData {
    suspend fun getContact(): WrappedMessage
}