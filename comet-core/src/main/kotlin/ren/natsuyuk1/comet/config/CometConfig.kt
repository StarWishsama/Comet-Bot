/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 */

package ren.natsuyuk1.comet.config

import kotlinx.serialization.Serializable

var config = CometConfig()

@Serializable
data class CometConfig(
    /**
     * 机器人的 ID
     *
     * 在 QQ 平台, 该变量为 QQ 号. 而 Telegram 则是 token.
     *
     * 在 Telegram 平台, 如果你担心安全问题, 可以自主设计加解密转换.
     */
    val id: Long = 0,

    /**
     * 自动保存数据的周期, 单位为分钟
     */
    var dataSaveDuration: Long = 60,

    /**
     * 机器人调用冷却时间, 单位为秒
     */
    var defaultCoolDownTime: Int = 5,

    /**
     * 进行网络请求时的 User-Agent, 部分特殊请求时不遵循该 User-Agent
     */
    var useragent: String = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.5005.124 Safari/537.36 Edg/102.0.1245.41"
)
