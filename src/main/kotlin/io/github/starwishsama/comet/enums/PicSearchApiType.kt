/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.enums

enum class PicSearchApiType(val desc: String) {
    SAUCENAO("老牌以图搜图，有超三十个图片来源"),
    ASCII2D("搜索准确的 pixiv、Twitter、ニコニコ静画来源，准确度对图片大小有一定要求"),
    BAIDU("支持三次元二次元图片，用来搜索表情包什么的很好用")
}