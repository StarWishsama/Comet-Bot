package io.github.starwishsama.comet.api.thirdparty.rainbowsix.data

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode

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
    @JsonProperty("username")
    val username: String,
    @JsonProperty("platform")
    val platform: String,
    @JsonProperty("ubisoft_id")
    val ubisoftID: String,
    @JsonProperty("uplay_id")
    val uPlayID: String,
    @JsonProperty("avatar_url_146")
    val avatarUrlSmall: String,
    @JsonProperty("avatar_url_256")
    val avatarUrlMedium: String,
    @JsonProperty("last_updated")
    val lastUpdatedTime: String,
    @JsonProperty("aliases")
    val aliases: List<Aliases>,
    @JsonProperty("progression")
    val levelInfo: Progression,
    @JsonProperty("stats")
    val stats: GameStats
) {
    data class Aliases(
        val username: String,
        @JsonProperty("last_seen_at")
        val lastSeenTime: String
    )

    data class Progression(
        val level: Int,
        @JsonProperty("lootbox_probability")
        val alphaBoxProbability: Double,
        @JsonProperty("total_xp")
        val totalXp: Long
    )

    data class GameStats(
        @JsonProperty("general")
        val generalStat: GeneralStat,
        @JsonProperty("queue")
        val queue: JsonNode, //Queue,
        @JsonProperty("gamemode")
        val gameMode: JsonNode, //GameMode,
        @JsonProperty("timestamps")
        val timeStamp: TimeStamp
    ) {
        data class GeneralStat(
            /**
             * 助攻数
             */
            @JsonProperty("assists")
            val assists: Long,
            /**
             * 部署过的阻挡物数
             */
            @JsonProperty("barricades_deployed")
            val deployedBarricades: Long,
            /**
             * 盲杀数量
             */
            @JsonProperty("blind_kills")
            val blindKills: Long,
            @JsonProperty("bullets_fired")
            val firedBullets: Long,
            @JsonProperty("bullets_hit")
            val hitBullets: Long,
            /**
             * DBNO (Down But Not Out / 倒了还没死) 次数
             *
             * 详见: https://data.fandom.com/wiki/DBNO
             */
            @JsonProperty("dbnos")
            val dbno: Long,
            @JsonProperty("deaths")
            val deaths: Long,
            @JsonProperty("distance_travelled")
            val travelledDistance: Long,
            @JsonProperty("draws")
            val draws: Long,
            @JsonProperty("gadgets_destroyed")
            val destroyedGadgets: Long,
            @JsonProperty("games_played")
            val playedGameTime: Long,
            @JsonProperty("headshots")
            val headshotTime: Long,
            @JsonProperty("kd")
            val kd: Double,
            @JsonProperty("kills")
            val kills: Long,
            @JsonProperty("losses")
            val lossTime: Long,
            /**
             * 近战击杀次数
             */
            @JsonProperty("melee_kills")
            val meleeKills: Long,
            /**
             * 穿透击杀次数
             */
            @JsonProperty("penetration_kills")
            val penetrationKills: Long,
            @JsonProperty("playtime")
            val playTime: Long,
            @JsonProperty("rappel_breaches")
            val rappelBreaches: Long,
            @JsonProperty("reinforcements_deployed")
            val deployedReinforcements: Long,
            @JsonProperty("revives")
            val reviveTime: Long,
            @JsonProperty("suicides")
            val suicideTime: Long,
            @JsonProperty("wins")
            val winTime: Long,
            @JsonProperty("wl")
            val winLoss: Double
        )

        //data class Queue()

        //data class GameMode()

        data class TimeStamp(
            val created: String,
            @JsonProperty("last_updated")
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