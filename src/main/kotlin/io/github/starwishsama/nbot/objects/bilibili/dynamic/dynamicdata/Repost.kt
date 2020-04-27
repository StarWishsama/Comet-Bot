package io.github.starwishsama.nbot.objects.bilibili.dynamic.dynamicdata

import com.google.gson.annotations.SerializedName

import io.github.starwishsama.nbot.objects.bilibili.dynamic.DynamicData
import io.github.starwishsama.nbot.objects.bilibili.user.UserProfile
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.asMessageChain
import net.mamoe.mirai.message.data.toMessage

data class Repost(@SerializedName("origin")
                  var originDynamic: String?,
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
                        val tips: String?) {
        fun isDeleted(): Boolean {
            return deleted == 1
        }
    }

    override suspend fun getMessageChain(contact: Contact): MessageChain {
        return ("转发了 ${if (item?.isDeleted()!!) "源动态已被删除" else "${originUser?.info?.userName} 的动态:"} \n${item?.content}\n" +
                "原动态信息: $originDynamic").toMessage().asMessageChain()
    }
}