package io.github.starwishsama.comet.exceptions

class EmptyTweetException(val msg: String = "该用户没有发送过推文") : RuntimeException(msg)