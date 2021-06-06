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

data class ArkNightItemInfo(
    val itemId: String,
    val displayName: String,
    val sortId: Long,
    val rarity: Int,
    //val existenceStatus: JsonNode
    val itemType: String,
    @JsonProperty("addTimePoint")
    val perTimePoint: Int,
    @JsonProperty("name_i18n")
    val localizedName: LocalizedObject,
    @JsonProperty("alias")
    val alias: Alias
) {
    data class LocalizedObject(
        val ko: String,
        val ja: String,
        val en: String,
        val zh: String
    ) {
        fun contains(name: String): Boolean {
            return ko == name || ja == name || en == name || zh == name
        }
    }

    data class Alias(
        val jp: List<String>,
        val zh: List<String>
    ) {
        fun contains(name: String): Boolean {
            return jp.contains(name) || zh.contains(name)
        }
    }

    fun exists(name: String): Boolean {
        return localizedName.contains(name) || alias.contains(name)
    }
}