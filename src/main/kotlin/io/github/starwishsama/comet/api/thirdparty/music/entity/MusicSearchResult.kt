package io.github.starwishsama.comet.api.thirdparty.music.entity

data class MusicSearchResult(
    val name: String,
    val author: List<String>,
    val jumpURL: String,
    val albumPicture: String,
    val songURL: String,
    val usable: Boolean = true
) {
    fun getAuthorName(): String =
        buildString {
            author.forEach {
                append("$it/")
            }
        }.removeSuffix("/")

    fun isEmpty(): Boolean = name.isEmpty() || jumpURL.isEmpty() || albumPicture.isEmpty() || songURL.isEmpty()

    companion object {
        fun empty(): MusicSearchResult = MusicSearchResult("", arrayListOf(), "", "", "", false)
    }
}