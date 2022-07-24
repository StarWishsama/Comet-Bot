package ren.natsuyuk1.comet.network.thirdparty.jikipedia

import cn.hutool.core.codec.Base64
import cn.hutool.core.lang.UUID
import io.ktor.client.request.*
import ren.natsuyuk1.comet.api.config.CometConfig
import ren.natsuyuk1.comet.consts.cometClient
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

private val logger = mu.KotlinLogging.logger {}

object JikiPediaAPI {
    private val iv = "12uh00]35#@(poj[".toByteArray()

    private fun generateKey(): ByteArray {
        val base = "web_2.7.3a_12uh00]35#@(poj[".toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(base)
    }

    fun encodeToJikiXID(): String {
        val base = "jikipedia_xid_${UUID.randomUUID()}".toByteArray()
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(generateKey(), "AES"), IvParameterSpec(ByteArray(16)))
        val cipherText = cipher.doFinal(base)
        return Base64.encode(cipherText)
    }

    suspend fun search(keyword: String): JikiPediaSearchResult {
        return cometClient.client.post<JikiPediaSearchResult>("https://api.jikipedia.com/go/search_entities") {
            headers {
                append("Origin", "https://jikipedia.com")
                append("Referer", "https://jikipedia.com")
                append(
                    "xid",
                    "pZBzqk4B5uHQDyU2satS+FKft78gvi+PruIpjhHJdfudi4PAcYs/TdhfQQeYZxvF8WR0KZM4FHUxK3dPm7rLfC3hexA1MFvsSw3R/eVPw48="
                )
                append("User-Agent", CometConfig.data.useragent)
                append("Client", "web")
                append("Client-Version", "2.7.3a")
                append("Content-Type", "application/json;charset=UTF-8")
                append("Accept", "application/json, text/plain, */*")
            }

            body = JikiPediaSearchRequest(keyword)
        }.also { logger.debug { it } }
    }
}
