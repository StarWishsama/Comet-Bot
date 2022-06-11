/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 */

package ren.natsuyuk1.comet.mirai.config

import kotlinx.serialization.Serializable
import ren.natsuyuk1.comet.config.CometConfig

@Serializable
class MiraiCometConfig(
    /**
     * 机器人的 ID
     *
     * 在 QQ 平台, 该变量为 QQ 号. 而 Telegram 则是 token.
     *
     * 在 Telegram 平台, 如果你担心安全问题, 可以自主设计加解密转换.
     */
    override val id: String,

    /**
     * 机器人的登录密码
     */
    val password: String,
) : CometConfig()
