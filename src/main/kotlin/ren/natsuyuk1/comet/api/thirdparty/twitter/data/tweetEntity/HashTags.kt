/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package ren.natsuyuk1.comet.api.thirdparty.twitter.data.tweetEntity

data class HashTags(
    /**
     * An array of integers indicating the offsets within the Tweet text where the hashtag begins and ends.
     * The first integer represents the location of the # character in the Tweet text string.
     * The second integer represents the location of the first character after the hashtag.
     * Therefore the difference between the two numbers will be the length of the hashtag name plus one
     * (for the ‘#’ character).
     */
    val indices: List<Int>,
    /**
     * hashtag 的名字, 删去了开头的 ‘#’
     */
    val text: String
)