/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.api.thirdparty.music

import cn.hutool.core.net.URLEncoder
import cn.hutool.http.HttpStatus
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.starwishsama.comet.CometVariables.mapper
import io.github.starwishsama.comet.api.thirdparty.music.data.NetEaseSearchResult
import io.github.starwishsama.comet.api.thirdparty.music.data.QQMusicSearchResult
import io.github.starwishsama.comet.api.thirdparty.music.data.ThirdPartyNetEaseSearchResult
import io.github.starwishsama.comet.api.thirdparty.music.entity.MusicSearchResult
import io.github.starwishsama.comet.managers.ApiManager
import io.github.starwishsama.comet.objects.config.api.ThirdPartyMusicConfig
import io.github.starwishsama.comet.utils.network.NetUtil
import org.jsoup.Jsoup
import java.nio.charset.StandardCharsets

/**
 * 腾讯音乐, 网易云音乐搜索 API
 */
object ThirdPartyMusicApi {
    private val apiConfig = ApiManager.getConfig<ThirdPartyMusicConfig>()

    fun searchNetEaseMusic(name: String, length: Int = 1): List<MusicSearchResult> {
        var isThirdParty = false

        val resp = if (apiConfig.netEaseCloudMusic.isEmpty()) {
            NetUtil.executeHttpRequest(
                "https://music.163.com/api/search/pc?offset=0&total=true&limit=${1.coerceAtMost(length)}&type=1&s=${name}",
                call = {
                    header("content-type", "application/x-www-form-urlencoded")
                }
            )
        } else {
            isThirdParty = true

            NetUtil.executeHttpRequest(
                "${apiConfig.netEaseCloudMusic.removeSuffix("/")}/cloudsearch?keywords=${
                    URLEncoder.DEFAULT.encode(
                        name,
                        StandardCharsets.UTF_8
                    )
                }",
                timeout = 12
            )
        }

        val page = resp.body?.string()

        val searchResult: Any = if (isThirdParty) {
            mapper.readValue<ThirdPartyNetEaseSearchResult>(page ?: return emptyList())
        } else {
            mapper.readValue<NetEaseSearchResult>(page ?: return emptyList())
        }

        val songResults = mutableListOf<MusicSearchResult>()

        when (searchResult) {
            is ThirdPartyNetEaseSearchResult -> {
                if (searchResult.code != HttpStatus.HTTP_OK) {
                    return emptyList()
                }

                searchResult.result.songs.forEach { song: ThirdPartyNetEaseSearchResult.Song ->

                    songResults.add(
                        MusicSearchResult(
                            song.songName,
                            song.buildArtistsName(),
                            "https://music.163.com/#/song?id=${song.id}",
                            song.album.picUrl,
                            "http://music.163.com/song/media/outer/url?id=${song.id}&userid=1"
                        )
                    )
                }
            }
            is NetEaseSearchResult -> {
                if (searchResult.code != HttpStatus.HTTP_OK) {
                    return emptyList()
                }

                searchResult.result.songs.forEach { song: NetEaseSearchResult.Song ->
                    songResults.add(
                        MusicSearchResult(
                            song.name,
                            song.buildArtistsName(),
                            "https://music.163.com/#/song?id=${song.id}",
                            song.album.picUrl,
                            "http://music.163.com/song/media/outer/url?id=${song.id}&userid=1"
                        )
                    )
                }
            }
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

            val playResult =
                NetUtil.getPageContent("${apiConfig.qqMusic.removeSuffix("/")}/song/urls?id=${song.songMid}")

            val playURL: String

            if (playResult?.isNotBlank() == true) {
                val musicUrlObject = mapper.readTree(playResult)

                playURL = if (musicUrlObject.isNull) {
                    return@forEach
                } else {
                    val urlObject = musicUrlObject["data"]
                    if (!urlObject.isNull) {
                        if (urlObject[song.songMid] != null && !urlObject[song.songMid].isNull) {
                            urlObject[song.songMid].asText()
                        } else {
                            return@forEach
                        }
                    } else {
                        return@forEach
                    }
                }
            } else {
                // Unable to play music
                playURL = ""
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

    fun getQQMusicSearchResult(name: String): QQMusicSearchResult? {
        val songResult =
            NetUtil.getPageContent(
                "https://c.y.qq.com/soso/fcgi-bin/client_search_cp?w=${
                    URLEncoder.DEFAULT.encode(
                        name,
                        StandardCharsets.UTF_8
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