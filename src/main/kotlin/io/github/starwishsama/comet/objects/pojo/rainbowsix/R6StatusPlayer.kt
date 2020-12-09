package io.github.starwishsama.comet.objects.pojo.rainbowsix

import com.google.gson.annotations.SerializedName

@Deprecated("Move to R6Stats API")
data class R6StatusPlayer(var status : Int, var found : Boolean,
                          var player: PlayerBean? = null, var custom: CustomBean? = null,
                          var refresh: RefreshBean? = null,
                          var stats: StatsBean? = null,
                          var ranked: RankedBean? = null) {

    data class PlayerBean (
            @SerializedName("p_id")
            var playerId: String? = null,
            @SerializedName("p_user")
            var playerUser: String? = null,
            @SerializedName("p_name")
            var playerName: String? = null,
            @SerializedName("p_platform")
            var playerPlatform: String? = null)

    data class CustomBean(@SerializedName("customurl")
                          var customUrl: String? = null,
                          var verified : Boolean = false,
                          var visitors : Int = 0,
                          var banned : Boolean = false)

    data class RefreshBean (var queued : Boolean = false,
            var possible : Boolean = false,
            var qtime : Int = 0,
            var utime : Int = 0,
            var status : Int = 0)

    data class StatsBean(
            var level : Int = 0,
            @SerializedName("casualpvp_kd")
            var casualKd: String? = null,
            @SerializedName("rankedpvp_kd")
            var rankedKd: String? = null,
            @SerializedName("generalpvp_kd")
            var generalKd: String? = null,
            @SerializedName("generalpvp_kills")
            var generalKills: Int? = null,
            @SerializedName("generalpvp_headshot")
            var generalHeadShot: Double? = null
    )

    data class RankedBean(
            @SerializedName("AS_mmr")
            var asMMR : Int = 0,
            @SerializedName("AS_mmrchange")
            var asMMRChange : Int? = 0,
            @SerializedName("AS_rank")
            var asRank : Int = 0)
}