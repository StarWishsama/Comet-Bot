package io.github.starwishsama.nbot.exceptions

class TwitterApiException(val code: Int, val reason: String) : RuntimeException(reason)