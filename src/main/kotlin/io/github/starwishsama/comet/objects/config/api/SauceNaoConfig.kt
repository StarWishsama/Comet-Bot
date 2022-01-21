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

import kotlinx.serialization.Serializable
import net.mamoe.yamlkt.Comment
import java.util.concurrent.TimeUnit

@Serializable
data class SauceNaoConfig(
    @Comment("SauceNao Token, 不填亦可")
    val token: String = "",
    @Comment("SauceNao API 此项无需修改")
    override val interval: Int = -1,
    @Comment("SauceNao API 此项无需修改")
    override val timeUnit: TimeUnit = TimeUnit.MINUTES
) : ApiConfig {
    override val apiName: String = "saucenao"
}