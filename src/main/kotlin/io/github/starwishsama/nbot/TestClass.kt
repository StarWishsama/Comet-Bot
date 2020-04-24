package io.github.starwishsama.nbot

import cn.hutool.core.util.RandomUtil
import cn.hutool.core.util.URLUtil
import cn.hutool.http.HttpRequest
import com.github.salomonbrys.kotson.get
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.github.starwishsama.nbot.util.R6SUtils
import net.mamoe.mirai.message.data.LightApp
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.asMessageChain
import net.mamoe.mirai.message.data.toMessage
import java.io.File
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * For test purpose
 * Run anything here
 */

fun main(){
    for (i in 0..20){
        println(RandomUtil.randomInt(0, 10000))
    }
}

fun searchNetEaseMusic(songName: String?): MessageChain {
    if (songName != null) {
        try {
            val searchResult = HttpRequest.get(BotConstants.cfg.netEaseApi + "/search?keywords=$songName").timeout(150_000).executeAsync()
            if (searchResult.isOk) {
                val result: JsonObject = JsonParser.parseString(searchResult.body()) as JsonObject
                if (result.isJsonObject) {
                    val musicId = result.getAsJsonObject("result").getAsJsonArray("songs")[0]["id"].asInt
                    val songResult = HttpRequest.get(BotConstants.cfg.netEaseApi + "/song/detail?ids=$musicId").timeout(150_000).executeAsync()
                    if (songResult.isOk) {
                        val songJson = JsonParser.parseString(songResult.body())
                        if (songJson.isJsonObject){
                            val albumUrl = songJson.asJsonObject["songs"].asJsonArray[0].asJsonObject["al"]["picUrl"].asString
                            val name = songJson.asJsonObject["songs"].asJsonArray[0].asJsonObject["name"].asString
                            var artistName = ""

                            songJson.asJsonObject["songs"].asJsonArray[0].asJsonObject["ar"].asJsonArray.forEach {
                                run {
                                    artistName += (it.asJsonObject["name"].asString + "/")
                                }
                            }

                            artistName = artistName.substring(0, artistName.length - 1)

                            val playResult = HttpRequest.get(BotConstants.cfg.netEaseApi + "/song/url?id=$musicId").timeout(150_000).executeAsync()
                            if (playResult.isOk){
                                val playJson = JsonParser.parseString(playResult.body())
                                if (playJson.isJsonObject){
                                    val playUrl = playJson["data"].asJsonArray[0]["url"].asString
                                    return LightApp("{\"app\":\"com.tencent.structmsg\",\"desc\":\"音乐\",\"view\":\"music\",\"ver\":\"0.0.0.1\"," +
                                            "\"prompt\":\"[分享]$name\",\"appID\":\"\",\"sourceName\":\"\",\"actionData\":\"\"," +
                                            "\"actionData_A\":\"\",\"sourceUrl\":\"\",\"meta\":{\"music\":{\"action\":\"\",\"android_pkg_name\":\"\"," +
                                            "\"app_type\":1,\"appid\":100495085,\"desc\":\"$artistName\",\"jumpUrl\":\"http:\\/\\/music.163.com\\/song\\/$musicId\\\"" +
                                            ",\"musicUrl\":\"${URLUtil.encode(playUrl)}\",\"preview\":\"${URLUtil.encode(albumUrl)}\"" +
                                            ",\"sourceMsgId\":\"0\",\"source_icon\":\"\",\"source_url\":\"\",\"tag\":\"网易云音乐\",\"title\":\"$name\"}}").asMessageChain()
                                }
                            }
                        }
                    }
                } else BotInstance.logger.debug("Can't request song(s) from API, Please wait a moment.")
            } else BotInstance.logger.debug("Can't request song(s) from API")
        } catch (e: IOException){
            BotInstance.logger.error(e)
        }
    }
    return "找不到歌曲".toMessage().asMessageChain()
}
