package io.github.starwishsama.namelessbot.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import com.google.gson.stream.JsonReader;
import io.github.starwishsama.namelessbot.BotConstants;
import io.github.starwishsama.namelessbot.objects.BiliLiver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class LiveUtils {
    private static Gson gson = new GsonBuilder().setLenient().create();

    public static List<BiliLiver> getBiliLivers() throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(BotConstants.cfg.getLiveApi()).openConnection();
        conn.connect();
        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK){
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            return gson.fromJson(br.readLine().trim(), new TypeToken<List<BiliLiver>>(){}.getType());
        }
        conn.disconnect();
        return new ArrayList<>();
    }

    public static BiliLiver getBiliLiver(String name) throws IOException {
        List<BiliLiver> result = getBiliLiverList(name);
        if (!result.isEmpty()){
            return result.get(0);
        }
        return null;
    }

    public static List<BiliLiver> getBiliLiverList(String name) throws IOException {
        List<BiliLiver> livers = getBiliLivers();
        List<BiliLiver> result = new ArrayList<>();
        if (livers != null) {
            for (BiliLiver liver : livers) {
                if (liver.getUname().contains(name)) {
                    result.add(liver);
                }
            }
        }
        return result;
    }
}
