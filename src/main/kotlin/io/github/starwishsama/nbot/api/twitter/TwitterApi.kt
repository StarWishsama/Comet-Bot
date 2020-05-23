package io.github.starwishsama.nbot.api.twitter

import cn.hutool.http.HttpException
import cn.hutool.http.HttpRequest
import cn.hutool.http.HttpResponse
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.roxstudio.utils.CUrl
import io.github.starwishsama.nbot.BotConstants
import io.github.starwishsama.nbot.BotConstants.gson
import io.github.starwishsama.nbot.BotMain
import io.github.starwishsama.nbot.api.ApiExecutor
import io.github.starwishsama.nbot.exceptions.EmptyTweetException
import io.github.starwishsama.nbot.exceptions.RateLimitException
import io.github.starwishsama.nbot.objects.pojo.twitter.Tweet
import io.github.starwishsama.nbot.objects.pojo.twitter.TwitterErrorInfo
import io.github.starwishsama.nbot.objects.pojo.twitter.TwitterUser
import java.io.IOException
import java.net.Proxy
import java.net.Socket
import java.time.Duration
import java.time.LocalDateTime

/**
 * Twitter API
 * 支持获取蓝鸟用户信息 & 最新推文
 * @author Nameless
 */
object TwitterApi : ApiExecutor {
    // 蓝鸟 API 地址
    private const val universalApi = "https://api.twitter.com/1.1/"

    // 使用 curl, 请
    private const val tokenGetApi = "https://api.twitter.com/oauth2/token"

    // Bearer Token
    var token: String = BotConstants.cache["token"].asString

    // Token 获取时间, 时间过长需要重新获取, Token 可能会到期
    var tokenGetTime = BotConstants.cache["get_time"].asLong

    // Api 调用次数
    override var usedTime: Int = 0

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
                token = JsonParser.parseString(result).asJsonObject["access_token"].asString
                BotMain.logger.debug("[蓝鸟] 成功获取 Access Token")
            }
        } catch (e: IOException) {
            BotMain.logger.error("获取 Token 时出现问题", e)
        }
    }

    @Throws(RateLimitException::class)
    fun getUserInfo(username: String): TwitterUser? {
        if (isReachLimit()) {
            throw RateLimitException("已达到API调用上限")
        }

        usedTime++
        return try {
            val startTime = LocalDateTime.now()
            val request = HttpRequest.get("$universalApi/users/show.json?screen_name=$username")
                    .header(
                            "authorization",
                            "Bearer $token"
                    )
                    .timeout(12_000)

            if (proxyHost != null && BotConstants.cfg.proxyPort != -1) {
                request.setProxy(
                        Proxy(
                                Proxy.Type.HTTP,
                                Socket(proxyHost, BotConstants.cfg.proxyPort).remoteSocketAddress
                        )
                )
            }

            val result = request.executeAsync()
            val entity = gson.fromJson(result.body(), TwitterUser::class.java)
            BotMain.logger.debug("[蓝鸟] 查询用户信息耗时 ${Duration.between(startTime, LocalDateTime.now()).toMillis()}ms")
            entity
        } catch (e: Exception) {
            BotMain.logger.error("获取蓝鸟用户信息出现问题", e)
            null
        }
    }

    @Throws(RateLimitException::class, EmptyTweetException::class)
    fun getLatestTweet(username: String): Tweet? {
        if (isReachLimit()) {
            throw RateLimitException("已达到API调用上限")
        }

        usedTime++
        val startTime = LocalDateTime.now()
        val request = HttpRequest.get("$universalApi/statuses/user_timeline.json?screen_name=$username&count=2")
                .header(
                        "authorization",
                        "Bearer $token"
                )
                .timeout(12_000)

        if (proxyHost != null && BotConstants.cfg.proxyPort != -1) {
            request.setProxy(
                    Proxy(
                            Proxy.Type.HTTP,
                            Socket(proxyHost, BotConstants.cfg.proxyPort).remoteSocketAddress
                    )
            )
        }

        var result : HttpResponse? = null
        try {
            result = request.executeAsync()
        } catch (e: HttpException) {
            BotMain.logger.error("[蓝鸟] 在获取用户最新推文时出现了问题", e)
        }

        BotMain.logger.debug("[蓝鸟] 查询用户最新推文耗时 ${Duration.between(startTime, LocalDateTime.now()).toMillis()}ms")

        if (result != null) {
            var tweet: Tweet? = null
            try {
                val tweets = gson.fromJson(
                        result.body(),
                        object : TypeToken<List<Tweet>>() {}.type
                ) as List<Tweet>

                if (tweets.isEmpty()) {
                    throw EmptyTweetException()
                }
                tweet = tweets[0]
            } catch (e: JsonSyntaxException) {
                try {
                    val errorInfo = gson.fromJson(result.body(), TwitterErrorInfo::class.java)
                    BotMain.logger.error("[蓝鸟] 调用 API 时出现了问题\n${errorInfo.getReason()}")
                } catch (e: JsonSyntaxException) {
                    BotMain.logger.error("[蓝鸟] 解析推文 JSON 时出现问题: 不支持的类型", e)
                }
            }
            return tweet
        } else {
           return null
        }
    }

    override fun isReachLimit(): Boolean {
        return usedTime > getLimitTime()
    }

    override fun getLimitTime(): Int = 1500
}