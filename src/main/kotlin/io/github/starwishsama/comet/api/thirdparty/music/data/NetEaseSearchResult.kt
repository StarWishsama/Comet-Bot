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
