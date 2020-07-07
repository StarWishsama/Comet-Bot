package io.github.starwishsama.nbot.objects.pojo.bilibili.dynamic

import io.github.starwishsama.nbot.objects.TextPlusPicture

interface DynamicData {
    suspend fun getContact(): TextPlusPicture
}