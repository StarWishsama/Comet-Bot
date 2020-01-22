package io.github.starwishsama.namelessbot.utils;

import com.google.gson.Gson;
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
    private static Gson gson = new Gson();

    public static List<BiliLiver> getBiliLivers() throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(BotConstants.cfg.getLiveApi()).openConnection();
        if (conn.getResponseCode() == 200){
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String result = br.readLine();
            if (result != null){
                // 去除特殊字符
                return gson.fromJson(gson.toJson(result), new TypeToken<List<BiliLiver>>(){}.getType());
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
