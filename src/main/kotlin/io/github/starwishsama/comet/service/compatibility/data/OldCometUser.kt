/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.service.compatibility.data

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.starwishsama.comet.objects.enums.UserLevel
import java.time.LocalDateTime

data class OldCometUser(
    @JsonProperty("userQQ")
    val id: Long,
    var lastCheckInTime: LocalDateTime = LocalDateTime.now().minusDays(1),
    var checkInPoint: Double = 0.0,
    var checkInTime: Int = 0,
    var r6sAccount: String = "",
    var level: UserLevel = UserLevel.USER,
    var checkInGroup: Long = 0,
    var lastExecuteTime: Long = -1,
    private val permissions: MutableSet<String> = mutableSetOf(),
)
