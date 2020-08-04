package io.github.starwishsama.comet.api.twitter

import cn.hutool.http.ContentType
import cn.hutool.http.HttpException
import cn.hutool.http.HttpResponse
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.roxstudio.utils.CUrl
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BotVariables.gson
import io.github.starwishsama.comet.api.ApiExecutor
import io.github.starwishsama.comet.exceptions.EmptyTweetException
import io.github.starwishsama.comet.exceptions.RateLimitException
import io.github.starwishsama.comet.exceptions.TwitterApiException
import io.github.starwishsama.comet.objects.pojo.twitter.Tweet
import io.github.starwishsama.comet.objects.pojo.twitter.TwitterUser
import io.github.starwishsama.comet.utils.BotUtil
import io.github.starwishsama.comet.utils.FileUtil
import io.github.starwishsama.comet.utils.NetUtil
import io.github.starwishsama.comet.utils.isType
import java.io.IOException
import java.time.Duration
import java.time.LocalDateTime

/**
 * Twitter API
 *
 * 支持获取蓝鸟用户信息 & 最新推文
 * @author Nameless
 */
object TwitterApi : ApiExecutor {
    // 蓝鸟 API 地址
    private const val twitterApiUrl = "https://api.twitter.com/1.1/"

    // 使用 curl 获取 token, 请
    private const val twitterTokenGetUrl = "https://api.twitter.com/oauth2/token"

    // Bearer Token
    var token: String? = BotVariables.cache["token"].asString

    private var cacheTweet = mutableMapOf<String, Tweet>()

    // Api 调用次数
    override var usedTime: Int = 0

    // 超时重连次数
    private var retryTime: Int = 0

    private const val apiReachLimit = "已达到 Twitter API调用上限"

    fun getBearerToken() {
        try {
            val curl = CUrl(twitterTokenGetUrl).opt(
                    "-u",
                    "${BotVariables.cfg.twitterToken}:${BotVariables.cfg.twitterSecret}",
                    "--data",
                    "grant_type=client_credentials"
            )

            if (BotVariables.cfg.proxyUrl.isNotEmpty() && BotVariables.cfg.proxyPort != -1) {
                curl.proxy(BotVariables.cfg.proxyUrl, BotVariables.cfg.proxyPort)
            }

            val result = curl.exec("UTF-8")

            if (JsonParser.parseString(result).isJsonObject) {
                // Get Token
                token = JsonParser.parseString(result).asJsonObject["access_token"].asString
                BotVariables.logger.debug("[蓝鸟] 成功获取 Access Token")
            }
        } catch (e: IOException) {
            BotVariables.logger.warning("获取 Token 时出现问题", e)
        }
    }

    @Throws(RateLimitException::class, TwitterApiException::class)
    fun getUserInfo(username: String): TwitterUser? {
        if (isReachLimit()) {
            throw RateLimitException(apiReachLimit)
        }

        usedTime++

        val startTime = LocalDateTime.now()
        val url = "$twitterApiUrl/users/show.json?screen_name=$username&tweet_mode=extended"
        val conn = NetUtil.doHttpRequestGet(url, 12_000)
                .header("authorization", "Bearer $token")

        var result: HttpResponse? = null
        try {
            result = conn.executeAsync()
        } catch (e: HttpException) {
            BotVariables.logger.warning("[蓝鸟] 在获取用户信息时出现了问题", e)
        }

        var tUser: TwitterUser? = null
        val body = result?.body()

        if (body != null) {
            try {
                tUser = gson.fromJson(result?.body(), TwitterUser::class.java)
            } catch (e: Throwable) {
                FileUtil.createErrorReportFile("twitter", e, body, url)
            }
        }

        BotVariables.logger.debug("[蓝鸟] 查询用户信息耗时 ${Duration.between(startTime, LocalDateTime.now()).toMillis()}ms")
        return tUser
    }

    @Throws(RateLimitException::class, EmptyTweetException::class, TwitterApiException::class)
    fun getUserLatestTweet(username: String): Tweet? {
        if (isReachLimit()) {
            throw RateLimitException(apiReachLimit)
        }

        usedTime++
        val request =
                NetUtil.doHttpRequestGet(
                        "$twitterApiUrl/statuses/user_timeline.json?screen_name=$username&count=2&tweet_mode=extended",
                        5_000
                ).header("authorization", "Bearer $token")

        val result = request.execute()

        if (result.isOk && result.isType(ContentType.JSON.value)) {
            return parseJsonToTweet(result.body())
        }

        return null
    }

    @Throws(RateLimitException::class)
    fun getTweetById(id: Long): Tweet? {
        if (isReachLimit()) {
            throw RateLimitException(apiReachLimit)
        }

        val request = NetUtil.doHttpRequestGet("$twitterApiUrl/statuses/show.json?id=$id&tweet_mode=extended", 5_000).header("authorization", "Bearer $token")
        val response = request.executeAsync()

        try {
            if (response.isOk && response.isType(ContentType.JSON.value)) {
                return parseJsonToTweet(response.body())
            }
        } catch (t: Throwable) {
            FileUtil.createErrorReportFile("twitter", t, response.body(), request.url)
        }

        return null
    }

    @Throws(RateLimitException::class)
    fun getTweetWithCache(username: String): Tweet? {
        val startTime = LocalDateTime.now()
        var tweet: Tweet? = null

        BotUtil.executeWithRetry(
                {
                    try {
                        if (retryTime >= 3) retryTime = 0

                        var cachedTweet = cacheTweet[username]
                        val result: Tweet?

                        if (cachedTweet == null) {
                            cachedTweet = getUserLatestTweet(username)
                        }

                        result = if (Duration.between(cachedTweet?.getSentTime(), LocalDateTime.now()).toMinutes() <= 3
                        ) {
                            cachedTweet
                        } else {
                            getUserLatestTweet(username)
                        }

                        BotVariables.logger.debug(
                                "[蓝鸟] 查询用户最新推文耗时 ${Duration.between(startTime, LocalDateTime.now()).toMillis()}ms"
                        )
                        tweet = result
                    } catch (x: TwitterApiException) {
                        BotVariables.logger.warning("[蓝鸟] 调用 API 时出现了问题", x)
                    }
                },
                3,
                "[蓝鸟] 调用 API 时出现了问题: 超时重连失败"
        )

        return tweet
    }

    @Synchronized
    fun addCacheTweet(username: String, tweet: Tweet) {
        cacheTweet[username] = tweet
    }

    private fun parseJsonToTweet(json: String): Tweet? {
        val parsedTweet: Tweet? = try {
            gson.fromJson(json, Tweet::class.java)
        } catch (e: JsonSyntaxException) {
            (gson.fromJson(json, object : TypeToken<List<Tweet>>() {}.type) as List<Tweet>)[0]
        }

        if (parsedTweet != null) {
            addCacheTweet(parsedTweet.user.name, parsedTweet)
        }
        return parsedTweet
    }

    override fun isReachLimit(): Boolean {
        return usedTime >= getLimitTime()
    }

    /** Twitter API: 1500次/15min, 10w次/24h */
    override fun getLimitTime(): Int = 1000
}