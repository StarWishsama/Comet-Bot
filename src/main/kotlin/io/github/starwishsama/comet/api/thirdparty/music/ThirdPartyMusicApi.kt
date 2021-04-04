package io.github.starwishsama.comet.api.thirdparty.music

import com.fasterxml.jackson.module.kotlin.readValue
import io.github.starwishsama.comet.BotVariables.mapper
import io.github.starwishsama.comet.api.thirdparty.music.data.LeanAppDetailResponse
import io.github.starwishsama.comet.api.thirdparty.music.data.LeanAppSearchResponse
import io.github.starwishsama.comet.api.thirdparty.music.data.QQMusicSearchResult
import io.github.starwishsama.comet.api.thirdparty.music.entity.MusicSearchResult
import io.github.starwishsama.comet.utils.CometUtil.toChain
import io.github.starwishsama.comet.utils.network.NetUtil
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MusicKind
import net.mamoe.mirai.message.data.MusicShare
import net.mamoe.mirai.message.data.toMessageChain
import java.net.URLEncoder

/**
 * 腾讯音乐, 网易云音乐搜索 API
 */
object ThirdPartyMusicApi {
    /** 腾讯, 调用限额1分钟100次，10分钟500次，1小时2000次 */
    private const val jsososo = "https://api.qq.jsososo.com/song/urls?id="

    // 网易
    private const val leanapp = "https://musicapi.leanapp.cn"

    fun searchNetEaseMusic(name: String, length: Int = 3): List<MusicSearchResult> {
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

        songs.subList(0, songs.size.coerceAtMost(length - 1)).forEach {
            val songResult =
                NetUtil.getPageContent("http://$leanapp/song/detail?ids=${it.id}")
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

    fun searchQQMusic(name: String, length: Int = 5): List<MusicSearchResult> {
        val searchResult = getQQMusicSearchResult(name) ?: return emptyList()

        val result = mutableListOf<MusicSearchResult>()

        val songs = searchResult.data.songs.songList

        songs.subList(0, songs.size.coerceAtMost(length))

        songs.forEach { song ->
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
                    if (!musicUrlObject["data"].isNull) {
                        musicUrlObject["data"][song.songMid].asText()
                    } else {
                        ""
                    }
                }
            } else {
                return@forEach
            }

            result.add(
                MusicSearchResult(
                    song.songName,
                    artists,
                    "https://y.qq.com/n/yqq/song/${song.songMid}.html?ADTAG=h5_playsong&no_redirect=1",
                    "http://imgcache.qq.com/music/photo/album_300/17/300_albumpic_${song.albumId}_0.jpg",
                    playURL
                )
            )
        }

        return result
    }

    fun generateQQMusicCard(result: List<MusicSearchResult>, index: Int = 0): MessageChain {
        if (result.isEmpty() || !result[0].usable) {
            return "找不到歌曲".toChain()
        }

        if (index > result.size - 1 || index < 0) {
            return "找不到对应的歌曲".toChain()
        }

        return MusicShare(
            kind = MusicKind.QQMusic,
            title = result[index].name,
            summary = result[index].getAuthorName(),
            jumpUrl = result[index].jumpURL,
            pictureUrl = result[index].albumPicture,
            musicUrl = result[index].songURL
        ).toMessageChain()
    }

    private fun getQQMusicSearchResult(name: String): QQMusicSearchResult? {
        val songResult =
            NetUtil.getPageContent(
                "https://c.y.qq.com/soso/fcgi-bin/client_search_cp?g_tk=5381&p=1&n=20&w=${
                    URLEncoder.encode(
                        name,
                        "UTF-8"
                    )
                }&format=json&loginUin=0&hostUin=0&inCharset=utf8&outCharset=utf-8&notice=0&platform=yqq&needNewCode=0&remoteplace=txt.yqq.song&t=0&aggr=1&cr=1&catZhida=0&flag_qc=0"
            )
        return mapper.readValue(songResult ?: return null)
    }
}