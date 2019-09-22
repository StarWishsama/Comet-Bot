package io.github.starwishsama.namelessbot.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class SongIDGetter {
    // 仅供测试
    public static void main(String[] args){
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        boolean quit = false;

        while (!quit){
            try {
                System.out.println("输入你要搜索的歌名 / Type song name you want to search (By Netease Cloud Music): ");
                String input = in.readLine();

                if (input == null || input.isEmpty()){
                    System.out.println("Exit!");
                    quit = true;
                } else {
                    System.out.println(searchQQMusic(input));
                    //System.out.println(getNetEaseSongID(input));
                }
            } catch (Exception x) {
                x.printStackTrace(System.out);
                System.out.println("Can't request song(s) from API.");
            }
        }
    }

    public static int getQQMusicSongID(String name){
        if (name != null) {
            try {
                String result = searchQQMusic(name);
                System.out.println(result);
                return Integer.parseInt(result);
            } catch (Exception x) {
                return -1;
            }
        } else {
            return -1;
        }
    }


    public static int getNetEaseSongID(String name){
        if (name != null)
            try {
                JSONObject object = JSON.parseObject(searchNetEaseMusic(name));
                return object.getJSONObject("result").getJSONArray("songs").getJSONObject(0).getInteger("id");
            } catch (IOException x) {
                return -1;
            }
        else // Name is null
            return -1;
    }

    public static String getNetEaseSongID(String name, String authorName){
        return null;
    }

    private static String searchNetEaseMusic(String songName) throws IOException {
        if (songName != null){
            URL url = new URL("http://localhost:3000/search?keywords=" + URLEncoder.encode(songName, "UTF-8"));
            System.out.println("Encoded url is " + url.toString());
            System.out.println("Decoded url is " + URLDecoder.decode(url.toString(), "UTF-8"));
            HttpURLConnection hc = (HttpURLConnection) url.openConnection();
            if (hc.getResponseCode() == 200) {
                InputStream is = url.openStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                JSONObject songObject = JSONObject.parseObject(br.readLine());
                if (songObject.getJSONObject("result").getJSONArray("songs") != null) {
                    /**int songId = songObject.getJSONObject("result").getJSONArray("songs").getJSONObject(0).getInteger("id");
                    String songName = songObject.getJSONObject("result").getJSONArray("songs").getJSONObject(0).getString("name");
                    String songArtist = "None";

                    if (songObject.getJSONObject("result").getJSONArray("songs").getJSONObject(0).getJSONArray("artists") != null) {
                        JSONArray artists = songObject.getJSONObject("result").getJSONArray("songs").getJSONObject(0).getJSONArray("artists");
                        StringBuffer sb = new StringBuffer();
                        for (int i = 0; i < artists.size(); i++)
                            sb.append(artists.getJSONObject(i).getString("name")).append(" ");
                        songArtist = sb.toString();
                    } */

                    return (songObject.toJSONString());
                } else
                    return "Can't request song(s) from API, Please wait a moment.";
            } else
                return "Can't request song(s) from API, Response code is " + hc.getResponseCode();
        } else
            return "Please assign a song name";
    }

    private static String searchNetEaseMusic(String songName, String authorName) throws IOException {
        String value = "Please assign a song name";
        if (songName != null){
            URL url = new URL("http://localhost:3000/search?keywords=" + URLEncoder.encode(songName, "UTF-8"));
            System.out.println("Encoded url is " + url.toString());
            System.out.println("Decoded url is " + URLDecoder.decode(url.toString(), "UTF-8"));
            HttpURLConnection hc = (HttpURLConnection) url.openConnection();
            if (hc.getResponseCode() == 200) {
                InputStream is = url.openStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                JSONObject songObject = JSONObject.parseObject(br.readLine());
                if (songObject.getJSONObject("result").getJSONArray("songs") != null) {
                    for (int i = 0; i < songObject.getJSONObject("result").getJSONArray("songs").size(); i++){
                        for (int x = 0; x < songObject.getJSONObject("result").getJSONArray("songs").getJSONObject(i).getJSONArray("artists").size(); x++){
                            if (songObject.getJSONObject("result").getJSONArray("songs").getJSONObject(x).getJSONArray("artists").getJSONObject(x).getString("name").equals(authorName)){
                                value = (songObject.getJSONObject("result").getJSONArray("songs").getJSONObject(x).toJSONString());
                            }
                        }
                    }
                } else
                    value = "Can't request song(s) from API, Please wait a moment.";
            } else
                value = "Can't request song(s) from API, Response code is " + hc.getResponseCode();
        }
        return value;
    }

    private static String searchQQMusic(String name){
        if (name != null) {
            try {
                URL url = new URL("https://c.y.qq.com/soso/fcgi-bin/client_search_cp?g_tk=5381&p=1&n=20&w=" + URLEncoder.encode(name, "UTF-8") + "&format=json&loginUin=0&hostUin=0&inCharset=utf8&outCharset=utf-8&notice=0&platform=yqq&needNewCode=0&remoteplace=txt.yqq.song&t=0&aggr=1&cr=1&catZhida=0&flag_qc=0");
                System.out.println("Encoded url is " + url.toString());
                System.out.println("Decoded url is " + URLDecoder.decode(url.toString(), "UTF-8"));
                HttpURLConnection hc = (HttpURLConnection) url.openConnection();
                if (hc.getResponseCode() == 200) {
                    InputStream is = url.openStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                    JSONObject songObject = JSONObject.parseObject(br.readLine());
                    if (songObject.getJSONObject("data").getJSONObject("song").getJSONArray("list") != null) {
                        JSONArray songs = songObject.getJSONObject("data").getJSONObject("song").getJSONArray("list");
                        if (songs.getJSONObject(0).getInteger("songid") != null){
                            return String.valueOf(songs.getJSONObject(0).getInteger("songid"));
                        } else
                            return "Can't request song(s) from API.";
                    } else
                        return "Can't request song(s) from API, Please wait a moment.";
                } else
                    return "Can't request song(s) from API, Response code is " + hc.getResponseCode();
            } catch (Exception x) {
                x.printStackTrace();
                return "Exception";
            }
        } else {
            return "No name";
        }
    }
}
