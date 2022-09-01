package ren.natsuyuk1.comet.network.thirdparty.twitter

import mu.KotlinLogging
import ren.natsuyuk1.setsuna.api.fetchTweet
import ren.natsuyuk1.setsuna.api.fetchUser
import ren.natsuyuk1.setsuna.api.fetchUserByUsername
import ren.natsuyuk1.setsuna.api.getUserTimeline
import ren.natsuyuk1.setsuna.api.options.Expansions
import ren.natsuyuk1.setsuna.api.options.defaultTweetOption
import ren.natsuyuk1.setsuna.api.options.defaultTwitterOption
import ren.natsuyuk1.setsuna.api.options.defaultUserOption
import ren.natsuyuk1.setsuna.objects.user.TwitterUser
import ren.natsuyuk1.setsuna.response.TweetFetchResponse
import ren.natsuyuk1.setsuna.response.UserTimelineResponse

private val logger = KotlinLogging.logger {}

object TwitterAPI {
    suspend fun fetchTweet(id: String): TweetFetchResponse? = kotlin.runCatching {
        val resp = client.fetchTweet(id, defaultTwitterOption + Expansions.Media())

        return if (resp.errors != null) {
            logger.warn { "获取推文 ($id) 失败, ${resp.errors}" }
            null
        } else {
            resp
        }
    }.onFailure {
        logger.warn(it) { "获取推文 ($id) 失败" }
    }.getOrNull()

    suspend fun fetchTimeline(userID: String): UserTimelineResponse? = kotlin.runCatching {
        val resp = client.getUserTimeline(userID, defaultUserOption + defaultTweetOption + Expansions.Media())

        return if (resp.errors != null) {
            logger.warn { "获取用户 ($userID) 的时间线失败, ${resp.errors}" }
            null
        } else {
            resp
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
