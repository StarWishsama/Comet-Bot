package io.github.starwishsama.comet.utils

import cn.hutool.crypto.SecureUtil

fun String.toSHA256(): String {
    return SecureUtil.sha256(this)
}