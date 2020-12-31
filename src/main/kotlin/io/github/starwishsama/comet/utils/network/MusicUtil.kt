package io.github.starwishsama.comet.utils.network

import cn.hutool.http.HttpRequest
import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.get
import com.google.gson.*
import com.google.gson.annotations.SerializedName
import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.BotVariables.logger
import io.github.starwishsama.comet.utils.CometUtil.sendMessage
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.LightApp
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.asMessageChain
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.io.InputStream
import java.net.URLEncoder

/**
 * 搜索音乐工具类
 *
 * FIXME: 将搜索结果转换为 Bean, 减少繁琐的解析反解析
 */
object MusicUtil {
    /** 1分钟100次，10分钟500次，1小时2000次 */
    private const val api4qq = "https://api.qq.jsososo.com/song/urls?id="
    private const val api4NetEase = "https://musicapi.leanapp.cn"
    private val gson = GsonBuilder().serializeNulls().setLenient().disableHtmlEscaping().create()
    private const val versionCode = ".0.0.1"

    fun searchNetEaseMusic(songName: String): MessageChain {
        try {
            val searchMusicResult = NetUtil.getPageContent("$api4NetEase/search?keywords=${URLEncoder.encode(songName, "UTF-8")}")

            if (searchMusicResult?.isNotEmpty() == true) {
                val searchResult = JsonParser.parseString(searchMusicResult)
                if (searchResult.isJsonObject) {
                    val musicId = searchResult.asJsonObject["result"].asJsonArray["songs"][0]["id"].asInt
                    val musicUrl = "https://music.163.com/#/song?id=$musicId"
                    val songResult = NetUtil.getPageContent("http://$api4NetEase/song/detail?ids=$musicId")
                    if (songResult?.isNotEmpty() == true) {
                        val songJson = JsonParser.parseString(songResult)
                        if (songJson.isJsonObject) {
                            val albumUrl = songJson.asJsonObject["songs"].asJsonArray[0].asJsonObject["al"]["picUrl"].asString
                            val name = songJson.asJsonObject["songs"].asJsonArray[0].asJsonObject["name"].asString
                            var artistName = ""

                            songJson.asJsonObject["songs"].asJsonArray[0].asJsonObject["ar"].asJsonArray.forEach {
                                run {
                                    artistName += (it.asJsonObject["name"].asString + "/")
                                }
                            }

                            artistName = artistName.substring(0, artistName.length - 1)

                            val playResult =
                                    HttpRequest.get("http://$api4NetEase/song/url?id=$musicId")
                                    .timeout(3_000).executeAsync()
                            if (playResult.isOk) {
                                val playJson = JsonParser.parseString(playResult.body())
                                if (playJson.isJsonObject) {
                                    val playUrl = playJson["data"].asJsonArray[0]["url"].asString

                                    val music = MusicCard.Meta.Music(
                                            jumpUrl = musicUrl,
                                            playMusicUrl = playUrl,
                                            previewImageUrl = albumUrl,
                                            singerName = artistName,
                                            title = name
                                    )
                                    val card = MusicCard(meta = MusicCard.Meta(music))
                                    card.prompt = "[分享]${name}"
                                    card.config.currentTime = 1605934298
                                    card.config.token = "66483da4edc6ea53a0646e4e60bb8a89"
                                    card.extra = "{\\\"app_type\\\":1,\\\"appid\\\":100495085,\\\"msg_seq\\\":6897435295466737212,\\\"uin\\\":1}"

                                    return LightApp(gson.toJson(card)).asMessageChain()
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: IOException) {
            logger.warning("在获取网易云音乐时发生了问题", e)
        }
        return "找不到歌曲".convertToChain()
    }

    fun searchQQMusic(name: String, useCard: Boolean = false, contact: Contact): MessageChain {
        if (useCard) {
            return getQQMusicCard(name)
        }

        val searchResult = getQQMusicSearchResult(name) ?: return "找不到歌曲".sendMessage()
        val song = searchResult.data.songs.songList[0]

        val artistName = buildString {
            song.singer.forEach {
                append(it.name + "/")
            }
        }.removeSuffix("/")

        val previewImageUrl = "http://imgcache.qq.com/music/photo/album_300/17/300_albumpic_${song.albumId}_0.jpg"
        var picIs: InputStream? = null

        NetUtil.executeRequest(url = previewImageUrl).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                daemonLogger.warning("在执行网络操作时出现异常", e)
            }

            override fun onResponse(call: Call, response: Response) {
                picIs = response.body?.byteStream()
            }

        })

        val result = "${song.songName}\nby $artistName\n🔗 > https://y.qq.com/n/yqq/song/${song.songMid}.html"

        return if (picIs == null) {
            result.sendMessage()
        } else {
            runBlocking {
                picIs!!.uploadAsImage(contact).plus(result)
            }
        }
    }

    private fun getQQMusicCard(name: String): MessageChain {
        try {
            val searchResult = getQQMusicSearchResult(name)

            if (searchResult != null) {
                val song = searchResult.data.songs.songList[0]

                val artistName = buildString {
                    song.singer.forEach {
                        append(it.name + "/")
                    }
                    substring(0, length - 1)
                }
                val playResult = NetUtil.getPageContent("$api4qq${song.songMid}")
                if (playResult?.isNotBlank() == true) {
                    val playUrl = JsonParser.parseString(playResult).asJsonObject["data"].asJsonObject[song.songMid].asString

                    val meta = MusicCard.Meta(
                            MusicCard.Meta.Music(
                                    jumpUrl = "https://y.qq.com/n/yqq/song/${song.songMid}.html?ADTAG=h5_playsong&no_redirect=1",
                                    playMusicUrl = playUrl,
                                    previewImageUrl = "http://imgcache.qq.com/music/photo/album_300/17/300_albumpic_${song.albumId}_0.jpg",
                                    singerName = artistName,
                                    title = song.songName
                            )
                    )

                    val card = MusicCard(meta = meta)
                    card.prompt = "[分享]${song.songName}"

                    return LightApp(gson.toJson(card)).asMessageChain()
                }
            } else {
                logger.warning("无法从 QQ API 获取到歌曲信息")
            }
        } catch (e: IOException) {
            logger.warning("在通过 QQ 音乐搜索歌曲时发生了一个错误", e)
        } catch (e: JsonParseException) {
            logger.warning("解析 QQ 音乐 json 失败", e)
        } catch (e: JsonSyntaxException) {
            logger.warning("解析 QQ 音乐 json 失败", e)
        }
        return "找不到歌曲".convertToChain()
    }

    private fun getQQMusicSearchResult(name: String): QQMusicSearchResult? {
        val songResult =
                NetUtil.getPageContent("https://c.y.qq.com/soso/fcgi-bin/client_search_cp?g_tk=5381&p=1&n=20&w=${URLEncoder.encode(name, "UTF-8")}&format=json&loginUin=0&hostUin=0&inCharset=utf8&outCharset=utf-8&notice=0&platform=yqq&needNewCode=0&remoteplace=txt.yqq.song&t=0&aggr=1&cr=1&catZhida=0&flag_qc=0")
        return gson.fromJson(songResult ?: return null)
    }

    private data class QQMusicSearchResult(
            val code: Int,
            val data: QQMusicSearchData
    ) {
        data class QQMusicSearchData(
                @SerializedName("keyword")
                val searchKeyWord: String,
                @SerializedName("priority")
                val priority: Int,
                @SerializedName("qc")
                val qc: JsonElement?,
                /**
                 * Including:
                 * curnum: Int,
                 * curpag: Int,
                 * list: List?
                 * totalnum: Int
                 */
                @SerializedName("semantic")
                val semantic: JsonObject,
                @SerializedName("song")
                val songs: QQMusicSongs
        ) {
            data class QQMusicSongs(
                    @SerializedName("curnum")
                    val currentNumber: Int,
                    @SerializedName("curpage")
                    val currentPage: Int,
                    @SerializedName("list")
                    val songList: List<QQMusicSong>
            ) {
                @Suppress("unused")
                data class QQMusicSong(
                        @SerializedName("albumid")
                        val albumId: Int,
                        @SerializedName("albummid")
                        val albumMid: String,
                        /** 专辑名 */
                        @SerializedName("albumname")
                        val albumName: String,
                        @SerializedName("albumname_hilight")
                        val albumNameHiLight: String,
                        @SerializedName("alertid")
                        val alertId: Int,
                        @SerializedName("belongCD")
                        val belongCD: Int,
                        @SerializedName("cdIdx")
                        val cdIdx: Int,
                        @SerializedName("chinesesinger")
                        val chineseSinger: Int,
                        @SerializedName("docid")
                        val docId: String,
                        /** 支持的加密格式? 例如: qqhq;common;mp3common;wmacommon */
                        @SerializedName("format")
                        val supportFormat: String,
                        @SerializedName("grp")
                        val grp: JsonElement?,
                        @SerializedName("interval")
                        val interval: Int,
                        @SerializedName("isonly")
                        val isOnly: Int,
                        @SerializedName("lyric")
                        val lyric: String?,
                        @SerializedName("lyric_hilight")
                        val lyricHiLight: String?,
                        @SerializedName("media_mid")
                        val mediaMid: String,
                        @SerializedName("msgid")
                        val msgId: Int,
                        @SerializedName("newStatus")
                        val newStatus: Int,
                        @SerializedName("nt")
                        val nt: Long,
                        @SerializedName("pay")
                        val pay: JsonObject,
                        @SerializedName("preview")
                        val preview: JsonObject,
                        /** 时间戳 */
                        @SerializedName("pubtime")
                        val publishTime: Long,
                        @SerializedName("pure")
                        val pure: Int,
                        @SerializedName("singer")
                        val singer: List<QQMusicSinger>,

                        /** 不同格式下载大小 */
                        val size128: Long,
                        val size320: Long,
                        val sizeape: Long,
                        val sizeflac: Long,
                        val sizeogg: Long,

                        @SerializedName("songid")
                        val songId: Long,
                        @SerializedName("songmid")
                        val songMid: String,
                        @SerializedName("songname")
                        val songName: String,
                        @SerializedName("songname_hilight")
                        val songNameHilight: String,
                        @SerializedName("songurl")
                        val songUrl: String,
                        @SerializedName("strMediaMid")
                        val mediaMidAsString: String,
                        @SerializedName("stream")
                        val stream: String,
                        @SerializedName("switch")
                        val switch: Long,

                        /** 未知参数 */
                        @SerializedName("t")
                        val t: Long,
                        val tag: Long,
                        val type: Int,
                        val ver: Int,
                        val vid: String
                ) {
                    data class QQMusicSinger(
                            val id: Long,
                            val mid: String,
                            val name: String,
                            @SerializedName("name_hilight")
                            val nameHilight: String
                    )

                    /**
                     * 是否为中文歌手
                     */
                    fun isChineseSinger(): Boolean = chineseSinger == 1
                }
            }
        }
    }
    
    data class MusicCard(
            @SerializedName("app")
            val app: String = "com.tencent.structmsg",
            @SerializedName("desc")
            val description: String = "音乐",
            @SerializedName("view")
            val viewType: String = "music",
            @SerializedName("ver")
            val version: String = "0$versionCode",
            /** 展示为纯文本消息时的样式 */
            @SerializedName("prompt")
            var prompt: String = "[分享]",
            @SerializedName("appID")
            val appID: String = "",
            @SerializedName("sourceName")
            val sourceName: String = "",
            @SerializedName("actionData")
            val actionData: String = "",
            @SerializedName("actionData_A")
            val actionDataA: String = "",
            @SerializedName("sourceUrl")
            val sourceUrl: String = "",
            @SerializedName("meta")
            val meta: Meta,
            @SerializedName("config")
            val config: Config = Config(),
            @SerializedName("text")
            val text: String = "",
            @SerializedName("sourceAd")
            val sourceAd: String = "",
            /** msg_seq 不可修改 */
            @SerializedName("extra")
            var extra: String = "{\\\"app_type\\\":1,\\\"appid\\\":100497308,\\\"msg_seq\\\":6897056676240247867,\\\"uin\\\":1}"
    ) {
        data class Meta(
                val music: Music
        ) {
            data class Music(
                    val action: String = "",
                    @SerializedName("android_pkg_name")
                    val androidPkgName: String = "",
                    @SerializedName("app_type")
                    val appType: Int = 1,
                    @SerializedName("appid")
                    var appId: Long = 100497308,
                    @SerializedName("desc")
                    val singerName: String,
                    @SerializedName("jumpUrl")
                    val jumpUrl: String,
                    @SerializedName("musicUrl")
                    val playMusicUrl: String,
                    @SerializedName("preview")
                    val previewImageUrl: String,
                    @SerializedName("sourceMsgId")
                    val sourceMsgId: String = "0",
                    @SerializedName("source_icon")
                    val sourceIcon: String = "",
                    @SerializedName("source_url")
                    val sourceUrl: String = "",
                    @SerializedName("tag")
                    var tag: String = "QQ音乐",
                    @SerializedName("title")
                    val title: String
            )
        }

        data class Config (
                @SerializedName("autosize")
                val autoSize: Boolean = true,
                /** 不可修改, 可能会失效 */
                @SerializedName("ctime")
                var currentTime: Long = 1605846146,
                @SerializedName("forward")
                val forward: Boolean = true,
                /** 不可修改, 可能会失效 */
                @SerializedName("token")
                var token: String = "966baea0ff335c8c8fcea318b6baaf3e",
                @SerializedName("type")
                val type: String = "normal"
        )
    }

}