/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.objects.config.api

import net.mamoe.yamlkt.Comment
import java.util.concurrent.TimeUnit

data class ThirdPartyMusicConfig(
    override val apiName: String = "音乐 API",
    override val interval: Int = 0,
    override val timeUnit: TimeUnit = TimeUnit.SECONDS,
    @Comment("QQ 音乐 API, 仅支持 https://github.com/jsososo/QQMusicApi")
    val qqMusic: String = "https://api.qq.jsososo.com",
    @Comment("网易云音乐 API, 仅支持 https://github.com/Binaryify/NeteaseCloudMusicApi")
    val netEaseCloudMusic: String = "",
) : ApiConfig
