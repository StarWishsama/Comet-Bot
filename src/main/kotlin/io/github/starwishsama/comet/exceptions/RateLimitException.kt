package io.github.starwishsama.comet.exceptions

class RateLimitException(reason: String = "已到达 API 调用上限") : RuntimeException(reason)