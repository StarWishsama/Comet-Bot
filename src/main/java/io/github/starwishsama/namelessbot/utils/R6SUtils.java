package io.github.starwishsama.namelessbot.utils;

import com.gitlab.siegeinsights.r6tab.api.R6TabApi;
import com.gitlab.siegeinsights.r6tab.api.R6TabApiException;
import com.gitlab.siegeinsights.r6tab.api.R6TabPlayerNotFoundException;
import com.gitlab.siegeinsights.r6tab.api.entity.player.Player;
import com.gitlab.siegeinsights.r6tab.api.entity.search.Platform;
import com.gitlab.siegeinsights.r6tab.api.entity.search.SearchResultWrapper;
import com.gitlab.siegeinsights.r6tab.api.impl.R6TabApiImpl;

import io.github.starwishsama.namelessbot.BotMain;

import java.text.NumberFormat;

public class R6SUtils {
    private static R6TabApi api = new R6TabApiImpl();
    private static String infoText = "=== 彩虹六号战绩查询 ===\n%s [%d级]" +
            "\n目前段位: %s(current)" +
            "\nKD: %s" +
            "\n爆头率: %s";
    private static NumberFormat num = NumberFormat.getPercentInstance();

    public static Player getR6SAccount(String player){
        if (BotUtils.isLegitID(player)){
            try {
                SearchResultWrapper result = api.searchPlayer(player, Platform.UPLAY);
                if (result != null){
                    Player p = api.getPlayerByUUID(result.getResults().get(0).getUserUuid());
                    if (p.isPlayerFound())
                        return p;
                }
            } catch (R6TabApiException | R6TabPlayerNotFoundException r6e){
                BotMain.getLogger().warning("在获取 R6 玩家信息时出现了问题, " + r6e);
            }
        }
        return null;
    }

    public static String getR6SInfo(String player) {
        Player p;
        try {
            if (BotUtils.isLegitID(player)) {
                SearchResultWrapper result = api.searchPlayer(player, Platform.UPLAY);
                if (result != null) {
                    p = api.getPlayerByUUID(result.getResults().get(0).getUserUuid());
                    if (p.isPlayerFound()){
                        num.setMaximumIntegerDigits(3);
                        num.setMaximumFractionDigits(2);
                        return String.format(infoText, p.getName(), p.getLevel(), p.getCurrentRank().getName(), String.format("%.2f", p.getKd()), num.format(p.getHeadshotAccuraccy() / 100000000d)).replaceAll("current", p.getCurrentMmr() + "");
                    }
                }
            }
        } catch (Exception e){
            BotMain.getLogger().warning("在获取 R6 玩家信息时出现了问题, " + e);
        }
        return "找不到此账号";
    }

    public static String getR6SInfo(String player, String platform) {
        Platform pf;
        switch (platform.toLowerCase()){
            case "ps":
            case "ps4":
                pf = Platform.PLAYSTATION_NETWORK;
                break;
            case "xbox":
                pf = Platform.XBOX;
                break;
            default:
                pf = Platform.UPLAY;
                break;
        }

        try {
            if (BotUtils.isLegitID(player)) {
                Player p = api.getPlayerByUUID(api.searchPlayer(player, pf).getResults().get(0).getUserUuid());
                if (p.isPlayerFound()) {
                    num.setMaximumIntegerDigits(3);
                    num.setMaximumFractionDigits(2);
                    return String.format(infoText, p.getName(), p.getLevel(), p.getCurrentRank().getName(), String.format("%.2f", p.getKd()), num.format(p.getHeadshotAccuraccy() / 100000000d)).replaceAll("current", p.getCurrentMmr() + "");
                }
            }
        } catch (Exception e){
            BotMain.getLogger().warning("在获取 R6 玩家信息时出现了问题, " + e);
        }
        return "找不到此账号";
    }
}

