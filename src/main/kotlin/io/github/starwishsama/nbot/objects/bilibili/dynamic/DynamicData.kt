package io.github.starwishsama.nbot.objects.bilibili.dynamic

interface DynamicData {
    suspend fun getContact(): List<String>
}