package ren.natsuyuk1.comet.network.thirdparty.twitter

import mu.KotlinLogging
import ren.natsuyuk1.setsuna.api.fetchTweet
import ren.natsuyuk1.setsuna.api.fetchUser
import ren.natsuyuk1.setsuna.api.fetchUserByUsername
import ren.natsuyuk1.setsuna.api.getUserTimeline
import ren.natsuyuk1.setsuna.objects.tweet.Tweet
import ren.natsuyuk1.setsuna.objects.user.TwitterUser

private val logger = KotlinLogging.logger {}

object TwitterAPI {
    suspend fun fetchTweet(id: String): Tweet? = kotlin.runCatching {
        val resp = client.fetchTweet(id)

        return if (resp.errors != null) {
            logger.warn { "获取推文 ($id) 失败, ${resp.errors}" }
            null
        } else {
            resp.tweet
        }
    }.onFailure {
        logger.warn(it) { "获取推文 ($id) 失败" }
    }.getOrNull()

    suspend fun fetchTimeline(userID: String): List<Tweet>? = kotlin.runCatching {
        val resp = client.getUserTimeline(userID)

        return if (resp.errors != null) {
            logger.warn { "获取用户 ($userID) 的时间线失败, ${resp.errors}" }
            null
        } else {
            resp.tweets
        }
    }.onFailure {
        logger.warn(it) { "获取用户 ($userID) 的时间线失败" }
    }.getOrNull()

    suspend fun fetchUserByUsername(username: String): TwitterUser? = kotlin.runCatching {
        val resp = client.fetchUserByUsername(username)

        return if (resp.errors != null) {
            logger.warn { "获取推特用户 ($username) 失败, ${resp.errors}" }
            null
        } else {
            resp.user
        }
    }.onFailure {
        logger.warn(it) { "获取推特用户 ($username) 失败" }
    }.getOrNull()

    suspend fun fetchUser(id: String): TwitterUser? = kotlin.runCatching {
        val resp = client.fetchUser(id)

        return if (resp.errors != null) {
            logger.warn { "获取推特用户 ($id) 失败, ${resp.errors}" }
            null
        } else {
            resp.user
        }
    }.onFailure {
        logger.warn(it) { "获取推特用户 ($id) 失败" }
    }.getOrNull()
}
