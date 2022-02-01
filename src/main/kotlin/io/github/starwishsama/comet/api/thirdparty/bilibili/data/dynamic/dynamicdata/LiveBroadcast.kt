/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.dynamicdata

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.api.thirdparty.bilibili.DynamicApi
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.DynamicData
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import io.github.starwishsama.comet.objects.wrapper.Picture
import io.github.starwishsama.comet.utils.NumberUtil.toLocalDateTime
import java.time.LocalDateTime

data class LiveBroadcast(
    @JsonProperty("live_play_info")
    val livePlayInfo: LivePlayInfo,
    /** B站直播录像信息 */
    @JsonProperty("live_record_info")
    val liveRecordInfo: JsonNode?,

    // 以下两项是给 APP 使用的

    @JsonProperty("style")
    val showStyle: Int,
    @JsonProperty("type")
    val type: Int
) : DynamicData() {
    data class LivePlayInfo(
        @JsonProperty("area_id")
        val areaId: Int,
        @JsonProperty("area_name")
        val areaName: String,
        @JsonProperty("cover")
        val coverImage: String,
        @JsonProperty("link")
        val link: String,
        @JsonProperty("live_id")
        val liveId: Long,
        @JsonProperty("live_screen_type")
        val liveScreenType: Int,
        @JsonProperty("live_start_time")
        val liveStartTime: Long,
        /**
         * 0 未在直播 1 正在直播 2 投稿视频轮播
         */
        @JsonProperty("live_status")
        val liveStatus: Int,
        @JsonProperty("online")
        val onlineCount: Long,
        @JsonProperty("parent_area_id")
        val parentAreaId: Int,
        @JsonProperty("parent_area_name")
        val parentAreaName: String,
        @JsonProperty("play_type")
        val playType: Int,
        @JsonProperty("room_id")
        val roomId: Long,
        @JsonProperty("room_type")
        val roomType: Int,
        @JsonProperty("title")
        val liveTitle: String,
        @JsonProperty("uid")
        val uid: Long
    ) {
        fun getLiveStartTime(): LocalDateTime = liveStartTime.toLocalDateTime()
    }

    override fun asMessageWrapper(): MessageWrapper {
        val wrapped = MessageWrapper().addText(
            "${DynamicApi.getUserNameByMid(livePlayInfo.uid)} 正在直播!\n" +
                    "标题: ${livePlayInfo.liveTitle}\n" +
                    "开播时间: ${CometVariables.yyMMddPattern.format(livePlayInfo.getLiveStartTime())}\n" +
                    "直达链接: ${livePlayInfo.link}\n"
        )

        wrapped.addElement(Picture(livePlayInfo.coverImage))

        return wrapped
    }
}