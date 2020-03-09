package io.github.starwishsama.namelessbot.utils;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.CharsetUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import io.github.starwishsama.namelessbot.BotConstants;
import io.github.starwishsama.namelessbot.objects.BiliBiliUser;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class BiliUtils {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().setLenient().serializeNulls().create();
    @Getter
    private static List<BiliBiliUser> userCache = new ArrayList<>();

    private static String getApiJson() throws IOException {
        String response = "";
        if (BotConstants.cfg.getLiveApi() != null) {
            URL url = new URL(BotConstants.cfg.getLiveApi());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                response = Convert.convertCharset(br.readLine(), CharsetUtil.GBK, CharsetUtil.UTF_8);
                br.close();
                conn.disconnect();
            }
        }
        return response;
    }

    public static void refreshUserCache(){
        try {
            String json = getApiJson();
            if (!json.isEmpty()){
                JsonElement element = JsonParser.parseString(json);
                if (!element.isJsonNull()){
                    userCache = GSON.fromJson(json, new TypeToken<ArrayList<BiliBiliUser>>(){}.getType());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static BiliBiliUser getUserByName(String userName){
        if (!userCache.isEmpty()){
            for (BiliBiliUser user : userCache){
                if (user.getUserName().contains(userName)) {
                    return user;
                }
            }
        }
        return null;
    }

    public static boolean getLiveStatus(BiliBiliUser user){
        if (user != null){
            return user.getOnline() != 0 && !user.getTitle().contains("投稿视频");
        }
        return false;
    }
}
