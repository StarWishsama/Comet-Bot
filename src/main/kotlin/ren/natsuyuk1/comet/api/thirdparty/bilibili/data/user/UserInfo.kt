/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package ren.natsuyuk1.comet.api.thirdparty.bilibili.data.user

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.CommonResponse

/**
 * 通过 UID 查询到的用户信息
 *
 * 端点: http://api.bilibili.com/x/web-interface/card
 */
data class UserInfo(
    val data: Data
) : CommonResponse() {
    data class Data(
        val card: InfoCard,
        val follower: Long,
        @JsonProperty("like_num")
        val likeCount: Long
    ) {
        data class InfoCard(
            val mid: Long,
            val name: String,
            val sex: String,
            val face: String,
            val sign: String,
            @JsonProperty("level_info")
            val levelInfo: LevelInfo,
            @JsonProperty("Official")
            val officialVerifyInfo: OfficialVerifyInfo,
            @JsonProperty("vip")
            val vipInfo: VipInfo,
        ) {
            data class LevelInfo(
                @JsonProperty("current_level")
                val currentLevel: Int,
            )

            data class OfficialVerifyInfo(
                val role: Int,
                val title: String,
                @JsonProperty("desc")
                val description: String,
            ) {
                private fun getVerifyType(): String {
                    return when (role) {
                        in 1..2, 7 -> {
                            "个人认证"
                        }
                        in 3..6 -> {
                            "机构认证"
                        }
                        else -> {
                            ""
                        }
                    }
                }

                override fun toString(): String {
                    return if (getVerifyType().isEmpty()) {
                        ""
                    } else {
                        getVerifyType() + " > $description"
                    }
                }
            }

            data class VipInfo(
                val type: Int,
                val status: Int,
                @JsonProperty("due_date")
                val dueDate: Long,
                val label: LabelInfo
            ) {
                data class LabelInfo(
                    val text: String,
                )

                private fun isVip(): Boolean = status == 1

                override fun toString(): String {
                    return if (!isVip()) {
                        ""
                    } else {
                        label.text
                    }
                }
            }
        }
    }
}