/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package ren.natsuyuk1.comet.objects.gacha.items

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 明日方舟干员
 *
 * 格式: 官方角色数据拆包
 */
data class ArkNightOperator(
    override val name: String,
    val desc: String = "",
    /**
     * 星级
     *
     * 注意: 明日方舟官方数据中, 星级从0开始计起
     * 即一星干员获取后为零, 但自定义卡池不受影响
     */
    @JsonProperty("rarity")
    override val rare: Int = 0,
    /**
     * 获得途径
     */
    @JsonProperty("itemObtainApproach")
    val obtain: String?,
    override val count: Int = 1,
) : GachaItem()