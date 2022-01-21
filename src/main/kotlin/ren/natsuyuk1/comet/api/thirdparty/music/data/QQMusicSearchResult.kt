/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package ren.natsuyuk1.comet.api.thirdparty.music.data

import com.fasterxml.jackson.annotation.JsonProperty

data class QQMusicSearchResult(
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
                @JsonProperty("singer")
                val singer: List<QQMusicSinger>,

                @JsonProperty("songid")
                val songId: Long,
                @JsonProperty("songmid")
                val songMid: String,
                @JsonProperty("songname")
                val songName: String,
            ) {
                data class QQMusicSinger(
                    val id: Long,
                    val mid: String,
                    val name: String,
                )
            }
        }
    }
}