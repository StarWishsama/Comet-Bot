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
import io.github.starwishsama.comet.api.thirdparty.penguinstats.PenguinStats

data class MatrixResponse(
    @JsonProperty("matrix")
    val matrix: List<DropMatrix>
) {
    override fun toString(): String {
        return buildString {
            if (matrix.isEmpty()) {
                return ""
            }

            append("${PenguinStats.itemInfo.find { item -> item.itemId == matrix[0].itemID }?.displayName ?: matrix[0].itemID}可在以下关卡获取:\n")

            matrix.forEach {
                append(
                    PenguinStats.stageInfo.find { info -> info.stageId == it.stageId }?.localizedCode?.zh
                        ?: it.stageId + ", "
                )
            }
        }.removeSuffix(", ")
    }
}