package io.github.starwishsama.comet.api.thirdparty.youtube

import cn.hutool.http.HttpException
import com.github.salomonbrys.kotson.fromJson
import com.google.gson.JsonSyntaxException
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.api.thirdparty.ApiExecutor
import io.github.starwishsama.comet.api.thirdparty.youtube.data.SearchChannelResult
import io.github.starwishsama.comet.api.thirdparty.youtube.data.SearchVideoResult
import io.github.starwishsama.comet.api.thirdparty.youtube.data.VideoType
import io.github.starwishsama.comet.api.thirdparty.youtube.data.YoutubeRequestError
import io.github.starwishsama.comet.exceptions.ApiKeyIsEmptyException
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import io.github.starwishsama.comet.utils.network.NetUtil

/**
 * FIXME: 需要重构
 */
object YoutubeApi : ApiExecutor {
    // 100 Unit
    private var searchApi = "https://www.googleapis.com/youtube/v3/search?order=date&part=snippet,"
    private const val searchByUserName = "contentDetails,statistics&forUsername="
    // 1 Unit
    private const val channelGetApi = "https://youtube.googleapis.com/youtube/v3/channels?part=snippet%2CcontentDetails%2Cstatistics&id="
    private const val maxResult = "&maxResults="
    private var init = false

    /**
     * 初始化 API 的 APIKey
     */
    private fun init() {
        if (BotVariables.cfg.youtubeApiKey.isNotEmpty()) {
            searchApi =
                    "https://www.googleapis.com/youtube/v3/search?key=${BotVariables.cfg.youtubeApiKey}&order=date&part=snippet,"
        }
        init = true
    }

    /**
     * 通过 Youtube 频道 ID 获取频道信息
     */
    @Throws(ApiKeyIsEmptyException::class, HttpException::class)
    fun getChannelByID(channelId: String): SearchChannelResult? {
        if (!init) init()

        if (!searchApi.contains("key")) throw ApiKeyIsEmptyException("Youtube")

        NetUtil.executeHttpRequest(channelGetApi + channelId + "&key=${BotVariables.cfg.youtubeApiKey}").use {
            if (it.isSuccessful) {
                val body = it.body?.string() ?: return null
                try {
                    return BotVariables.nullableGson.fromJson(body)
                } catch (e: JsonSyntaxException) {
                    try {
                        val error = BotVariables.nullableGson.fromJson(body, YoutubeRequestError::class.java)
                        BotVariables.logger.warning("[YTB] 无法访问 API \n返回码: ${error.error.code}, 信息: ${error.error.message}")
                    } catch (e: JsonSyntaxException) {
                        BotVariables.logger.warning("[YTB] 无法解析 API 传入的 json", e)
                    }
                }
            }
        }

        return null
    }

    /**
     * 获取频道下的所有视频
     */
    @Throws(ApiKeyIsEmptyException::class, HttpException::class)
    fun getChannelVideos(channelId: String, count: Int = 5): SearchVideoResult? {
        if (!init) init()

        if (!searchApi.contains("key")) throw ApiKeyIsEmptyException("Youtube")

        NetUtil.executeHttpRequest(
                url = "${searchApi}id&channelId=${channelId}$maxResult${count}",
                timeout = 5
        ).use { response ->

            if (response.isSuccessful) {
                val body = response.body?.string() ?: return null
                /** @TODO 类型自动选择, 类似于 BiliBili 的动态解析 */
                try {
                    return BotVariables.nullableGson.fromJson(body, SearchVideoResult::class.java)
                } catch (e: JsonSyntaxException) {
                    try {
                        val error = BotVariables.nullableGson.fromJson(body, YoutubeRequestError::class.java)
                        BotVariables.logger.warning("[YTB] 无法访问 API \n返回码: ${error.error.code}, 信息: ${error.error.message}")
                    } catch (e: JsonSyntaxException) {
                        BotVariables.logger.warning("[YTB] 无法解析 API 传入的 json", e)
                    }
                }
            }
        }

        return null
    }

    /**
     * 获取直播实体类
     */
    fun SearchVideoResult.getLiveItemOrNull(): SearchVideoResult.SearchResultItem? {
        val items = items
        items.forEach { item ->
            if (item.snippet.getType() == VideoType.STREAMING) {
                return item
            }
        }
        return null
    }

    /**
     * 获取直播状态
     */
    fun getLiveStatus(result: SearchVideoResult): Boolean {
        val items = result.items
        items.forEach { item ->
            if (item.snippet.getType() == VideoType.STREAMING) {
                return true
            }
        }
        return false
    }

    /**
     * 获取直播状态, 并将其转换为 [MessageWrapper]
     */
    fun getLiveStatusAsMessage(channelId: String): MessageWrapper? {
        val result = getChannelVideos(channelId, 5)
        if (result != null) {
            return getLiveStatusByResult(result)
        }

        return MessageWrapper().addText("找不到对应ID的频道")
    }

    /**
     * 通过 [SearchVideoResult] 获取直播状态, 并将其转换为 [MessageWrapper]
     */
    fun getLiveStatusByResult(result: SearchVideoResult?): MessageWrapper {
        if (result == null) return MessageWrapper().addText("无直播数据").setUsable(false)

        val items = result.items
        items.forEach { item ->
            run {
                if (item.snippet.getType() == VideoType.STREAMING) {
                    return MessageWrapper().addText("""${item.snippet.channelTitle} 正在直播!
直播标题: ${item.snippet.videoTitle}
直播时间: ${item.snippet.publishTime}
直达链接: ${item.getVideoUrl()}""")
                            .addPictureByURL(item.snippet.getCoverImgUrl())
                } else if (item.snippet.getType() == VideoType.UPCOMING) {
                    return MessageWrapper().addText("""
${item.snippet.channelTitle} 有即将进行的直播!
直播标题: ${item.snippet.videoTitle}
开播时间请打开查看 ${item.getVideoUrl()}
""").addPictureByURL(item.snippet.getCoverImgUrl())
                }
            }
        }

        return MessageWrapper().addText("${result.items[0].snippet.channelTitle} 最近没有直播哦").setUsable(false)
    }

    override var usedTime: Int = 0
    override val duration: Int = 24

    override fun isReachLimit(): Boolean = usedTime > getLimitTime()

    override fun getLimitTime(): Int = 10000
}