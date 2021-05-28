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
import io.github.starwishsama.comet.api.thirdparty.bilibili.DynamicApi
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.DynamicData
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import io.github.starwishsama.comet.objects.wrapper.Picture
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class LiveRoom(
    /** 直播间 ID, 例如 21396545 */
    @JsonProperty("roomid")
    val roomID: Long,
    /** 用户 UID, 例如 407106379 */
    @JsonProperty("uid")
    val uid: Long,
    /** 用户名, 例如 绯赤艾莉欧Official*/
    @JsonProperty("uname")
    val userName: String,
    /** 认证类型(?) */
    @JsonProperty("verify")
    val verifyName: String?,
    /** 似乎是新加的, 判断是不是 VTuber/Vup, 1为VTuber/Vup */
    @JsonProperty("virtual")
    val isVirtual: Int,
    /** 直播间封面图片 */
    @JsonProperty("cover")
    val coverImg: String,
    /** 直播时间, 格式 yyyy-MM-dd HH:mm:ss, 例如 2020-10-31 11:02:26 */
    @JsonProperty("live_time")
    val liveTime: String,
    @JsonProperty("round_status")
    val roundStatus: Int,
    @JsonProperty("on_flag")
    val onFlag: Int,
    @JsonProperty("title")
    val title: String?,
    /**
     * 直播间 Tag, 例如 "润羽露西娅,潤羽るしあ,VTuber,hololive,虚拟UP主"
     */
    @JsonProperty("tags")
    val tags: String,
    /** 直播间封禁时间, 格式 yyyy-MM-dd HH:mm:ss */
    @JsonProperty("lock_status")
    val lockStatus: String,
    @JsonProperty("hidden_status")
    val hiddenStatus: String,
    @JsonProperty("user_cover")
    val userCoverImg: String,
    /** 直播间短ID, 部分UP主才有 */
    @JsonProperty("short_id")
    val shortId: Int,
    /** 人气, 肯定不是真实在线人数 */
    @JsonProperty("online")
    val onlineCount: Int,
    /** 旧版分区 ID */
    @JsonProperty("area")
    val area: Int,
    /** 旧版分区 ID */
    @JsonProperty("area_v2_id")
    val areaNew: Int,
    /** 旧版大区 ID */
    @JsonProperty("area_v2_parent_id")
    val areaParentNew: Int,
    /** 未知, 可能是警告次数? */
    @JsonProperty("attentions")
    val attentionCount: Int,
    /** 直播间背景 */
    @JsonProperty("background")
    val liveBackgroundImg: String,
    @JsonProperty("room_silent")
    val roomSilent: Int,
    @JsonProperty("room_shield")
    val roomShield: Int,
    /** 格式 yyyy-MM-dd HH:mm:ss */
    @JsonProperty("try_time")
    val tryTime: String,
    /** 直播分区名, 例如 虚拟主播 */
    @JsonProperty("area_v2_name")
    val areaNameNew: String,
    /** 首次直播时间, 时间戳 */
    @JsonProperty("first_live_time")
    val firstLiveTime: Long,
    @JsonProperty("live_status")
    val liveStatus: Int,
    @JsonProperty("area_v2_parent_name")
    val areaParentNameNew: String,
    @JsonProperty("broadcast_type")
    val broadcastType: Int,
    @JsonProperty("face")
    val face: String
) : DynamicData {
    private val yyMMddPattern: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    private fun getRoomURL(): String = "https://live.bilibili.com/$roomID"

    enum class Status(var status: String) {
        NoStreaming("闲置"), Streaming("直播中"), PlayingVideo("轮播中"), Unknown("未知");
    }

    private fun getStatus(roundStatus: Int): Status {
        return when (roundStatus) {
            0 -> Status.NoStreaming
            1 -> Status.Streaming
            2 -> Status.PlayingVideo
            else -> Status.Unknown
        }
    }

    override fun getContact(): MessageWrapper {
        val wrapped = MessageWrapper().addText(
            "分享了 ${DynamicApi.getUserNameByMid(uid)} 的直播间\n" +
                    "直播间标题: ${title}\n" +
                    "直播状态: ${getStatus(roundStatus).status}\n" +
                    "直达链接: ${getRoomURL()}\n"
        )
        if (coverImg.isNotEmpty()) {
            wrapped.addElement(Picture(coverImg))
        }
        return wrapped
    }

    override fun getSentTime(): LocalDateTime = LocalDateTime.from(yyMMddPattern.parse(liveTime))
}