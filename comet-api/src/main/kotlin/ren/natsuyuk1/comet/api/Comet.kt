/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 MIT 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the MIT License which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/dev/LICENSE
 */

package ren.natsuyuk1.comet.api

import mu.KLogger
import ren.natsuyuk1.comet.api.config.CometConfig
import ren.natsuyuk1.comet.utils.coroutine.ModuleScope

/**
 * [Comet] 代表单个对应多平台的机器人实例
 *
 * 不同平台应实现此实例
 *
 */
abstract class Comet(
    /**
     * 一个 Comet 实例的 [CometConfig]
     */
    val config: CometConfig,

    val logger: KLogger,

    val scope: ModuleScope
) {
    abstract fun login()

    abstract fun close()
}
