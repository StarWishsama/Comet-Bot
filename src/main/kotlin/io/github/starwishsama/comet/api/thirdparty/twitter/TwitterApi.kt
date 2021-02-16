package io.github.starwishsama.comet.api.thirdparty.twitter

import cn.hutool.http.ContentType
import com.github.salomonbrys.kotson.fromJson
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.roxstudio.utils.CUrl
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.BotVariables.logger
import io.github.starwishsama.comet.BotVariables.nullableGson
import io.github.starwishsama.comet.api.thirdparty.ApiExecutor
import io.github.starwishsama.comet.api.thirdparty.twitter.data.Tweet
import io.github.starwishsama.comet.api.thirdparty.twitter.data.TwitterUser
import io.github.starwishsama.comet.exceptions.EmptyTweetException
import io.github.starwishsama.comet.exceptions.RateLimitException
import io.github.starwishsama.comet.exceptions.TwitterApiException
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
    private const val twitterApiUrl = "https://api.twitter.com/1.1"

    // 获取 token 地址
    private const val twitterTokenGetUrl = "https://api.twitter.com/oauth2/token"

    // Bearer Token
    private var token = BotVariables.cfg.twitterToken

    // Api 调用次数
    override var usedTime: Int = 0
    override val duration: Int = 5

    private const val apiReachLimit = "已达到 Twitter API 调用上限"

    private fun checkToken() {
        if (token == null) getBearerToken()
    }

    private val cacheTweet = mutableSetOf<Tweet>()

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
                BotVariables.cfg.twitterToken = token
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
       NetUtil.executeHttpRequest(
                url = url,
                timeout = 5,
                call = {
                    header("authorization", "Bearer $token")
                }
        ).use { conn ->

            var bodyCopy = ""

            try {
                val result = conn.body

                val body = result?.string()
                if (body != null) {
                    bodyCopy = body
                }

                return nullableGson.fromJson(bodyCopy)
            } catch (e: IOException) {
                if (!NetUtil.isTimeout(e)) {
                    FileUtil.createErrorReportFile(type = "data", t = e, content = bodyCopy, message = "Request URL: $url")
                } else {
                    daemonLogger.verboseS("[蓝鸟] 在获取用户信息时连接超时")
                }
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

        NetUtil.executeHttpRequest(
                url = "$twitterApiUrl/statuses/user_timeline.json?screen_name=$username&count=${count}&tweet_mode=extended",
                timeout = 5,
                call = {
                    header("authorization", "Bearer $token")
                    header("content-type", "application/json;charset=utf-8")
                }
        ).use { request ->
            if (request.isSuccessful) {
                val tweetList = parseJsonToTweet(
                    request.body?.string()
                        ?: return emptyList(), request.request.url.toString())
                return if (tweetList.isNotEmpty()) {
                    tweetList.sortedByDescending { it.getSentTime() }
                } else {
                    emptyList()
                }
            }
        }

        return emptyList()
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

        NetUtil.executeHttpRequest(
                url = "$twitterApiUrl/statuses/show.json?id=$id&tweet_mode=extended",
                timeout = 5,
                call = {
                    header("authorization", "Bearer $token")
                }
        ).use { request ->
            return if (request.isSuccessful && request.isType(ContentType.JSON.value)) {
                val tweet = parseJsonToTweet(
                    request.body?.string()
                        ?: return null, request.request.url.toString())
                if (tweet.isNotEmpty()) {
                    addCacheTweet(tweet[0])
                    tweet[0]
                } else {
                    throw EmptyTweetException()
                }
            } else {
                null
            }
        }
    }

    /**
     * 获取推特用户时间线
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
    fun getTweetInTimeline(username: String, index: Int = 0, max: Int = 5): Tweet? {
        val startTime = LocalDateTime.now()

        if (index < 0 || max <= index) {
            return null
        }

        val result: Tweet?

        val list = getUserTweets(username, max)
        result = if (list.isNotEmpty()) {
            addCacheTweet(list[index])
            list[index]
        } else {
            null
        }

        logger.verboseS("[蓝鸟] 查询用户最新推文耗时 ${Duration.between(startTime, LocalDateTime.now()).toMillis()}ms")

        return result
    }

    private fun addCacheTweet(tweet: Tweet) {
        if (getCacheByID(tweet.id) == null) {
            cacheTweet.add(tweet)
        }
    }

    /**
     * 通过推文 ID 获取缓存推文
     *
     * @param id 推文 ID
     * @return 推文, 找不到时返回空
     */
    fun getCacheByID(id: Long): Tweet? {
        for (tweet in cacheTweet) {
            if (tweet.id == id) {
                return tweet
            }
        }

        return null
    }

    /**
     * 将 json 解析为推文实体
     * 支持多个推文和单个推文 (以链表形式返回)
     *
     * @param json 从 Twitter API 中获取到的推文 json
     * @param url 请求解析 json 的推文, 用于创建错误报告
     *
     * @return 推文列表
     */
    private fun parseJsonToTweet(json: String, url: String): List<Tweet> {
        return try {
            listOf(nullableGson.fromJson(json, Tweet::class.java))
        } catch (e: JsonSyntaxException) {
            try {
                nullableGson.fromJson(json)
            } catch (e: RuntimeException) {
                FileUtil.createErrorReportFile("在解析推文时出现了问题", "tweet", e, json, url)
                return emptyList()
            }
        }
    }

    override fun isReachLimit(): Boolean {
        return usedTime >= getLimitTime()
    }

    /** Twitter API: 1500次/15min, 10w次/24h */
    override fun getLimitTime(): Int = 1000
}