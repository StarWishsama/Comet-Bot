package io.github.starwishsama.namelessbot.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

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
    private static Gson gson = new GsonBuilder().serializeNulls().setLenient().create();

    public static List<BiliLiver> getBiliLivers() throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(BotConstants.cfg.getLiveApi()).openConnection();
        conn.setRequestProperty("Accept-Charset", "utf-8");
        conn.setRequestProperty("contentType", "utf-8");
        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK){
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            JsonElement result = JsonParser.parseString(br.readLine());
            if (result != null && !result.isJsonNull()) {
                // 去除特殊字符
                return gson.fromJson(result, new TypeToken<List<BiliLiver>>(){}.getType());
            }
        }
        return new ArrayList<>();
    }

    public static BiliLiver getBiliLiver(String name) throws IOException {
        List<BiliLiver> livers = getBiliLivers();
        for (BiliLiver liver : livers){
            if (liver.getVtuberName().equals(name)){
                return liver;
            }
        }
        return null;
    }
}
