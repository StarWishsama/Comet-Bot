/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 MIT 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the MIT License which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/dev/LICENSE
 */

package ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.sekaibest

import kotlinx.serialization.json.JsonObject

@kotlinx.serialization.Serializable
data class SekaiBestPredictionInfo(
    val status: String,
    val data: JsonObject,
    val message: String
)
