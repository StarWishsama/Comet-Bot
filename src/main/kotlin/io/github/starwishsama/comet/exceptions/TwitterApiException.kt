package io.github.starwishsama.comet.exceptions

class TwitterApiException(val code: Int, val reason: String) : RuntimeException(reason)

class ApiKeyIsEmptyException(apiName: String) : RuntimeException("$apiName API 的 APIKey 不能为空!")

class EmptyTweetException(val msg: String = "该用户没有发送过推文") : RuntimeException(msg)