package io.github.starwishsama.comet.exceptions

/**
 * API 调用达到上限
 */
class RateLimitException(reason: String = "已到达 API 调用上限") : RuntimeException(reason)