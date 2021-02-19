package io.github.starwishsama.comet.api.thirdparty.youtube

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BotVariables.cfg
import io.github.starwishsama.comet.api.thirdparty.ApiExecutor
import io.github.starwishsama.comet.api.thirdparty.youtube.data.SearchChannelResult
import io.github.starwishsama.comet.api.thirdparty.youtube.data.SearchVideoResult
import io.github.starwishsama.comet.api.thirdparty.youtube.data.VideoType
import io.github.starwishsama.comet.exceptions.RateLimitException
import io.github.starwishsama.comet.objects.push.YoutubeUser
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import org.jsoup.Jsoup
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * FIXME: 需要重构
 */
object YoutubeApi : ApiExecutor {
    val service: IYoutubeApi
    get() {
        if (!isReachLimit()) {
            usedTime++
            return field
        } else {
            throw RateLimitException("Youtube API 调用已达上限")
        }
    }

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://youtube.googleapis.com/youtube/v3/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(BotVariables.client)
            .build()

        service = retrofit.create(IYoutubeApi::class.java)
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
    fun getLiveStatusAsMessage(channelId: String): MessageWrapper {
        val result = service.getSearchResult(channelId = channelId, maxResult = 5).execute().body()
        if (result != null) {
            return getLiveStatusByResult(result)
        }

        return MessageWrapper().addText("找不到对应ID的频道").setUsable(false)
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

    fun getLiveStatusByPage(youtubeUser: YoutubeUser): MessageWrapper {
        val url = "https://www.youtube.com/channel/${youtubeUser.id}/live"

        try {
            val page = Jsoup.connect(url).apply {
                if (cfg.proxySwitch) {
                    proxy(cfg.proxyUrl, cfg.proxyPort)
                }
            }

            return if (page.execute().body().contains("isLive: true")) {
                val doc = page.get()
                val streamTitle = doc.title()
                MessageWrapper().addText("""${youtubeUser.userName} 正在直播!
直播标题: $streamTitle
直达链接: https://www.youtube.com/channel/${youtubeUser.id}/live""")
            } else {
                MessageWrapper().addText("当前没有在直播").setUsable(false)
            }

        } catch (e: Exception) {
            return MessageWrapper().addText("在获取直播信息时发生异常").setUsable(false)
        }
    }

    override var usedTime: Int = 0
    override val duration: Int = 24

    override fun isReachLimit(): Boolean = usedTime > getLimitTime()

    override fun getLimitTime(): Int = 10000
}

interface IYoutubeApi {
    @GET("/channels")
    fun getSearchResult(
        @Query("part") part: String = "snippet%2CcontentDetails%2Cstatistics",
        @Query("id") channelId: String,
        @Query("maxResults") maxResult: Int = 5,
        @Query("key") token: String = cfg.youtubeApiKey
    ): Call<SearchVideoResult>

    @GET("/channels")
    fun getChannelResult(
        @Query("part") part: String = "snippet%2CcontentDetails%2Cstatistics",
        @Query("id") channelId: String,
        @Query("key") token: String = cfg.youtubeApiKey
    ): Call<SearchChannelResult>
}
