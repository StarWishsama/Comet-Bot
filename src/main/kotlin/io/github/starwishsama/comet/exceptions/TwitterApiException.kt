package io.github.starwishsama.comet.exceptions

class TwitterApiException(val code: Int, val reason: String) : RuntimeException(reason)