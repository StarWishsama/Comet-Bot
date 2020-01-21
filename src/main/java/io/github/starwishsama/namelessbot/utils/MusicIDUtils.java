package io.github.starwishsama.namelessbot.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.github.starwishsama.namelessbot.BotMain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class MusicIDUtils {
    // 仅供测试
    public static void main(String[] args){
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in), 1024);
        boolean quit = false;

        while (!quit){
            try {
                System.out.println("输入你要搜索的歌名 / Type song name you want to search (By Netease Cloud Music): ");
                String input = in.readLine();

                if (input == null || input.isEmpty()){
                    System.out.println("Exit!");
                    quit = true;
                } else {
                    System.out.println(getQQMusicSongID(input));
                    System.out.println(getNetEaseSongID(input));
                }
            } catch (Exception x) {
                x.printStackTrace(System.out);
                System.out.println("Can't request song(s) from API.");
            }
        }
    }

    public static int getQQMusicSongID(String name){
        if (name != null) {
            JsonArray songs = searchQQMusic(name);
            if (songs.get(0).getAsJsonObject().get("songid") != null){
                return songs.get(0).getAsJsonObject().get("songid").getAsInt();
            }
        }
        return -1;
    }


    public static int getNetEaseSongID(String name){
        if (name != null)
            try {
                return searchNetEaseMusic(name).get(0).getAsJsonObject().get("id").getAsInt();
            } catch (IOException ignored) {
            }
        return -1;
    }

    private static JsonArray searchNetEaseMusic(String songName) throws IOException {
        if (songName != null){
            URL url = new URL("http://localhost:3000/search?keywords=" + URLEncoder.encode(songName, "UTF-8"));
            HttpURLConnection hc = (HttpURLConnection) url.openConnection();
            if (hc.getResponseCode() == 200) {
                InputStream is = url.openStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                JsonObject object = (JsonObject) new JsonParser().parse(br.readLine());
                if (!object.isJsonNull()){
                    return object.getAsJsonObject("result").getAsJsonArray("songs");
                } else
                    BotMain.getLogger().debug("Can't request song(s) from API, Please wait a moment.");
            } else
                BotMain.getLogger().debug("Can't request song(s) from API, Response code is " + hc.getResponseCode());
        }
        return null;
    }

    private static JsonArray searchQQMusic(String name){
        if (name != null) {
            try {
                URL url = new URL("https://c.y.qq.com/soso/fcgi-bin/client_search_cp?g_tk=5381&p=1&n=20&w=" + URLEncoder.encode(name, "UTF-8") + "&format=json&loginUin=0&hostUin=0&inCharset=utf8&outCharset=utf-8&notice=0&platform=yqq&needNewCode=0&remoteplace=txt.yqq.song&t=0&aggr=1&cr=1&catZhida=0&flag_qc=0");
                HttpURLConnection hc = (HttpURLConnection) url.openConnection();
                if (hc.getResponseCode() == 200) {
                    InputStream is = url.openStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                    JsonObject object = (JsonObject) new JsonParser().parse(br.readLine());
                    if (!object.isJsonNull()){
                        if (object.getAsJsonObject("data").getAsJsonObject("song").getAsJsonArray("list") != null){
                            return object.getAsJsonObject("data").getAsJsonObject("song").getAsJsonArray("list");
                        } else
                            BotMain.getLogger().debug("Can't request song(s) from API, Please wait a moment.");
                    }
                } else
                    BotMain.getLogger().debug("无法从 API 获取到歌曲信息, 响应码为 " + hc.getResponseCode());
            } catch (Exception x) {
                BotMain.getLogger().warning("在通过 QQ 音乐搜索歌曲时发生了一个错误, " + x.getMessage());
            }
        }
        return null;
    }
}
