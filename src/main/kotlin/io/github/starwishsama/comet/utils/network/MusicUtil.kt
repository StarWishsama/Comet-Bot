package io.github.starwishsama.comet.utils.network

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.salomonbrys.kotson.fromJson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import com.google.gson.JsonSyntaxException
import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.BotVariables.logger
import io.github.starwishsama.comet.BotVariables.mapper
import io.github.starwishsama.comet.utils.CometUtil.toChain
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MusicKind
import net.mamoe.mirai.message.data.MusicShare
import net.mamoe.mirai.message.data.toMessageChain
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
 * TODO: 重构搜索方式, 应给出选项
 */
object MusicUtil {
    /** 1分钟100次，10分钟500次，1小时2000次 */
    private const val api4qq = "https://api.qq.jsososo.com/song/urls?id="
    private const val api4NetEase = "https://musicapi.leanapp.cn"
    private val gson = GsonBuilder().serializeNulls().setLenient().disableHtmlEscaping().create()

    fun searchNetEaseMusic(songName: String): MessageChain {
        try {
            val searchMusicResult = NetUtil.getPageContent("$api4NetEase/search?keywords=${URLEncoder.encode(songName, "UTF-8")}")

            if (searchMusicResult?.isNotEmpty() == true) {
                val searchResult = mapper.readTree(searchMusicResult)
                if (!searchResult.isNull) {
                    val musicId = searchResult["result"]["songs"][0]["id"].asInt()
                    val musicUrl = "https://music.163.com/#/song?id=$musicId"
                    val songResult = NetUtil.getPageContent("http://$api4NetEase/song/detail?ids=$musicId")
                    if (songResult?.isNotEmpty() == true) {
                        val songJson = mapper.readTree(songResult)
                        if (!songJson.isNull) {
                            val albumUrl = songJson["songs"][0]["al"]["picUrl"].asText()
                            val name = songJson["songs"][0]["name"].asText()
                            var artistName = ""

                            songJson["songs"][0]["ar"].elements().forEach {
                                run {
                                    artistName += (it["name"].asText() + "/")
                                }
                            }

                            artistName = artistName.substring(0, artistName.length - 1)


                            return MusicShare(
                                MusicKind.NeteaseCloudMusic,
                                name,
                                artistName,
                                jumpUrl = musicUrl,
                                pictureUrl = albumUrl,
                                musicUrl = "http://music.163.com/song/media/outer/url?id=${musicId}&userid=1"
                            ).toMessageChain()
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

        val searchResult = getQQMusicSearchResult(name) ?: return "找不到歌曲".toChain()
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
            result.toChain()
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
                }.removeSuffix("/")

                val playResult = NetUtil.getPageContent("$api4qq${song.songMid}")
                if (playResult?.isNotBlank() == true) {
                    val musicUrlObject = mapper.readTree(playResult)

                    val playUrl = if (musicUrlObject.isNull) {
                        ""
                    } else {
                        if (!musicUrlObject["data"].isNull) {
                            musicUrlObject["data"][song.songMid].asText()
                        } else {
                            ""
                        }
                    }

                    return MusicShare(
                        kind = MusicKind.QQMusic,
                        title = song.songName,
                        summary = artistName,
                        jumpUrl = "https://y.qq.com/n/yqq/song/${song.songMid}.html?ADTAG=h5_playsong&no_redirect=1",
                        pictureUrl = "http://imgcache.qq.com/music/photo/album_300/17/300_albumpic_${song.albumId}_0.jpg",
                        musicUrl = playUrl
                    ).toMessageChain()
                }
            } else {
                logger.warning("无法从 QQ 音乐 API 获取到歌曲信息")
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
                @JsonProperty("keyword")
                val searchKeyWord: String,
                @JsonProperty("song")
                val songs: QQMusicSongs
        ) {
            data class QQMusicSongs(
                    @JsonProperty("list")
                    val songList: List<QQMusicSong>
            ) {
                @Suppress("unused")
                data class QQMusicSong(
                        @JsonProperty("albumid")
                        val albumId: Int,
                        @JsonProperty("albummid")
                        val albumMid: String,
                        /** 专辑名 */
                        @JsonProperty("albumname")
                        val albumName: String,
                        @JsonProperty("chinesesinger")
                        val chineseSinger: Int,
                        @JsonProperty("singer")
                        val singer: List<QQMusicSinger>,

                        @JsonProperty("songid")
                        val songId: Long,
                        @JsonProperty("songmid")
                        val songMid: String,
                        @JsonProperty("songname")
                        val songName: String,
                        @JsonProperty("songurl")
                        val songUrl: String,
                        @JsonProperty("strMediaMid")
                        val mediaMidAsString: String,
                ) {
                    data class QQMusicSinger(
                            val id: Long,
                            val mid: String,
                            val name: String,
                    )

                    /**
                     * 是否为中文歌手
                     */
                    fun isChineseSinger(): Boolean = chineseSinger == 1
                }
            }
        }
    }
}