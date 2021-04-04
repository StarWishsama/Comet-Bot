package io.github.starwishsama.comet.api.thirdparty.music.data

import com.fasterxml.jackson.annotation.JsonProperty

data class LeanAppSearchResponse(
    val result: Result,
    val code: Int
) {
    data class Result(
        val songs: List<Song>,
        val hasMore: Boolean = false,
        val songCount: Int
    )

    data class Song(
        val id: Long,
        @JsonProperty("name")
        val songName: String,
        val artists: List<Artist>,
        val album: Album
    ) {
        data class Artist(
            val id: Long,
            val name: String
        )

        data class Album(
            val id: Long,
            val name: String,
            val artists: List<Artist>
        )
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