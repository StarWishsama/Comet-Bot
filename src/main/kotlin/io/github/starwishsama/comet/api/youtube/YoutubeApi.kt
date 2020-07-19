package io.github.starwishsama.comet.api.youtube

import cn.hutool.http.HttpException
import com.google.gson.JsonSyntaxException
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.Comet
import io.github.starwishsama.comet.exceptions.ApiKeyIsEmptyException
import io.github.starwishsama.comet.objects.WrappedMessage
import io.github.starwishsama.comet.objects.pojo.youtube.SearchResult
import io.github.starwishsama.comet.objects.pojo.youtube.VideoType
import io.github.starwishsama.comet.objects.pojo.youtube.YoutubeRequestError
import io.github.starwishsama.comet.utils.NetUtil
import java.net.Proxy
import java.net.Socket


object YoutubeApi {
    private var channelInfoUrl = "https://www.googleapis.com/youtube/v3/search?part=snippet,id&order=date"

    init {
        if (BotVariables.cfg.youtubeApiKey.isNotEmpty()) {
            channelInfoUrl = "https://www.googleapis.com/youtube/v3/search?part=snippet,id&order=date&key=${BotVariables.cfg.youtubeApiKey}"
        }
    }

    @Throws(ApiKeyIsEmptyException::class, HttpException::class)
    fun getChannelVideos(channelId: String, count: Int): SearchResult? {
        if (!channelInfoUrl.contains("&key")) throw ApiKeyIsEmptyException("Youtube")

        val request = NetUtil.doHttpRequest("${channelInfoUrl}&channelId=${channelId}&maxResults=${count}", 5000)

        /**
         * @TODO 重构设置代理这一步, 可以复用
         */
        if (BotVariables.cfg.proxyUrl != null && BotVariables.cfg.proxyPort != 0) {
            request.setProxy(
                    Proxy(
                            Proxy.Type.HTTP,
                            Socket(BotVariables.cfg.proxyUrl, BotVariables.cfg.proxyPort).remoteSocketAddress
                    )
            )
        }

        val response = request.executeAsync()
        if (response.isOk) {
            val body = response.body()
            /** @TODO 类型自动选择, 类似于 BiliBili 的动态解析 */
            try {
                return BotVariables.gson.fromJson(body, SearchResult::class.java)
            } catch (e: JsonSyntaxException) {
                try {
                    val error = BotVariables.gson.fromJson(body, YoutubeRequestError::class.java)
                    Comet.logger.error("[YTB] 无法访问 API \n返回码: ${error.code}, 信息: ${error.message}")
                } catch (e: JsonSyntaxException) {
                    Comet.logger.error("[YTB] 无法解析 API 传入的 json", e)
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
}