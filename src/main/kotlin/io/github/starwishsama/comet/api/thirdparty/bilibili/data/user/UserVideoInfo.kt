/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.api.thirdparty.bilibili.data.user

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.CommonResponse
import io.github.starwishsama.comet.utils.StringUtil.limitStringSize

data class UserVideoInfo(
    val data: Data
) : CommonResponse() {
    data class Data(
        val list: VideoInfo
    ) {
        data class VideoInfo(
            @JsonProperty("vlist")
            val videoList: List<Video>
        ) {
            data class Video(
                val comment: Long,
                @JsonProperty("pic")
                val cover: String,
                val description: String,
                val title: String,
                val subtitle: String,
                val author: String,
                val mid: Long,
                val created: Long,
                val length: String,
                val aid: Long,
                val bvid: String
            ) {
                override fun toString(): String {
                    return "${title}\n${description.limitStringSize(30)}"
                }
            }
        }
    }
}