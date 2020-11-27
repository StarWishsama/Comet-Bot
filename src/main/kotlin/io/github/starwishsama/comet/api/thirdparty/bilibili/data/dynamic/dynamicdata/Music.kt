package io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.dynamicdata

import com.google.gson.annotations.SerializedName
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.DynamicData
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import io.github.starwishsama.comet.utils.NumberUtil.toLocalDateTime
import java.time.LocalDateTime

data class Music(
        @SerializedName("id")
        val id: Long,
        @SerializedName("upId")
        val upId: Long,
        @SerializedName("title")
        val songName: String,
        @SerializedName("upper")
        val uploader: String,
        @SerializedName("cover")
        val coverURL: String?,
        @SerializedName("author")
        val author: String,
        @SerializedName("ctime")
        val uploadTime: Long,
        @SerializedName("intro")
        val dynamic: String?,
        @SerializedName("replyCnt")
        val replyCount: Long,
        @SerializedName("playCnt")
        val playCount: Long
) : DynamicData {
    override suspend fun getContact(): MessageWrapper {
        return MessageWrapper("${dynamic ?: "获取失败"}\n" +
                "发布了音乐: $songName\n" +
                "🕘 ${BotVariables.yyMMddPattern.format(uploadTime.toLocalDateTime())}")
                .plusImageUrl(coverURL)
    }

    override fun getSentTime(): LocalDateTime = uploadTime.toLocalDateTime()
}