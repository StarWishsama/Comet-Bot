/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 MIT 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the MIT License which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/dev/LICENSE
 */

package ren.natsuyuk1.comet.api.event

/**
 * [EventPriopity]
 *
 * 事件优先级, 从左到右优先级逐次减小.
 */
enum class EventPriopity {
    HIGHEST, HIGH, NORMAL, LOW, LOWEST, MONITOR;
}
