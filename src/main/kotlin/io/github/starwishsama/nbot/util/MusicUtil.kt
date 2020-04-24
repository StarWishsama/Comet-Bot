package io.github.starwishsama.nbot.util

import cn.hutool.core.util.URLUtil
import cn.hutool.http.HttpRequest
import com.github.salomonbrys.kotson.get
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.github.starwishsama.nbot.BotConstants
import io.github.starwishsama.nbot.BotInstance
import net.mamoe.mirai.message.data.*
import java.io.IOException
import java.net.URLEncoder


object MusicUtil {
    fun searchNetEaseMusic(songName: String?): MessageChain {
        if (songName != null) {
            try {
                val searchResult = HttpRequest.get("http://${BotConstants.cfg.netEaseApi}/search?keywords=${URLUtil.encode(songName)}").timeout(150_000).executeAsync()
                if (searchResult.isOk) {
                    val result: JsonObject = JsonParser.parseString(searchResult.body()) as JsonObject
                    if (result.isJsonObject) {
                        val musicId = result.getAsJsonObject("result").getAsJsonArray("songs")[0]["id"].asInt
                        val musicUrl = "https://music.163.com/#/song?id=$musicId"
                        val songResult = HttpRequest.get("http://${BotConstants.cfg.netEaseApi}/song/detail?ids=$musicId").timeout(150_000).executeAsync()
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

                                val playResult = HttpRequest.get("http://${BotConstants.cfg.netEaseApi}/song/url?id=$musicId").timeout(150_000).executeAsync()
                                if (playResult.isOk){
                                    val playJson = JsonParser.parseString(playResult.body())
                                    if (playJson.isJsonObject){
                                        val playUrl = playJson["data"].asJsonArray[0]["url"].asString
                                        return LightApp("{\n" +
                                                "    \"app\":\"com.tencent.structmsg\",\n" +
                                                "    \"desc\":\"音乐\",\n" +
                                                "    \"meta\":{\n" +
                                                "    \"music\":{\n" +
                                                "        \"action\":\"\",\n" +
                                                "        \"android_pkg_name\":\"\",\n" +
                                                "        \"app_type\":1,\n" +
                                                "        \"appid\":\"100495085\",\n" +
                                                "        \"desc\":\"$artistName\",\n" +
                                                "        \"jumpUrl\":\"$musicUrl\",\n" +
                                                "        \"musicUrl\":\"$playUrl\",\n" +
                                                "        \"preview\":\"$albumUrl\",\n" +
                                                "        \"sourceMsgId\":0,\n" +
                                                "        \"source_icon\":\"\",\n" +
                                                "        \"source_url\":\"\",\n" +
                                                "        \"tag\":\"网易云音乐\",\n" +
                                                "        \"title\":\"$name\"\n" +
                                                "    }\n" +
                                                "},\n" +
                                                "\"prompt\":\"[分享 $name]\",\n" +
                                                "\"ver\":\"0.0.0.1\",\n" +
                                                "\"view\":\"music\"\n" +
                                                "}").asMessageChain()
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: IOException){
                BotInstance.logger.error(e)
            }
        }
        return "找不到歌曲".toMessage().asMessageChain()
    }

    private fun searchQQMusic(name: String?): JsonArray? {
        if (name != null) {
            try {
                val songResult = HttpRequest.get("https://c.y.qq.com/soso/fcgi-bin/client_search_cp?g_tk=5381&p=1&n=20&w=${URLEncoder.encode(name, "UTF-8")}&format=json&loginUin=0&hostUin=0&inCharset=utf8&outCharset=utf-8&notice=0&platform=yqq&needNewCode=0&remoteplace=txt.yqq.song&t=0&aggr=1&cr=1&catZhida=0&flag_qc=0").timeout(150_000).executeAsync()
                if (songResult.isOk) {
                    val json= JsonParser.parseString(songResult.body())
                    if (!json.isJsonNull) {
                        return json.asJsonObject["data"].asJsonObject["song"]["list"].asJsonArray
                    }
                } else BotInstance.logger.debug("无法从 API 获取到歌曲信息, 响应码为 " + songResult.status)
            } catch (x: Exception) {
                BotInstance.logger.error("在通过 QQ 音乐搜索歌曲时发生了一个错误, ", x)
            }
        }
        return null
    }
}