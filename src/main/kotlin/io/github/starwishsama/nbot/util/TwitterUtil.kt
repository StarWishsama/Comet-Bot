package io.github.starwishsama.nbot.util

import cn.hutool.http.HttpException
import cn.hutool.http.HttpRequest
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.roxstudio.utils.CUrl
import io.github.starwishsama.nbot.BotConstants
import io.github.starwishsama.nbot.BotInstance
import io.github.starwishsama.nbot.exceptions.RateLimitException
import io.github.starwishsama.nbot.objects.pojo.twitter.Tweet
import io.github.starwishsama.nbot.objects.pojo.twitter.TwitterUser
import java.io.IOException
import java.net.Proxy
import java.net.Socket
import java.time.Duration
import java.time.LocalDateTime


object TwitterUtil {
    // 蓝鸟 API 地址
    private const val universalApi = "https://api.twitter.com/1.1/"

    // 使用 curl, 请
    private const val tokenGetApi = "https://api.twitter.com/oauth2/token"

    // Bearer Token
    var token: String = BotConstants.cache["token"].asString

    // Token 获取时间, 时间过长需要重新获取, Token 可能会到期
    var tokenGetTime = BotConstants.cache["get_time"].asLong

    // Api 调用次数
    var apiExecuteTime = 0

    // 代理地址
    private val proxyHost = BotConstants.cfg.proxyUrl

    fun getBearerToken() {
        try {
            val curl = CUrl(tokenGetApi).opt(
                "-u",
                "${BotConstants.cfg.twitterToken}:${BotConstants.cfg.twitterSecret}",
                "--data",
                "grant_type=client_credentials"
            )

            val proxyHost = BotConstants.cfg.proxyUrl
            if (proxyHost != null && BotConstants.cfg.proxyPort != -1) {
                curl.proxy(proxyHost, BotConstants.cfg.proxyPort)
            }
            val result = curl.exec("UTF-8")

            if (JsonParser.parseString(result).isJsonObject) {
                // Get Token
                println("Ladies & gentleman, We got it.")
                token = JsonParser.parseString(result).asJsonObject["access_token"].asString
            }
        } catch (e: IOException) {
            BotInstance.logger.error("获取 Token 出现问题", e)
        }
    }

    @Throws(RateLimitException::class)
    fun getUserInfo(username: String): TwitterUser? {
        if (apiExecuteTime < 1500) {
            apiExecuteTime++
            return try {
                val startTime = LocalDateTime.now()
                val request = HttpRequest.get("$universalApi/users/show.json?screen_name=$username")
                        .header(
                                "authorization",
                                "Bearer $token"
                        )
                        .timeout(8000)

                if (proxyHost != null && BotConstants.cfg.proxyPort != -1) {
                    request.setProxy(
                            Proxy(
                                    Proxy.Type.HTTP,
                                    Socket(proxyHost, BotConstants.cfg.proxyPort).remoteSocketAddress
                            )
                    )
                }

                val result = request.executeAsync()
                val entity = BotConstants.gson.fromJson(result.body(), TwitterUser::class.java)
                BotInstance.logger.debug("[蓝鸟] 查询用户信息耗时 ${Duration.between(startTime, LocalDateTime.now()).toMillis()}ms")
                entity
            } catch (e: Exception) {
                BotInstance.logger.error("获取蓝鸟用户信息出现问题", e)
                null
            }
        } else {
            throw RateLimitException("已达到API调用上限")
        }
    }

    @Throws(RateLimitException::class)
    fun getLatestTweet(username: String): Tweet? {
        if (apiExecuteTime < 1500) {
            return try {
                val startTime = LocalDateTime.now()
                val request = HttpRequest.get("$universalApi/statuses/user_timeline.json?screen_name=$username&count=2")
                    .header(
                        "authorization",
                        "Bearer $token"
                    )
                    .timeout(8000)

                if (proxyHost != null && BotConstants.cfg.proxyPort != -1) {
                    request.setProxy(
                        Proxy(
                            Proxy.Type.HTTP,
                            Socket(proxyHost, BotConstants.cfg.proxyPort).remoteSocketAddress
                        )
                    )
                }

                val result = request.executeAsync()
                BotInstance.logger.debug("[蓝鸟] 查询用户最新推文耗时 ${Duration.between(startTime, LocalDateTime.now()).toMillis()}ms")
                BotInstance.logger.debug("获取到的 Json: \n${result.body()}")
                return (BotConstants.gson.fromJson(
                    result.body(),
                    object : TypeToken<List<Tweet>>() {}.type
                ) as List<Tweet>)[0]
            } catch (e: IOException) {
                null
            } catch (e: NullPointerException) {
                null
            } catch (e: HttpException) {
                BotInstance.logger.error("[蓝鸟] 在获取用户最新推文时出现了问题")
                null
            } catch (e: JsonSyntaxException) {
                BotInstance.logger.error("[蓝鸟] 解析推文 JSON 时出现问题", e)
                null
            }
        } else {
            throw RateLimitException("已达到API调用上限")
        }
    }
}