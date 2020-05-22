package io.github.starwishsama.nbot.objects.pojo.bilibili.dynamic

interface DynamicData {
    suspend fun getContact(): List<String>
}