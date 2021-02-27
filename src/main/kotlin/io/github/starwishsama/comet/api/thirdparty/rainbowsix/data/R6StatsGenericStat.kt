package io.github.starwishsama.comet.api.thirdparty.rainbowsix.data

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

/**
 * R6Tab 综合玩家数据
 *
 * Endpoint: https://api2.r6stats.com/public-api/stats/{username}/{platform}/generic
 *
 * 时间格式: 2020-12-09T04:19:47.000Z
 *
 * @author Nameless
 */
data class R6StatsGenericStat(
    @SerializedName("username")
    val username: String,
    @SerializedName("platform")
    val platform: String,
    @SerializedName("ubisoft_id")
    val ubisoftID: String,
    @SerializedName("uplay_id")
    val uPlayID: String,
    @SerializedName("avatar_url_146")
    val avatarUrlSmall: String,
    @SerializedName("avatar_url_256")
    val avatarUrlMedium: String,
    @SerializedName("last_updated")
    val lastUpdatedTime: String,
    @SerializedName("aliases")
    val aliases: List<Aliases>,
    @SerializedName("progression")
    val levelInfo: Progression,
    @SerializedName("stats")
    val stats: GameStats
) {
    data class Aliases(
        val username: String,
        @SerializedName("last_seen_at")
        val lastSeenTime: String
    )

    data class Progression(
        val level: Int,
        @SerializedName("lootbox_probability")
        val alphaBoxProbability: Double,
        @SerializedName("total_xp")
        val totalXp: Long
    )

    data class GameStats(
        @SerializedName("general")
        val generalStat: GeneralStat,
        @SerializedName("queue")
        val queue: JsonObject, //Queue,
        @SerializedName("gamemode")
        val gameMode: JsonObject, //GameMode,
        @SerializedName("timestamps")
        val timeStamp: TimeStamp
    ) {
        data class GeneralStat(
            /**
             * 助攻数
             */
            @SerializedName("assists")
            val assists: Long,
            /**
             * 部署过的阻挡物数
             */
            @SerializedName("barricades_deployed")
            val deployedBarricades: Long,
            /**
             * 盲杀数量
             */
            @SerializedName("blind_kills")
            val blindKills: Long,
            @SerializedName("bullets_fired")
            val firedBullets: Long,
            @SerializedName("bullets_hit")
            val hitBullets: Long,
            /**
             * DBNO (Down But Not Out / 倒了还没死) 次数
             *
             * 详见: https://data.fandom.com/wiki/DBNO
             */
            @SerializedName("dbnos")
            val dbno: Long,
            @SerializedName("deaths")
            val deaths: Long,
            @SerializedName("distance_travelled")
            val travelledDistance: Long,
            @SerializedName("draws")
            val draws: Long,
            @SerializedName("gadgets_destroyed")
            val destroyedGadgets: Long,
            @SerializedName("games_played")
            val playedGameTime: Long,
            @SerializedName("headshots")
            val headshotTime: Long,
            @SerializedName("kd")
            val kd: Double,
            @SerializedName("kills")
            val kills: Long,
            @SerializedName("losses")
            val lossTime: Long,
            /**
             * 近战击杀次数
             */
            @SerializedName("melee_kills")
            val meleeKills: Long,
            /**
             * 穿透击杀次数
             */
            @SerializedName("penetration_kills")
            val penetrationKills: Long,
            @SerializedName("playtime")
            val playTime: Long,
            @SerializedName("rappel_breaches")
            val rappelBreaches: Long,
            @SerializedName("reinforcements_deployed")
            val deployedReinforcements: Long,
            @SerializedName("revives")
            val reviveTime: Long,
            @SerializedName("suicides")
            val suicideTime: Long,
            @SerializedName("wins")
            val winTime: Long,
            @SerializedName("wl")
            val winLoss: Double
        )

        //data class Queue()

        //data class GameMode()

        data class TimeStamp(
            val created: String,
            @SerializedName("last_updated")
            val lastUpdatedTime: String
        )
    }

    fun getFancyInfo(): String {
        return "♦ $username [${levelInfo.level}]\n" +
                "▶ 总游玩时长 ${stats.generalStat.playTime}\n" +
                "▶ KD ${stats.generalStat.kd} | 胜率 ${(stats.generalStat.winTime / stats.generalStat.playedGameTime) * 100}%\n" +
                ""
    }
}