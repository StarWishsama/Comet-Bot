/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.api.thirdparty.penguinstats.data

import com.fasterxml.jackson.annotation.JsonProperty

data class DropMatrix(
    /**
     * 关卡内部 ID
     */
    val stageId: String,
    /**
     * 物品内部 ID
     */
    @JsonProperty("itemId")
    val itemID: String,
    /**
     * 间隔时间内掉落的物品
     */
    @JsonProperty("quantity")
    val quantity: Long,
    /**
     * 在间隔时间内需要刷关卡的次数
     */
    @JsonProperty("times")
    val times: Int,
    /**
     * 该掉落开始时间, 单位毫秒
     */
    @JsonProperty("start")
    val start: Long,
    /**
     * 该掉落结束时间, 单位毫秒
     */
    @JsonProperty("end")
    val end: Long
)