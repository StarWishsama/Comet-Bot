package io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.dynamicdata

import com.google.gson.annotations.SerializedName
import io.github.starwishsama.comet.BotVariables.hmsPattern
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.DynamicData
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.user.UserProfile
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import io.github.starwishsama.comet.utils.NumberUtil.toLocalDateTime
import java.time.LocalDateTime

data class PlainText(
        val user: UserProfile.Info,
        val item: ItemBean
) : DynamicData {
    data class ItemBean(
        @SerializedName("rp_id")
        val rpId: Long,
        @SerializedName("uid")
        val senderUid: Long,
        @SerializedName("content")
        val context: String?,
        @SerializedName("orig_dy_id")
        val originalDynamicId: Long,
        @SerializedName("pre_dy_id")
        val previousDynamicId: Long,
        @SerializedName("timestamp")
        val sentTimestamp: Long,
        @SerializedName("reply")
        val replyCount: Int
    )

    override fun getContact(): MessageWrapper {
        return MessageWrapper(
            "发布了动态: \n" +
                    "${item.context ?: "获取失败"}\n\n" +
                    "🕘 ${hmsPattern.format(item.sentTimestamp.toLocalDateTime())}"
        )
    }

    override fun getSentTime(): LocalDateTime = item.sentTimestamp.toLocalDateTime()
}