/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package ren.natsuyuk1.comet.objects.gacha.pool

import java.time.LocalDateTime
import java.time.ZoneOffset

fun GachaPool.isAvailable(): Boolean {
    if (startTime < 0 || endTime < 0) {
        return false
    }

    val startTime = LocalDateTime.ofEpochSecond(startTime, 0, ZoneOffset.ofHours(8))
    val endTime = LocalDateTime.ofEpochSecond(endTime, 0, ZoneOffset.ofHours(8))
    val now = LocalDateTime.now()

    return !now.isAfter(endTime) || !now.isBefore(startTime)
}