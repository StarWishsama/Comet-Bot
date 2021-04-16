package io.github.starwishsama.comet.objects.config.api

import net.mamoe.yamlkt.Comment
import java.util.concurrent.TimeUnit

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