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
import io.github.starwishsama.comet.BotVariables.logger
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
    var token = BotVariables.cfg.twitterToken

    private var cacheTweet = mutableMapOf<String, Tweet>()

    // Api 调用次数
    override var usedTime: Int = 0

    private const val apiReachLimit = "已达到 Twitter API调用上限"

    fun getBearerToken() {
        try {
            val curl = CUrl(twitterTokenGetUrl).opt(
                "-u",
                "${BotVariables.cfg.twitterAccessToken}:${BotVariables.cfg.twitterAccessSecret}",
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
                logger.debug("[蓝鸟] 成功获取 Access Token")
            }
        } catch (e: IOException) {
            logger.warning("获取 Token 时出现问题", e)
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
            logger.warning("[蓝鸟] 在获取用户信息时出现了问题", e)
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

        logger.debug("[蓝鸟] 查询用户信息耗时 ${Duration.between(startTime, LocalDateTime.now()).toMillis()}ms")
        return tUser
    }

    @Throws(RateLimitException::class, EmptyTweetException::class, TwitterApiException::class)
    fun getUserTweets(username: String, count: Int): List<Tweet> {
        val tweets = mutableListOf<Tweet>()

        if (isReachLimit()) {
            throw RateLimitException(apiReachLimit)
        }

        if (count >= 3200) throw UnsupportedOperationException("This method can only return up to 3,200 of a user's most recent Tweets")

        usedTime++
        val request =
                NetUtil.doHttpRequestGet(
                        "$twitterApiUrl/statuses/user_timeline.json?screen_name=$username&count=${count}&tweet_mode=extended",
                        5_000
                ).header("authorization", "Bearer $token").header("content-type", "application/json;charset=utf-8")

        val result = request.execute()

        if (result.isOk && result.isType(ContentType.JSON.value)) {
            return parseJsonToTweet(result.body(), request.url).sortedByDescending { it.getSentTime() }
        }

        return tweets
    }

    @Throws(RateLimitException::class)
    fun getTweetById(id: Long): Tweet? {
        if (isReachLimit()) {
            throw RateLimitException(apiReachLimit)
        }

        val request = NetUtil.doHttpRequestGet("$twitterApiUrl/statuses/show.json?id=$id&tweet_mode=extended", 5_000).header("authorization", "Bearer $token")
        val response = request.executeAsync()


        if (response.isOk && response.isType(ContentType.JSON.value)) {
            return parseJsonToTweet(response.body(), request.url)[0]
        }

        return null
    }

    fun getCachedTweet(username: String, index: Int = 0, max: Int = 10): TweetResponse {
        val startTime = LocalDateTime.now()
        var tweet: Tweet? = null

        val executedStatus = BotUtil.executeWithRetry({
            try {
                var cachedTweet = cacheTweet[username]
                val result: Tweet?

                if (cachedTweet == null) {
                    cachedTweet = getUserTweets(username, max)[index]
                }

                result = if (Duration.between(cachedTweet.getSentTime(), LocalDateTime.now()).toMinutes() <= 3
                ) {
                    cachedTweet
                } else {
                    getUserTweets(username, max)[index]
                }

                logger.debug(
                        "[蓝鸟] 查询用户最新推文耗时 ${Duration.between(startTime, LocalDateTime.now()).toMillis()}ms"
                )
                tweet = result
            } catch (x: TwitterApiException) {
                logger.warning("[蓝鸟] 调用 API 时出现了问题", x)
                    }
                },
                1
        )

        return TweetResponse(tweet, executedStatus)
    }

    @Synchronized
    fun addCacheTweet(username: String, tweet: Tweet) {
        cacheTweet[username] = tweet
    }

    private fun parseJsonToTweet(json: String, url: String): List<Tweet> {
        val parsedTweet: List<Tweet> = try {
            listOf(gson.fromJson(json, Tweet::class.java))
        } catch (e: JsonSyntaxException) {
            try {
                (gson.fromJson(json, object : TypeToken<List<Tweet>>() {}.type) as List<Tweet>)
            } catch (t: Throwable) {
                logger.error("[推文] 在解析推文时出现了问题", t)
                FileUtil.createErrorReportFile("tweet", t, json, url)
                return emptyList()
            }
        }

        if (parsedTweet.isNotEmpty()) {
            parsedTweet.parallelStream().forEach {
                addCacheTweet(it.user.name, it)
            }
        }

        return parsedTweet
    }

    data class TweetResponse(val tweet: Tweet?, val status: BotUtil.TaskStatus)

    override fun isReachLimit(): Boolean {
        return usedTime >= getLimitTime()
    }

    /** Twitter API: 1500次/15min, 10w次/24h */
    override fun getLimitTime(): Int = 1000
}