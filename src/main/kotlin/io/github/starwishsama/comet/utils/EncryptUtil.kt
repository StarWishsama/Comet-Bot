package io.github.starwishsama.comet.utils

import cn.hutool.crypto.SecureUtil

fun String.toHMAC(key: String): String {
    return SecureUtil.hmacSha256(key).digestHex(this)
}