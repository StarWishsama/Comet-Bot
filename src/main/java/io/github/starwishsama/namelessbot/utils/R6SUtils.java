package io.github.starwishsama.namelessbot.utils;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.google.gson.*;
import io.github.starwishsama.namelessbot.enums.R6Rank;
import io.github.starwishsama.namelessbot.objects.rainbowsix.R6Player;

import java.text.NumberFormat;

public class R6SUtils {
    private static final String infoText = "=== 彩虹六号战绩查询 ===\n%s [%d级]" +
            "\n目前段位: %s current mmrchange" +
            "\nKD: %s" +
            "\n爆头率: %s";
    private static final NumberFormat num = NumberFormat.getPercentInstance();
    private final static Gson gson = new GsonBuilder().serializeNulls().setLenient().create();

    public static R6Player searchPlayer(String name){
        try {
            HttpResponse hr = HttpRequest.get("https://r6.apitab.com/search/uplay/" + name).timeout(5000).executeAsync();
            if (hr.isOk()){
                String body = hr.body();
                if (body != null && isValidJson(body)){
                    JsonElement element = JsonParser.parseString(body).getAsJsonObject().get("players");
                    if (isValidJson(element)) {
                        JsonObject object = element.getAsJsonObject();
                        String uuid = object.get(object.keySet().iterator().next()).getAsJsonObject().get("profile").getAsJsonObject().get("p_user").getAsString();
                        HttpResponse hr2 = HttpRequest.get("https://r6.apitab.com/player/" + uuid).timeout(5000).setFollowRedirects(true).executeAsync();
                        if (hr2.isOk()) {
                            return gson.fromJson(hr2.body(), R6Player.class);
                        }
                    }
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static String getR6SInfo(String player) {
        try {
            if (BotUtils.isLegitId(player)) {
                R6Player p = searchPlayer(player);
                if (p != null && p.isFound()) {
                    num.setMaximumIntegerDigits(3);
                    num.setMaximumFractionDigits(2);
                    String response = String.format(infoText, p.getPlayer().getP_name(), p.getStats().getLevel(),
                            R6Rank.getRank(p.getRanked().getAS_rank()).getName(), String.format("%.2f", p.getStats().getGeneralpvp_kd()),
                            num.format(p.getStats().getGeneralpvp_headshot() / (double) p.getStats().getGeneralpvp_kills()));

                    if (R6Rank.getRank(p.getRanked().getAS_rank()) != R6Rank.UNRANKED) {
                        response = response.replaceAll("current", p.getRanked().getAS_mmr()
                                + "");
                    } else {
                        response = response.replaceAll("current", "");
                    }

                    int mmrChange = p.getRanked().getAS_mmrchange();
                    if (mmrChange != 0) {
                        if (mmrChange > 0) {
                            response = response.replaceAll("mmrchange", "+" + mmrChange);
                        } else {
                            response = response.replaceAll("mmrchange", "" + mmrChange);
                        }
                    }

                    return response;
                }
            }
        } catch (Exception e){
            e.printStackTrace();
            return "在获取时发生了问题";
        }
        return "找不到此账号";
    }

    private static boolean isValidJson(String json){
        JsonElement jsonElement;
        try {
            jsonElement = JsonParser.parseString(json);
        } catch (Exception e){
            return false;
        }

        if (jsonElement == null) {
            return false;
        }

        return jsonElement.isJsonObject();
    }

    private static boolean isValidJson(JsonElement element){
        if (element == null) {
            return false;
        }

        return element.isJsonObject();
    }
}

