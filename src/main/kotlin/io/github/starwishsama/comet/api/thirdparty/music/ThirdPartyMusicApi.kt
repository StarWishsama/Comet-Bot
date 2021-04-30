package io.github.starwishsama.comet.api.thirdparty.music

import com.fasterxml.jackson.module.kotlin.readValue
import io.github.starwishsama.comet.BotVariables.mapper
import io.github.starwishsama.comet.api.thirdparty.music.data.LeanAppDetailResponse
import io.github.starwishsama.comet.api.thirdparty.music.data.LeanAppSearchResponse
import io.github.starwishsama.comet.api.thirdparty.music.data.QQMusicSearchResult
import io.github.starwishsama.comet.api.thirdparty.music.entity.MusicSearchResult
import io.github.starwishsama.comet.utils.network.NetUtil
import org.jsoup.Jsoup
import java.net.URLEncoder

/**
 * 腾讯音乐, 网易云音乐搜索 API
 */
object ThirdPartyMusicApi {
    /** 腾讯, 调用限额1分钟100次，10分钟500次，1小时2000次 */
    private const val jsososo = "https://api.qq.jsososo.com/song/urls?id="

    // 网易
    private const val leanapp = "https://musicapi.leanapp.cn"

    fun searchNetEaseMusic(name: String, length: Int = 1): List<MusicSearchResult> {
        val page = NetUtil.getPageContent("$leanapp/search?keywords=${URLEncoder.encode(name, "UTF-8")}")

        val searchResult: LeanAppSearchResponse = mapper.readValue(page ?: return emptyList())

        if (searchResult.code != 200) {
            return emptyList()
        }

        val songDetails = mutableListOf<LeanAppDetailResponse>()

        val songs = searchResult.result.songs

        if (songs.isEmpty()) {
            return emptyList()
        }

        songs.subList(0, songs.size.coerceAtMost(length)).forEach {
            val songResult =
                NetUtil.getPageContent("$leanapp/song/detail?ids=${it.id}")
            songDetails.add(mapper.readValue(songResult ?: return@forEach))
        }

        val songResults = mutableListOf<MusicSearchResult>()

        songDetails.forEach {
            songResults.add(
                MusicSearchResult(
                    it.songs[0].name,
                    it.songs[0].buildArtistsName(),
                    "https://music.163.com/#/song?id=${it.songs[0].id}",
                    it.songs[0].album.albumPictureURL,
                    "http://music.163.com/song/media/outer/url?id=${it.songs[0].id}&userid=1"
                )
            )
        }

        return songResults
    }

    fun searchQQMusic(name: String, length: Int = 1): List<MusicSearchResult> {
        val searchResult = getQQMusicSearchResult(name) ?: return emptyList()

        val result = mutableListOf<MusicSearchResult>()

        val songs = searchResult.data.songs.songList

        songs.subList(0, songs.size.coerceAtMost(length)).forEach { song ->
            val artists = mutableListOf<String>().also { l ->
                song.singer.forEach {
                    l.add(it.name)
                }
            }

            val playResult = NetUtil.getPageContent("$jsososo${song.songMid}")

            val playURL: String

            if (playResult?.isNotBlank() == true) {
                val musicUrlObject = mapper.readTree(playResult)

                playURL = if (musicUrlObject.isNull) {
                    ""
                } else {
                    try {
                        val urlObject = musicUrlObject["data"]
                        if (!urlObject.isNull) {
                            urlObject[song.songMid].asText()
                        } else {
                            ""
                        }
                    } catch (e: NullPointerException) {
                        return@forEach
                    }
                }
            } else {
                return@forEach
            }

            val jumpUrl = "https://y.qq.com/n/yqq/song/${song.songMid}.html?no_redirect=1"

            result.add(
                MusicSearchResult(
                    song.songName,
                    artists,
                    jumpUrl,
                    getQQMusicCover(jumpUrl),
                    playURL
                )
            )
        }

        return result
    }

    private fun getQQMusicSearchResult(name: String): QQMusicSearchResult? {
        val songResult =
            NetUtil.getPageContent(
                "https://c.y.qq.com/soso/fcgi-bin/client_search_cp?w=${
                    URLEncoder.encode(
                        name,
                        "UTF-8"
                    )
                }&format=json&inCharset=utf8&outCharset=utf-8"
            )
        return mapper.readValue(songResult ?: return null)
    }

    private fun getQQMusicCover(songURL: String): String {
        val req = Jsoup.connect(songURL)
        val resp = req.execute()

        return "http:" + resp.parse().getElementsByClass("data__cover").select("img")[0].attributes()["src"]
    }
}