/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.api.thirdparty.music.entity

import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
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
    private fun getAuthorName(): String =
        buildString {
            author.forEach {
                append("$it/")
            }
        }.removeSuffix("/")

    fun isEmpty(): Boolean = name.isEmpty() || jumpURL.isEmpty() || albumPicture.isEmpty() || songURL.isEmpty()

    fun toMusicShare(kind: MusicKind): MessageChain {
        return MusicShare(
            kind,
            name,
            getAuthorName(),
            jumpURL,
            albumPicture,
            songURL
        ).toMessageChain()
    }

    fun toMessageWrapper(): MessageWrapper {
        val wrapper = MessageWrapper()

        wrapper.addPictureByURL(albumPicture)

        wrapper.addText(
            "$name - ${getAuthorName()}\n" +
                    "跳转链接: $jumpURL"
        )

        return wrapper
    }
}