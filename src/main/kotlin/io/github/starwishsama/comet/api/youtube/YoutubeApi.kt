package io.github.starwishsama.comet.api.youtube

import cn.hutool.http.HttpException
import com.google.gson.JsonSyntaxException
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.api.ApiExecutor
import io.github.starwishsama.comet.exceptions.ApiKeyIsEmptyException
import io.github.starwishsama.comet.objects.WrappedMessage
import io.github.starwishsama.comet.objects.pojo.youtube.SearchUserResult
import io.github.starwishsama.comet.objects.pojo.youtube.SearchVideoResult
import io.github.starwishsama.comet.objects.pojo.youtube.VideoType
import io.github.starwishsama.comet.objects.pojo.youtube.YoutubeRequestError
import io.github.starwishsama.comet.utils.FileUtil
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
    fun searchYoutubeUser(channelName: String, count: Int): SearchUserResult? {
        if (!init) init()

        if (!searchApi.contains("key")) throw ApiKeyIsEmptyException("Youtube")

        val request = NetUtil.doHttpRequestGet("$searchApi$searchByUserName$channelName$maxResult$count", 5000)

        val response = request.executeAsync()

        if (response.isOk) {
            val body = response.body()
            /** @TODO 类型自动选择, 类似于 BiliBili 的动态解析 */
            try {
                return BotVariables.gson.fromJson(body, SearchUserResult::class.java)
            } catch (e: JsonSyntaxException) {
                try {
                    val error = BotVariables.gson.fromJson(body, YoutubeRequestError::class.java)
                    BotVariables.logger.warning("[API] 无法访问 Youtube API \n返回码: ${error.code}, 信息: ${error.message}")
                } catch (t: Throwable) {
                    BotVariables.logger.warning("[API] 无法解析 Youtube API 传入的 json", t)
                    FileUtil.createErrorReportFile("youtube", t, body, request.url)
                }
            }
        }

        return null
    }

    @Throws(ApiKeyIsEmptyException::class, HttpException::class)
    fun getChannelVideos(channelId: String, count: Int): SearchVideoResult? {
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

    fun getLiveStatus(channelId: String): WrappedMessage? {
        try {
            val result = getChannelVideos(channelId, 5)
            if (result != null) {
                val items = result.items
                items.forEach { item ->
                    run {
                        if (item.snippet.getType() == VideoType.STREAMING) {
                            return item.snippet.getCoverImgUrl()?.let { WrappedMessage(item.snippet.videoTitle + item.snippet.desc).plusImageUrl(it) }
                        }
                    }
                }
            }
        } catch (e: ApiKeyIsEmptyException) {
            return WrappedMessage("${e.message}")
        }
        return null
    }

    override var usedTime: Int = 0

    override fun isReachLimit(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getLimitTime(): Int {
        TODO("Not yet implemented")
    }
}