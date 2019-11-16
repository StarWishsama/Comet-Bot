package io.github.starwishsama.namelessbot.utils;

import com.gitlab.siegeinsights.r6tab.api.R6TabApi;
import com.gitlab.siegeinsights.r6tab.api.R6TabApiException;
import com.gitlab.siegeinsights.r6tab.api.R6TabPlayerNotFoundException;
import com.gitlab.siegeinsights.r6tab.api.entity.player.Player;
import com.gitlab.siegeinsights.r6tab.api.entity.search.Platform;
import com.gitlab.siegeinsights.r6tab.api.impl.R6TabApiImpl;
import io.github.starwishsama.namelessbot.BotMain;

public class R6SUtils {
    static R6TabApi api = new R6TabApiImpl();

    public static Player getR6SInfo(String player) {
        try {
            if (BotUtils.isLegitID(player)) {
                Player p = api.getPlayerByUUID(api.searchPlayer(player, Platform.UPLAY).getResults().get(0).getUserUuid());
                if (p.isPlayerFound())
                    return p;
            }
        } catch (R6TabApiException | R6TabPlayerNotFoundException e){
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
            if (p == null) {
                if (BotUtils.isLegitID(player)) {
                    Player searchPlayer = api.getPlayerByUUID(api.searchPlayer(player, p).getResults().get(0).getUserUuid());
                    if (searchPlayer.isPlayerFound()) {
                        return searchPlayer;
                    }
                }
            } return null;
        } catch (R6TabApiException | R6TabPlayerNotFoundException e){
            BotMain.getLogger().warning("在获取 R6 玩家信息时出现了问题, " + e);
        }
        return null;
    }
}

