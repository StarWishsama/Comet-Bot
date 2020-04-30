package io.github.starwishsama.nbot.objects.bilibili.dynamic.dynamicdata

import io.github.starwishsama.nbot.objects.bilibili.dynamic.DynamicData

data class ShareContext(var vest: VestBean, var sketch: SketchBean)  : DynamicData {
    override suspend fun getContact(): List<String> {
        val list = arrayListOf("分享了 ${vest.context}\n")
        if (!sketch.cover_url.isNullOrEmpty()){
            sketch.cover_url.let {
                if (it != null) {
                    list.add(it)
                }
            }
        }
        return list
    }

    data class VestBean(var uid: Long, var context: String)
    data class SketchBean(var title: String?, var desc_text: String?, var cover_url: String?, var target_url: String?)
}