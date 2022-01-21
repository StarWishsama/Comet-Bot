/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package ren.natsuyuk1.comet.exceptions

open class ApiException(reason: String = "", cause: Exception? = null) : RuntimeException(reason, cause)

/**
 * API 调用达到上限
 */
class RateLimitException(reason: String = "已到达 API 调用上限") : ApiException(reason)

/**
 * 重试次数达到上限
 */
class ReachRetryLimitException : ApiException()
