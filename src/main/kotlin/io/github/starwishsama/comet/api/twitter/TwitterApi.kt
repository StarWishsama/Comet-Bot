package io.github.starwishsama.comet.api.twitter

import cn.hutool.http.ContentType
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.roxstudio.utils.CUrl
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.BotVariables.gson
import io.github.starwishsama.comet.BotVariables.logger
import io.github.starwishsama.comet.api.ApiExecutor
import io.github.starwishsama.comet.exceptions.EmptyTweetException
import io.github.starwishsama.comet.exceptions.RateLimitException
import io.github.starwishsama.comet.exceptions.TwitterApiException
import io.github.starwishsama.comet.objects.pojo.twitter.Tweet
import io.github.starwishsama.comet.objects.pojo.twitter.TwitterUser
import io.github.starwishsama.comet.utils.FileUtil
import io.github.starwishsama.comet.utils.TaskUtil
import io.github.starwishsama.comet.utils.network.NetUtil
import io.github.starwishsama.comet.utils.network.isType
import io.github.starwishsama.comet.utils.network.isUsable
import java.io.IOException
import java.net.Socket
import java.time.Duration
import java.time.LocalDateTime

/**
 * Twitter API
 *
 * 支持获取蓝鸟用户信息 & 最新推文 & 推主时间线
 * @author Nameless
 */
object TwitterApi : ApiExecutor {
    // 蓝鸟 APIv1.1 地址
    private const val twitterApiUrl = "https://api.twitter.com/1.1/"

    // curl 获取 token, 请
    private const val twitterTokenGetUrl = "https://api.twitter.com/oauth2/token"

    // Bearer Token
    private var token = BotVariables.cfg.twitterToken

    private var cacheTweet = mutableMapOf<String, Tweet>()

    // Api 调用次数
    override var usedTime: Int = 0

    private const val apiReachLimit = "已达到 Twitter API 调用上限"

    private fun checkToken() {
        if (token == null) getBearerToken()
    }

    /**
     * 获取用于调用 Twitter API 的 Bearer Token
     */
    private fun getBearerToken() {
        try {
            val curl = CUrl(twitterTokenGetUrl).opt(
                    "-u",
                    "${BotVariables.cfg.twitterAccessToken}:${BotVariables.cfg.twitterAccessSecret}",
                    "--data",
                    "grant_type=client_credentials"
            )

            val socket = Socket(BotVariables.cfg.proxyUrl, BotVariables.cfg.proxyPort)

            if (BotVariables.cfg.proxyUrl.isNotEmpty() && BotVariables.cfg.proxyPort != -1 && socket.isUsable()) {
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

    /**
     * 获取推主的账号信息
     *
     * @throws RateLimitException
     * @throws TwitterApiException
     * @return 账号信息
     */
    @Throws(RateLimitException::class, TwitterApiException::class)
    fun getUserProfile(username: String): TwitterUser? {
        checkToken()

        if (isReachLimit()) {
            throw RateLimitException(apiReachLimit)
        }

        usedTime++

        val startTime = LocalDateTime.now()
        val url = "$twitterApiUrl/users/show.json?screen_name=$username&tweet_mode=extended"
        val conn = NetUtil.doHttpRequestGet(url, 12_000)
                .header("authorization", "Bearer $token")
        var bodyCopy = ""

        try {
            val result = conn.executeAsync()

            val body = result.body()
            bodyCopy = body

            return gson.fromJson(result.body(), TwitterUser::class.java)
        } catch (t: Throwable) {
            if (!NetUtil.isTimeout(t)) {
                FileUtil.createErrorReportFile("twitter", t, bodyCopy, url)
            } else {
                daemonLogger.verbose("[蓝鸟] 在获取用户信息时连接超时")
            }
        }

        daemonLogger.verbose("[蓝鸟] 查询用户信息耗时 ${Duration.between(startTime, LocalDateTime.now()).toMillis()}ms")
        return null
    }

    /**
     * 获取用户时间线上的推文, 最高可获取 3200 条
     *
     * @return 推文列表
     */
    @Throws(RateLimitException::class, EmptyTweetException::class, TwitterApiException::class)
    fun getUserTweets(username: String, count: Int): List<Tweet> {
        checkToken()

        if (isReachLimit()) {
            throw RateLimitException(apiReachLimit)
        }

        if (count >= 3200) throw UnsupportedOperationException("This method can only return up to 3,200 of a user's most recent Tweets")

        usedTime++
        val request =
                NetUtil.doHttpRequestGet(
                        "$twitterApiUrl/statuses/user_timeline.json?screen_name=$username&count=${count}&tweet_mode=extended",
                        5_000)
                        .header("authorization", "Bearer $token")
                        .header("content-type", "application/json;charset=utf-8")

        val result = request.executeAsync()
        val tweetList = parseJsonToTweet(result.body(), request.url)
        return if (tweetList.isNotEmpty()) {
            tweetList.sortedByDescending { it.getSentTime() }
        } else {
            emptyList()
        }
    }

    /**
     * 通过推文 ID 获取指定推文实体类
     *
     * @throws RateLimitException
     * @throws EmptyTweetException
     * @return 推文
     */
    @Throws(RateLimitException::class, EmptyTweetException::class)
    fun getTweetById(id: Long): Tweet {
        checkToken()

        if (isReachLimit()) {
            throw RateLimitException(apiReachLimit)
        }

        val request = NetUtil.doHttpRequestGet("$twitterApiUrl/statuses/show.json?id=$id&tweet_mode=extended", 5_000).header("authorization", "Bearer $token")
        val response = request.executeAsync()

        if (response.isOk && response.isType(ContentType.JSON.value)) {
            return if (parseJsonToTweet(response.body(), request.url).isNotEmpty()) parseJsonToTweet(response.body(), request.url)[0] else throw EmptyTweetException()
        } else {
            throw EmptyTweetException()
        }
    }

    /**
     * 推荐获取推文方式
     * 优先获取缓存中的推文, 若缓存中推文过时则获取新推文
     *
     * 注意: 在重试时抛出的错误将会一并从该方法抛出 (除了超时和达到 API 调用上限)
     *
     * @return 推文, 可空
     */
    fun getCachedTweet(username: String, index: Int = 0, max: Int = 5): Tweet? {
        val startTime = LocalDateTime.now()
        var tweet: Tweet? = null

        if (index < 0 || max <= index) {
            return null
        }

        val exception = TaskUtil.executeWithRetry(1) {
            try {
                val cachedTweet = cacheTweet[username]
                val result: Tweet?

                result = if (cachedTweet != null && Duration.between(cachedTweet.getSentTime(), LocalDateTime.now())
                        .toMinutes() <= 2
                ) {
                    cachedTweet
                } else {
                    val list = getUserTweets(username, max)
                    if (list.isNotEmpty()) list[index] else throw EmptyTweetException("返回的推文列表为空")
                }

                logger.debug("[蓝鸟] 查询用户最新推文耗时 ${Duration.between(startTime, LocalDateTime.now()).toMillis()}ms")
                tweet = result
            } catch (x: TwitterApiException) {
                logger.warning("[蓝鸟] 调用 API 时出现了问题", x)
            }
        }

        if (exception != null) throw exception

        return tweet
    }

    /**
     * 添加缓存推文
     * 将推文放入缓存池中
     */
    fun addCacheTweet(username: String, tweet: Tweet) {
        cacheTweet[username] = tweet
    }

    /**
     * 将 json 解析为推文实体
     * 支持多个推文和单个推文 (以 List 形式返回)
     *
     * @return 推文列表
     */
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

    override fun isReachLimit(): Boolean {
        return usedTime >= getLimitTime()
    }

    /** Twitter API: 1500次/15min, 10w次/24h */
    override fun getLimitTime(): Int = 1000
}