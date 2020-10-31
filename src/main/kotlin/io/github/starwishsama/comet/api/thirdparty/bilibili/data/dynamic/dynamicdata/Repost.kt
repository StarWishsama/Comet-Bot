package io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.dynamicdata

import com.google.gson.annotations.SerializedName
import io.github.starwishsama.comet.BotVariables.gson
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.DynamicData
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.DynamicTypeSelector
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.user.UserProfile
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import io.github.starwishsama.comet.utils.FileUtil

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

    override suspend fun getContact(): MessageWrapper {
        val originalDynamic = item?.originType?.let { getOriginalDynamic(originDynamic, it) }
                ?: return MessageWrapper("源动态已被删除")
        val repostPicture = originalDynamic.pictureUrl
        val msg = MessageWrapper("转发了 ${if (item == null || item?.isDeleted()!!) "源动态已被删除" else "${originUser?.info?.userName} 的动态:"} \n${item?.content}\n" +
                "原动态信息: \n${originalDynamic.text}")

        if (repostPicture.isNotEmpty()) {
            repostPicture.forEach {
                if (!repostPicture.isNullOrEmpty()) {
                    try {
                        msg.plusImageUrl(it)
                    } catch (e: UnsupportedOperationException) {
                        return@forEach
                    }
                }
            }
        }

        return msg
    }

    private suspend fun getOriginalDynamic(contact: String, type: Int): MessageWrapper {
        try {
            val dynamicType = DynamicTypeSelector.getType(type)
            if (dynamicType != UnknownType::class.java) {
                val info = gson.fromJson(contact, dynamicType)
                if (info != null) {
                    return info.getContact()
                }
            }
            return MessageWrapper("无法解析此动态消息, 你还是另请高明吧")
        } catch (e: Exception) {
            FileUtil.createErrorReportFile(
                    "在解析动态时出现了异常",
                    "bilibili",
                    e,
                    contact,
                    "None"
            )
            return MessageWrapper("在获取时遇到了错误")
        }
    }
}