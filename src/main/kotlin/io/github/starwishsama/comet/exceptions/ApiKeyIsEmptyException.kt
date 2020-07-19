package io.github.starwishsama.comet.exceptions

class ApiKeyIsEmptyException(apiName: String) : RuntimeException("$apiName API 的 APIKey 必须不为空!")