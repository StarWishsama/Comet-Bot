package io.github.starwishsama.comet.utils.network

import cn.hutool.http.HttpRequest
import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.get
import com.google.gson.*
import com.google.gson.annotations.SerializedName
import io.github.starwishsama.comet.BotVariables.gson
import io.github.starwishsama.comet.BotVariables.logger
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import net.mamoe.mirai.message.data.LightApp
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.asMessageChain
import java.io.IOException
import java.net.URLEncoder

/**
 * 搜索音乐工具类
 *
 * FIXME: 将搜索结果转换为 Bean, 减少繁琐的解析反解析
 */
object MusicUtil {
    /** 1分钟100次，10分钟500次，1小时2000次 */
    private const val api4qq = "https://api.qq.jsososo.com/song/urls?id="
    private const val api4NetEase = "http://musicapi.leanapp.cn/"

    fun searchNetEaseMusic(songName: String): MessageChain {
        try {

            val searchMusicResult = NetUtil.getPageContent("http://$api4NetEase/search?keywords=${
                URLEncoder.encode(
                        songName,
                        "UTF-8"
                )
            }")

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
                                    .timeout(8000).executeAsync()
                            if (playResult.isOk) {
                                val playJson = JsonParser.parseString(playResult.body())
                                if (playJson.isJsonObject) {
                                    val playUrl = playJson["data"].asJsonArray[0]["url"].asString
                                    return LightApp("{\n" +
                                            "    \"app\":\"com.tencent.structmsg\",\n" +
                                            "    \"desc\":\"音乐\",\n" +
                                            "    \"meta\":{\n" +
                                            "    \"music\":{\n" +
                                            "        \"action\":\"\",\n" +
                                            "        \"android_pkg_name\":\"\",\n" +
                                            "        \"app_type\":1,\n" +
                                            "        \"appid\":\"100495085\",\n" +
                                            "        \"desc\":\"$artistName\",\n" +
                                            "        \"jumpUrl\":\"$musicUrl\",\n" +
                                            "        \"musicUrl\":\"$playUrl\",\n" +
                                            "        \"preview\":\"$albumUrl\",\n" +
                                            "        \"sourceMsgId\":0,\n" +
                                            "        \"source_icon\":\"\",\n" +
                                            "        \"source_url\":\"\",\n" +
                                            "        \"tag\":\"网易云音乐\",\n" +
                                            "        \"title\":\"$name\"\n" +
                                            "    }\n" +
                                            "},\n" +
                                            "\"prompt\":\"[分享 $name]\",\n" +
                                            "\"ver\":\"0.0.0.1\",\n" +
                                            "\"view\":\"music\"\n" +
                                            "}").asMessageChain()
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

    fun searchQQMusic(name: String): MessageChain {
        try {
            val songResult =
                    NetUtil.getPageContent("https://c.y.qq.com/soso/fcgi-bin/client_search_cp?g_tk=5381&p=1&n=20&w=${URLEncoder.encode(name, "UTF-8")}&format=json&loginUin=0&hostUin=0&inCharset=utf8&outCharset=utf-8&notice=0&platform=yqq&needNewCode=0&remoteplace=txt.yqq.song&t=0&aggr=1&cr=1&catZhida=0&flag_qc=0")

            if (songResult?.isNotBlank() == true) {
                val searchResult = gson.fromJson<QQMusicSearchResult>(songResult)

                val song = searchResult.data.songs.songList[0]

                val artistName = buildString {
                    song.singer.forEach {
                        append(it.name + "/")
                    }
                    removeSuffix("/")
                }
                val playResult = NetUtil.getPageContent("$api4qq${song.songMid}")
                if (playResult?.isNotBlank() == true) {
                    val playUrl = JsonParser.parseString(playResult).asJsonObject["data"].asJsonObject[song.songMid].asString

                    val json = "{\n	\"app\": \"com.tencent.structmsg\",\n	\"desc\": \"com.tencent.structmsg\",\n	\"view\": \"music\",\n	\"ver\": \"0.0.0.1\",\n	\"prompt\": \"[应用]${song.songName}\",\n	\"appID\": \"\",\n	\"sourceName\": \"\",\n	\"sourceUrl\": \"\",\n	\"meta\": {\n		\"music\": {\n			\"title\": \"${song.songName}\",\n			\"musicUrl\": \"$playUrl\",\n			\"desc\": \"$artistName\",\n			\"preview\": \"http:\\/\\/imgcache.qq.com\\/music\\/photo\\/album_300\\/17\\/300_albumpic_\"${song.albumId}\"_0.jpg\",\n			\"tag\": \"QQ音乐\",\n			\"jumpUrl\": \"web.p.qq.com\\/qqmpmobile\\/aio\\/app.html?id=${song.songId}\",\n			\"appid\": 100497308,\n			\"app_type\": 1\n		}\n	},\n	\"config\": {\n		\"forward\": 1,\n		\"autosize\": 1,\n		\"type\": \"normal\"\n	},\n	\"text\": \"\",\n	\"sourceAd\": \"\",\n	\"extra\": \"\"\n}"

                    return LightApp(json).asMessageChain()
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

}