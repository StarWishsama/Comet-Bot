package io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.dynamicdata

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.DynamicData
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import io.github.starwishsama.comet.objects.wrapper.buildMessageWrapper

// From Yabapi @ https://github.com/SDLMoe/Yabapi
data class Bangumi(
    @JsonProperty("aid") val aid: Int? = null,
    @JsonProperty("apiSeasonInfo") val apiSeasonInfo: Season? = null,
    @JsonProperty("bullet_count") val bulletCount: Int? = null,
    @JsonProperty("cover") val cover: String? = null,
    @JsonProperty("episode_id") val episodeId: Int? = null,
    @JsonProperty("index") val index: String? = null,
    @JsonProperty("index_title") val indexTitle: String? = null,
    @JsonProperty("new_desc") val newDesc: String? = null,
    @JsonProperty("online_finish") val onlineFinish: Int? = null,
    @JsonProperty("play_count") val playCount: Int? = null,
    @JsonProperty("reply_count") val replyCount: Int? = null,
    @JsonProperty("url") val url: String? = null,
): DynamicData() {
    data class Season(
        @JsonProperty("bgm_type") val type: BangumiType = BangumiType.UNKNOWN,
        @JsonProperty("cover") val cover: String? = null,
        @JsonProperty("is_finish") val isFinish: Boolean? = null,
        @JsonProperty("season_id") val seasonId: Int? = null,
        @JsonProperty("title") val title: String? = null,
        @JsonProperty("total_count") val totalCount: Int? = null,
        @JsonProperty("ts") val timestamp: Long? = null,
        @JsonProperty("type_name") val typeName: String? = null,
    )

    enum class BangumiType {
        UNKNOWN,
        @JsonProperty("1")
        ANIME, // 番剧
        @JsonProperty("2")
        MOVIE, // 电影
        @JsonProperty("3")
        DOCUMENTARY, // 纪录片
        @JsonProperty("4")
        GUOCHUANG, // 国创
        @JsonProperty("5")
        SERIES, // 电视剧
        @JsonProperty("7")
        VARIETY, // 综艺
    }

    override fun asMessageWrapper(): MessageWrapper {
        return buildMessageWrapper {
            addText("分享了番剧 > ${apiSeasonInfo?.title}\n")
            addText("${newDesc}\n")
            addText("🔗 $url")
        }
    }
}
