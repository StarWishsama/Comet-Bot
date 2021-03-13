package io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.dynamicdata

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.DynamicData
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import io.github.starwishsama.comet.utils.NumberUtil.toLocalDateTime
import java.time.LocalDateTime

data class Music(
        @JsonProperty("id")
        val id: Long,
        @JsonProperty("upId")
        val upId: Long,
        @JsonProperty("title")
        val songName: String,
        @JsonProperty("upper")
        val uploader: String,
        @JsonProperty("cover")
        val coverURL: String?,
        @JsonProperty("author")
        val author: String,
        @JsonProperty("ctime")
        val uploadTime: Long,
        @JsonProperty("intro")
        val dynamic: String?,
        @JsonProperty("replyCnt")
        val replyCount: Long,
        @JsonProperty("playCnt")
        val playCount: Long
) : DynamicData {
    override fun getContact(): MessageWrapper {
            return MessageWrapper().addText("${dynamic ?: "获取失败"}\n" +
                    "发布了音乐: $songName\n" +
                    "🕘 ${BotVariables.yyMMddPattern.format(uploadTime.toLocalDateTime())}").apply {
                    if (coverURL != null) {
                            addPictureByURL(coverURL)
                    }
            }
    }

    override fun getSentTime(): LocalDateTime = uploadTime.toLocalDateTime()
}