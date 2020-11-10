package io.github.starwishsama.comet.api.thirdparty.twitter

import cn.hutool.http.ContentType
import com.github.salomonbrys.kotson.fromJson
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.roxstudio.utils.CUrl
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.BotVariables.gson
import io.github.starwishsama.comet.BotVariables.logger
import io.github.starwishsama.comet.api.thirdparty.ApiExecutor
import io.github.starwishsama.comet.exceptions.EmptyTweetException
import io.github.starwishsama.comet.exceptions.RateLimitException
import io.github.starwishsama.comet.exceptions.TwitterApiException
import io.github.starwishsama.comet.objects.pojo.twitter.Tweet
import io.github.starwishsama.comet.objects.pojo.twitter.TwitterUser
import io.github.starwishsama.comet.utils.FileUtil
import io.github.starwishsama.comet.utils.network.NetUtil
import io.github.starwishsama.comet.utils.network.isType
import io.github.starwishsama.comet.utils.verboseS
import java.io.IOException
import java.time.Duration
import java.time.LocalDateTime

/**
 * Twitter API
 *
 * 支持获取蓝鸟用户信息 | 最新推文 | 推主时间线上推文
 *
 * FIXME: 整理一下代码结构
 *
 * @author Nameless
 */
object TwitterApi : ApiExecutor {
    // 蓝鸟 APIv1.1 地址
    private const val twitterApiUrl = "https://api.twitter.com/1.1/"

    // 获取 token 地址
    private const val twitterTokenGetUrl = "https://api.twitter.com/oauth2/token"

    // Bearer Token
    private var token = BotVariables.cfg.twitterToken

    // 缓存的推文
    private var cacheTweet = mutableMapOf<String, Tweet>()

    // Api 调用次数
    override var usedTime: Int = 0

    private const val apiReachLimit = "已达到 Twitter API 调用上限"

    private fun checkToken() {
        if (token == null) getBearerToken()
    }

    /**
     * 获取用于调用 Twitter API 的 Bearer Token
     *
     * 获取成功后可以在 [token] 下获取
     */
    private fun getBearerToken() {
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
                logger.info("[蓝鸟] 成功获取 Access Token")
            }
        } catch (e: IOException) {
            logger.warning("获取 Token 时出现问题", e)
        }
    }

    /**
     * 获取推主的账号信息
     *
     * @param username 推特用户的用户名
     *
     * @throws RateLimitException
     * @throws TwitterApiException
     * @return 账号信息
     */
    @Throws(RateLimitException::class, TwitterApiException::class)
    fun getUserProfile(username: String): TwitterUser? {
        checkToken()
        checkRateLimit(apiReachLimit)

        usedTime++

        val startTime = LocalDateTime.now()
        val url = "$twitterApiUrl/users/show.json?screen_name=$username&tweet_mode=extended"
        val conn = NetUtil.doHttpRequestGet(url, 5_000)
                .header("authorization", "Bearer $token")
        var bodyCopy = ""

        try {
            val result = conn.executeAsync()

            val body = result.body()
            bodyCopy = body

            return gson.fromJson(result.body())
        } catch (e: IOException) {
            if (!NetUtil.isTimeout(e)) {
                FileUtil.createErrorReportFile(type = "twitter", t = e, content = bodyCopy, message = "Request URL: $url")
            } else {
                daemonLogger.verboseS("[蓝鸟] 在获取用户信息时连接超时")
            }
        }

        daemonLogger.verboseS("[蓝鸟] 查询用户信息耗时 ${Duration.between(startTime, LocalDateTime.now()).toMillis()}ms")
        return null
    }

    /**
     * 获取用户时间线上的推文, 最高可获取 3200 条
     *
     * @param username 推特用户的用户名
     * @param count 获取时间线上推文的条数
     *
     * @return 推文列表
     */
    @Throws(RateLimitException::class, EmptyTweetException::class, TwitterApiException::class)
    fun getUserTweets(username: String, count: Int): List<Tweet> {
        checkToken()
        checkRateLimit(apiReachLimit)

        if (count >= 3200) throw UnsupportedOperationException("最多只能获取时间线上3200条推文")

        usedTime++
        val request =
                NetUtil.doHttpRequestGet(
                        "$twitterApiUrl/statuses/user_timeline.json?screen_name=$username&count=${count}&tweet_mode=extended",
                        5_000)
                        .header("authorization", "Bearer $token")
                        .header("content-type", "application/json;charset=utf-8")

        val result = request.execute()
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
     * @param id 推文 ID
     *
     * @throws RateLimitException
     * @throws EmptyTweetException
     * @return 推文
     */
    @Throws(RateLimitException::class)
    fun getTweetById(id: Long): Tweet? {
        checkToken()
        checkRateLimit(apiReachLimit)

        val request = NetUtil.doHttpRequestGet("$twitterApiUrl/statuses/show.json?id=$id&tweet_mode=extended", 5_000).header("authorization", "Bearer $token")
        val response = request.executeAsync()

        return if (response.isOk && response.isType(ContentType.JSON.value)) {
            val tweet = parseJsonToTweet(response.body(), request.url)
            if (tweet.isNotEmpty()) {
                tweet[0]
            } else {
                throw EmptyTweetException()
            }
        } else {
            null
        }
    }

    /**
     * 获取单条推文
     *
     * 优先获取缓存中的推文, 若缓存中推文过时则获取新推文
     *
     * 注意: 在重试时抛出的错误将会一并从该方法抛出
     *
     * @param username 推特用户的用户名
     * @param index 获取时间线上的第几条推文 (按照时间顺序排序)
     * @param max 获取推文条数上限
     *
     * @return 推文, 若获取失败则返回空值
     */
    fun getTweetInTimeline(username: String, index: Int = 0, max: Int = 5, needCache: Boolean = true): Tweet? {
        val startTime = LocalDateTime.now()
        val isCache: Boolean

        if (index < 0 || max <= index) {
            return null
        }

        val cachedTweet = getCacheTweet(username)
        val result: Tweet? = if (cachedTweet != null && Duration.between(cachedTweet.getSentTime(), LocalDateTime.now())
                        .toMinutes() <= 1
        ) {
            isCache = true
            cachedTweet
        } else {
            isCache = false
            val list = getUserTweets(username, max)
            if (list.isNotEmpty()) {
                // 只对获取最新推文时才缓存
                if (needCache && index == 0) {
                    addCacheTweet(username, list[index])
                }
                list[index]
            } else {
                throw EmptyTweetException("返回的推文列表为空")
            }
        }

        if (!isCache) logger.verboseS("[蓝鸟] 查询用户最新推文耗时 ${Duration.between(startTime, LocalDateTime.now()).toMillis()}ms")

        return result
    }

    /**
     * 添加缓存推文
     * 将推文放入缓存池中
     *
     * @param username 推特用户的用户名
     * @param tweet 推文实体
     */
    private fun addCacheTweet(username: String, tweet: Tweet) {
        cacheTweet[username] = tweet
    }

    /**
     * 将 json 解析为推文实体
     * 支持多个推文和单个推文 (以链表形式返回)
     *
     * @param json 从 Twitter API 中获取到的推文 json
     * @param url 请求解析 json 的来源网站, 用于创建错误报告
     *
     * @return 推文列表
     */
    private fun parseJsonToTweet(json: String, url: String): List<Tweet> {
        return try {
            listOf(gson.fromJson(json, Tweet::class.java))
        } catch (e: JsonSyntaxException) {
            try {
                gson.fromJson(json)
            } catch (e: RuntimeException) {
                FileUtil.createErrorReportFile("在解析推文时出现了问题", "tweet", e, json, url)
                return emptyList()
            }
        }
    }

    fun getCacheTweet(username: String): Tweet? = cacheTweet[username]

    override fun isReachLimit(): Boolean {
        return usedTime >= getLimitTime()
    }

    /** Twitter API: 1500次/15min, 10w次/24h */
    override fun getLimitTime(): Int = 1000
}