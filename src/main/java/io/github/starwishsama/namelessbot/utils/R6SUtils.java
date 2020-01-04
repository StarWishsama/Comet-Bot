package io.github.starwishsama.namelessbot.utils;

import com.gitlab.siegeinsights.r6tab.api.R6TabApi;
import com.gitlab.siegeinsights.r6tab.api.entity.player.Player;
import com.gitlab.siegeinsights.r6tab.api.entity.search.Platform;
import com.gitlab.siegeinsights.r6tab.api.entity.search.SearchResultWrapper;
import com.gitlab.siegeinsights.r6tab.api.impl.R6TabApiImpl;

import io.github.starwishsama.namelessbot.BotMain;

public class R6SUtils {
    private static R6TabApi api = new R6TabApiImpl();

    public static Player getR6SInfo(String player) {
        try {
            if (BotUtils.isLegitID(player)) {
                SearchResultWrapper result = api.searchPlayer(player, Platform.UPLAY);
                if (result != null) {
                    Player p = api.getPlayerByUUID(result.getResults().get(0).getUserUuid());
                    if (p.isPlayerFound())
                        return p;
                }
            }
        } catch (Exception e){
            BotMain.getLogger().warning("在获取 R6 玩家信息时出现了问题, " + e);
        }
        return null;
    }

    public static Player getR6SInfo(String player, String platform) {
        Platform p = null;
        switch (platform.toLowerCase()){
            case "pc":
            case "uplay":
                p = Platform.UPLAY;
                break;
            case "ps":
            case "ps4":
                p = Platform.PLAYSTATION_NETWORK;
                break;
            case "xbox":
                p = Platform.XBOX;
                break;
        }

        try {
            if (p != null) {
                if (BotUtils.isLegitID(player)) {
                    Player searchPlayer = api.getPlayerByUUID(api.searchPlayer(player, p).getResults().get(0).getUserUuid());
                    if (searchPlayer.isPlayerFound()) {
                        return searchPlayer;
                    }
                }
            }
        } catch (Exception e){
            BotMain.getLogger().warning("在获取 R6 玩家信息时出现了问题, " + e);
        }
        return null;
    }
}

