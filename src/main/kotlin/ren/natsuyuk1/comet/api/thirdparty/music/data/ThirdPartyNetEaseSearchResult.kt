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

data class ThirdPartyNetEaseSearchResult(
    val result: Result,
    val code: Int
) {
    data class Result(
        val songs: List<Song>,
        val songCount: Int
    )

    data class Song(
        val id: Long,
        @JsonProperty("name")
        val songName: String,
        val artists: List<Artist>,
        @JsonProperty("al")
        val album: Album
    ) {
        data class Artist(
            val id: Long,
            val name: String
        )

        data class Album(
            val id: Long,
            val name: String,
            val picUrl: String
        )

        fun buildArtistsName(): List<String> {
            val names = mutableSetOf<String>()

            artists.forEach {
                names.add(it.name)
            }

            return names.toList()
        }
    }
}

data class LeanAppDetailResponse(
    val songs: List<SongInfo>,
    val code: Int
) {
    data class SongInfo(
        val name: String,
        val id: Long,
        @JsonProperty("ar")
        val artist: List<Artist>,
        @JsonProperty("al")
        val album: Album
    ) {
        fun buildArtistsName(): List<String> {
            val names = mutableSetOf<String>()

            artist.forEach {
                names.add(it.name)
            }

            return names.toList()
        }

        data class Artist(
            val id: Long,
            val name: String
        )

        data class Album(
            val id: Long,
            val name: String,
            @JsonProperty("picUrl")
            val albumPictureURL: String
        )
    }
}
