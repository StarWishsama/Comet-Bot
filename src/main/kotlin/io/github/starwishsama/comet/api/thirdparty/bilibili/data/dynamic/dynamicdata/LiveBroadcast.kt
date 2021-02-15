package io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.dynamicdata

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.api.thirdparty.bilibili.DynamicApi
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.DynamicData
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import io.github.starwishsama.comet.objects.wrapper.Picture
import io.github.starwishsama.comet.utils.NumberUtil.toLocalDateTime
import java.time.LocalDateTime

data class LiveBroadcast(
        @SerializedName("live_play_info")
        val livePlayInfo: LivePlayInfo,
        /** 似乎是B站的直播留档信息, 没见过有这功能的主播 */
        @SerializedName("live_record_info")
        val liveRecordInfo: JsonElement?,

        // 以下两项是给 APP 使用的

        @SerializedName("style")
        val showStyle: Int,
        @SerializedName("type")
        val type: Int
) : DynamicData {
    data class LivePlayInfo(
            @SerializedName("area_id")
            val areaId: Int,
            @SerializedName("area_name")
            val areaName: String,
            @SerializedName("cover")
            val coverImage: String,
            @SerializedName("link")
            val link: String,
            @SerializedName("live_id")
            val liveId: Long,
            @SerializedName("live_screen_type")
            val liveScreenType: Int,
            @SerializedName("live_start_time")
            val liveStartTime: Long,
            /**
             * 0 未在直播 1 正在直播 2 投稿视频轮播
             */
            @SerializedName("live_status")
            val liveStatus: Int,
            @SerializedName("online")
            val onlineCount: Long,
            @SerializedName("parent_area_id")
            val parentAreaId: Int,
            @SerializedName("parent_area_name")
            val parentAreaName: String,
            @SerializedName("play_type")
            val playType: Int,
            @SerializedName("room_id")
            val roomId: Long,
            @SerializedName("room_type")
            val roomType: Int,
            @SerializedName("title")
            val liveTitle: String,
            @SerializedName("uid")
            val uid: Long
    ) {
        fun getLiveStartTime(): LocalDateTime = liveStartTime.toLocalDateTime()
    }

    override fun getContact(): MessageWrapper {
        val wrapped = MessageWrapper().addText("${DynamicApi.getUserNameByMid(livePlayInfo.uid)} 正在直播!\n" +
                "标题: ${livePlayInfo.liveTitle}\n" +
                "开播时间: ${BotVariables.yyMMddPattern.format(livePlayInfo.getLiveStartTime())}\n" +
                "直达链接: ${livePlayInfo.link}\n")

        wrapped.addElement(Picture(livePlayInfo.coverImage))

        return wrapped
    }

    override fun getSentTime(): LocalDateTime = livePlayInfo.getLiveStartTime()
}