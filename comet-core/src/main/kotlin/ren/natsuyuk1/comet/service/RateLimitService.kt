package ren.natsuyuk1.comet.service

import io.ktor.http.*
import kotlinx.serialization.Serializable
import ren.natsuyuk1.comet.api.config.provider.PersistDataFile
import ren.natsuyuk1.comet.utils.file.dataDirectory
import java.io.File

@Serializable
enum class RateLimitAPI {
    GITHUB
}

object RateLimitData : PersistDataFile<RateLimitData.Data>(
    File(dataDirectory, "rate_limit.json"),
    Data.serializer(),
    Data()
) {
    @Serializable
    data class Data(
        val rateLimitData: MutableMap<RateLimitAPI, Long> = mutableMapOf()
    )
}

object RateLimitService {
    fun checkRate(apiType: RateLimitAPI, headers: Headers) {
        when (apiType) {
            RateLimitAPI.GITHUB -> {
                val remaining = headers["x-ratelimit-remaining"] ?: return
                if (remaining.toIntOrNull() != null && remaining.toInt() > 1) {
                    return
                } else {
                    val nextReset = headers["x-ratelimit-reset"] ?: return
                    if (nextReset.toLongOrNull() == null) {
                        return
                    }
                    RateLimitData.data.rateLimitData[apiType] = nextReset.toLong()
                }
            }
        }
    }

    fun isRateLimit(apiType: RateLimitAPI): Boolean {
        return RateLimitData.data.rateLimitData.containsKey(apiType) && RateLimitData.data.rateLimitData[apiType]!! > System.currentTimeMillis() // ktlint-disable max-line-length
    }
}
