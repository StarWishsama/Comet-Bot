package io.github.starwishsama.comet.api.thirdparty.music.entity

import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MusicKind
import net.mamoe.mirai.message.data.MusicShare
import net.mamoe.mirai.message.data.toMessageChain

data class MusicSearchResult(
    val name: String,
    val author: List<String>,
    val jumpURL: String,
    val albumPicture: String,
    val songURL: String
) {
    fun getAuthorName(): String =
        buildString {
            author.forEach {
                append("$it/")
            }
        }.removeSuffix("/")

    fun isEmpty(): Boolean = name.isEmpty() || jumpURL.isEmpty() || albumPicture.isEmpty() || songURL.isEmpty()

    fun toMessageChain(kind: MusicKind): MessageChain {
        return MusicShare(
            kind,
            name,
            getAuthorName(),
            jumpURL,
            albumPicture,
            songURL
        ).toMessageChain()
    }
}