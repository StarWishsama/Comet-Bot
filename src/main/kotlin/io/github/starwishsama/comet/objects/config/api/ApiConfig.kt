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

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.util.concurrent.TimeUnit

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes(
    JsonSubTypes.Type(value = BiliBiliConfig::class),
    JsonSubTypes.Type(value = R6StatsConfig::class),
    JsonSubTypes.Type(value = TwitterConfig::class),
    JsonSubTypes.Type(value = SauceNaoConfig::class),
    JsonSubTypes.Type(value = ThirdPartyMusicConfig::class),
)
interface ApiConfig {
    val apiName: String

    val interval: Int

    val timeUnit: TimeUnit
}
