package ren.natsuyuk1.comet.network.thirdparty.minecraft

import kotlinx.serialization.Serializable

val colorCodeRegex = Regex("§[A-Za-z0-9]")

@Serializable
data class QueryInfo(
    val payload: String,
    val type: MinecraftServerType,
    val ping: Long
)
