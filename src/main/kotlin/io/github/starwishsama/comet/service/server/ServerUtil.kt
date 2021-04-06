package io.github.starwishsama.comet.service.server

import io.github.starwishsama.comet.BotVariables.cfg
import io.github.starwishsama.comet.utils.toSHA256
import java.net.InetSocketAddress

object ServerUtil {
    private val coolDownCache = mutableMapOf<String, Long>()

    fun checkSignature(remote: String, requestBody: String): Boolean {
        val local = "sha256=" + (cfg.webHookSecret + requestBody).toSHA256()
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