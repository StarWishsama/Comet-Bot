/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 MIT 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the MIT License which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/dev/LICENSE
 */

package ren.natsuyuk1.comet.mirai.config

import net.mamoe.mirai.utils.BotConfiguration

@kotlinx.serialization.Serializable
data class MiraiConfig(
    val id: Long,
    val password: String,
    val protocol: BotConfiguration.MiraiProtocol = BotConfiguration.MiraiProtocol.ANDROID_PHONE,
    val heartbeatStrategy: BotConfiguration.HeartbeatStrategy = BotConfiguration.HeartbeatStrategy.STAT_HB,
)
