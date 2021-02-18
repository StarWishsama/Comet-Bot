package io.github.starwishsama.comet.api.thirdparty.rainbowsix.data

import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import io.github.starwishsama.comet.enums.R6Rank

data class R6StatsSeasonalStat(
    @SerializedName("username")
    val userName: String,
    @SerializedName("platform")
    val platform: String,
    @SerializedName("ubisoft_id")
    val ubisoftId: String,
    @SerializedName("uplay_id")
    val uplayId: String,
    @SerializedName("avatar_url_146")
    val smallAvatar: String,
    @SerializedName("avatar_url_256")
    val avatar: String,
    /**
     * 格式 "2021-02-17T12:47:24.000Z"
     */
    @SerializedName("last_updated")
    val lastUpdateTime: String,
    @SerializedName("seasons")
    val seasonalStat: JsonObject
) {
    fun getSeasonalStat(season: SeasonName): SeasonInfo? {
        val seasonInfo = seasonalStat[season.season]
        return if (!seasonInfo.isJsonObject) {
            null
        } else {
            Gson().fromJson(Gson().toJson(seasonInfo))
        }
    }

    data class SeasonInfo(
        val name: String,
        @SerializedName("start_date")
        val startDate: String,
        @SerializedName("end_date")
        val endDate: String?,
        @SerializedName("regions")
        val regions: JsonObject
    ) {
        fun getRegionStat(region: Region): PerRegionStat? {
            val regionStat = regions[region.region]
            return if (!regionStat.isJsonObject) {
                null
            } else {
                Gson().fromJson(Gson().toJson(regionStat))
            }
        }

        data class PerRegionStat(
            @SerializedName("season_id")
            val seasonId: Int,
            @SerializedName("region")
            val regionName: String,
            @SerializedName("abandons")
            val abandons: Int,
            @SerializedName("losses")
            val losses: Int,
            @SerializedName("max_mmr")
            val maxMMR: Long,
            @SerializedName("max_rank")
            val maxRank: Int,
            @SerializedName("mmr")
            val currentMMR: Long,
            @SerializedName("rank")
            val currentRank: Int,
            @SerializedName("wins")
            val wins: Long,
            @SerializedName("kills")
            val kills: Long,
            @SerializedName("deaths")
            val deaths: Long,
            @SerializedName("last_match_mmr_change")
            val lastMatchMMRChange: Long,
            @SerializedName("champions_rank_position")
            val championPosition: Int,
            @SerializedName("rank_image")
            val rankImage: String,
            @SerializedName("max_rank_image")
            val maxRankImage: String
        ) {
            fun getRank(): R6Rank = R6Rank.getRank(currentRank)
        }
    }
}

enum class SeasonName(val season: String) {
    NEON_DAWN("neon_dawn"),

    SHADOW_LEGACY("shadow_legacy"),

    STEEL_WAVE("steel_wave"),

    VOID_EDGE("void_edge"),

    SHIFTING_TIDES("shifting_tides"),

    EMBER_RISE("ember_rise")
}

enum class Region(val region: String) {
    /**
     * 亚太
     */
    NCSA("ncsa"),
    EMEA("emea"),
    APAC("apac")
}