/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.service.server

import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.utils.toHMAC
import java.net.InetSocketAddress

object ServerUtil {
    private val coolDownCache = mutableMapOf<String, Long>()

    fun checkSignature(secret: String, remote: String, requestBody: String): Boolean {
        val local = "sha256=" + requestBody.toHMAC(secret)
        daemonLogger.debug("本地解析签名为: $local, 远程签名为: $remote")
        return local == remote
    }

    fun checkCoolDown(remote: InetSocketAddress): Boolean {
        val target = coolDownCache[remote.hostString]

        return when {
            target == null -> {
                coolDownCache[remote.hostString] = System.currentTimeMillis()
                false
            }
            System.currentTimeMillis() - target > 10 * 1000 -> {
                coolDownCache.remove(remote.hostString)
                true
            }
            else -> false
        }
    }
}