package io.github.starwishsama.comet.objects.config.api

import kotlinx.serialization.Serializable
import net.mamoe.yamlkt.Comment
import java.util.concurrent.TimeUnit

@Serializable
data class R6StatsConfig(
    val token: String = "",
    @Comment("R6Stats API 此项无需修改")
    override val interval: Int = -1,
    @Comment("R6Stats API 此项无需修改")
    override val timeUnit: TimeUnit = TimeUnit.MINUTES
) : ApiConfig {
    override val apiName: String = "r6stats"
}