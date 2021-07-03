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

data class ArkNightStageInfo(
    val stageType: StageType,
    val stageId: String,
    val zoneId: String,
    val code: String,
    val apCost: Int,
    val dropInfos: List<DropInfo>,
    @JsonProperty("code_i18n")
    val localizedCode: ArkNightItemInfo.LocalizedObject
) {
    data class DropInfo(
        val itemId: String?,
        val dropType: String,
        val bounds: DropBound
    ) {
        data class DropBound(
            val lower: Int,
            val upper: Int
        )
    }
}

enum class StageType {
    MAIN, ACTIVITY, SUB, DAILY
}
