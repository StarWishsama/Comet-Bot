package io.github.starwishsama.comet.api.youtube

import cn.hutool.http.HttpException
import com.google.gson.JsonSyntaxException
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.api.ApiExecutor
import io.github.starwishsama.comet.exceptions.ApiKeyIsEmptyException
import io.github.starwishsama.comet.objects.WrappedMessage
import io.github.starwishsama.comet.objects.pojo.youtube.SearchVideoResult
import io.github.starwishsama.comet.objects.pojo.youtube.VideoType
import io.github.starwishsama.comet.objects.pojo.youtube.YoutubeRequestError
import io.github.starwishsama.comet.utils.NetUtil

object YoutubeApi : ApiExecutor {
    private var searchApi = "https://www.googleapis.com/youtube/v3/search?order=date&part=snippet,"
    private const val searchByUserName = "contentDetails,statistics&forUsername="
    private const val maxResult = "&maxResults="
    private var init = false

    private fun init() {
        if (BotVariables.cfg.youtubeApiKey.isNotEmpty()) {
            searchApi =
                "https://www.googleapis.com/youtube/v3/search?key=${BotVariables.cfg.youtubeApiKey}&order=date&part=snippet,"
        }
        init = true
    }

    @Throws(ApiKeyIsEmptyException::class, HttpException::class)
    fun getChannelVideos(channelId: String, count: Int = 5): SearchVideoResult? {
        if (!init) init()
        if (!searchApi.contains("key")) throw ApiKeyIsEmptyException("Youtube")

        val request = NetUtil.doHttpRequestGet("${searchApi}id&channelId=${channelId}$maxResult${count}", 5000)

        val response = request.executeAsync()
        if (response.isOk) {
            val body = response.body()
            /** @TODO 类型自动选择, 类似于 BiliBili 的动态解析 */
            try {
                return BotVariables.gson.fromJson(body, SearchVideoResult::class.java)
            } catch (e: JsonSyntaxException) {
                try {
                    val error = BotVariables.gson.fromJson(body, YoutubeRequestError::class.java)
                    BotVariables.logger.warning("[YTB] 无法访问 API \n返回码: ${error.code}, 信息: ${error.message}")
                } catch (e: JsonSyntaxException) {
                    BotVariables.logger.warning("[YTB] 无法解析 API 传入的 json", e)
                }
            }
        }

        return null
    }

    fun SearchVideoResult.getLiveItemOrNull(): SearchVideoResult.SearchResultItem? {
        val items = items
        items.forEach { item ->
            run {
                if (item.snippet.getType() == VideoType.STREAMING) return item
            }
        }
        return null
    }

    fun getLiveStatus(result: SearchVideoResult): Boolean {
        val items = result.items
        items.forEach { item ->
            run {
                if (item.snippet.getType() == VideoType.STREAMING) return true
            }
        }
        return false
    }

    fun getLiveStatusAsMessage(channelId: String): WrappedMessage? {
        val result = getChannelVideos(channelId, 5)
        if (result != null) {
            return getLiveStatusByResult(result)
        }

        return WrappedMessage("找不到对应ID的频道")
    }

    fun getLiveStatusByResult(result: SearchVideoResult?): WrappedMessage {
        if (result == null) return WrappedMessage("找不到对应ID的频道")

        val items = result.items
        items.forEach { item ->
            run {
                if (item.snippet.getType() == VideoType.STREAMING) {
                    return WrappedMessage("""${item.snippet.channelTitle} 正在直播!
                                                  直播标题: ${item.snippet.videoTitle}
                                                  直播时间: ${item.snippet.publishTime}
                                                  直达链接: ${item.getVideoUrl()}""")
                            .plusImageUrl(item.snippet.getCoverImgUrl())
                } else if (item.snippet.getType() == VideoType.UPCOMING) {
                    return WrappedMessage("""
                                                            ${item.snippet.channelTitle} 有即将进行的直播!
                                                            直播标题: ${item.snippet.videoTitle}
                                                            开播时间请打开查看 ${item.getVideoUrl()}
                                                        """.trimIndent()).plusImageUrl(item.snippet.getCoverImgUrl())
                }
            }
        }

        return WrappedMessage("${result.items[0].snippet.channelTitle} 最近没有直播哦")
    }

    override var usedTime: Int = 0

    override fun isReachLimit(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getLimitTime(): Int {
        TODO("Not yet implemented")
    }
}