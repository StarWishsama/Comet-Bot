/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.api.thirdparty.music.data

/**
 * [NetEaseSearchResult]
 *
 * 网易云音乐 API 搜索结果
 *
 * 🔗: http://music.163.com/api/search/pc
 */
data class NetEaseSearchResult(
    val result: SongResult,
    val code: Int
) {
    data class SongResult(
        val songs: List<Song>,
        val songCount: Int
    )

    data class Song(
        val name: String,
        val id: Long,
        val artists: List<Artist>,
        val album: AlbumInfo
    ) {
        data class Artist(
            val name: String,
            val id: Long
        )

        data class AlbumInfo(
            val name: String,
            val id: Long,
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
