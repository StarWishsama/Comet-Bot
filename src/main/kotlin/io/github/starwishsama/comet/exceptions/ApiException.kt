package io.github.starwishsama.comet.exceptions

open class ApiException(reason: String = "") : RuntimeException(reason)

/**
 * API 调用达到上限
 */
class RateLimitException(reason: String = "已到达 API 调用上限") : ApiException(reason)

/**
 * 重试次数达到上限
 */
class ReachRetryLimitException : ApiException()