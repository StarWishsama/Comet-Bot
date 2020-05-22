package io.github.starwishsama.nbot.objects.pojo.bilibili.dynamic.dynamicdata

import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import io.github.starwishsama.nbot.objects.pojo.bilibili.dynamic.DynamicTypeSelector

import io.github.starwishsama.nbot.objects.pojo.bilibili.dynamic.DynamicData
import io.github.starwishsama.nbot.objects.pojo.bilibili.user.UserProfile

data class Repost(@SerializedName("origin")
                  var originDynamic: String,
                  @SerializedName("origin_extend_json")
                  var originExtend: String?,
                  @SerializedName("origin_user")
                  var originUser: UserProfile?,
                  var item: ItemBean?,
                  @SerializedName("user")
                  val profile: UserProfile.Info?) : DynamicData {
    data class ItemBean(@SerializedName("content")
                        val content: String,
                        @SerializedName("miss")
                        val deleted: Int,
                        @SerializedName("tips")
                        val tips: String?,
                        @SerializedName("orig_type")
                        val originType: Int?) {
        fun isDeleted(): Boolean {
            return deleted == 1
        }
    }

    override suspend fun getContact(): List<String> {
        return arrayListOf(("转发了 ${if (item?.isDeleted()!!) "源动态已被删除" else "${originUser?.info?.userName} 的动态:"} \n${item?.content}\n" +
                "原动态信息: ${item?.originType?.let { getOriginalDynamic(originDynamic, it) }}"))
    }

    private suspend fun getOriginalDynamic(contact: String, type: Int): String {
        try {
            val dynamicType = DynamicTypeSelector.getType(type)
            if (dynamicType.typeName != UnknownType::javaClass.name) {
                val gson = GsonBuilder().serializeNulls().setPrettyPrinting().create()
                val info = gson.fromJson(contact, dynamicType)
                if (info != null && !info.getContact().isNullOrEmpty()) {
                    return info.getContact()[0]
                }
            }
            return "无法解析此动态消息, 你还是另请高明吧"
        } catch (e: Exception) {
            println("在处理时遇到了问题\n原动态内容: $contact\n动态类型: $type\n报错堆栈")
            e.printStackTrace()
        }
        return "在获取时遇到了错误"
    }
}