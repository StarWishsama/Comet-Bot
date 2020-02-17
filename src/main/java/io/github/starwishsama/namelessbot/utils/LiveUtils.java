package io.github.starwishsama.namelessbot.utils;

import cn.hutool.core.date.DateUtil;
import io.github.starwishsama.namelessbot.objects.RssItem;

public class LiveUtils {
    private final static String biliUrlOnline = "https://rsshub.app/bilibili/live/search/%s/online";
    private final static String biliUrlLastLive = "https://rsshub.app/bilibili/live/search/%s/live_time";
    private final static String biliLiveStatus = "https://rsshub.app/bilibili/live/room/";

    public static boolean isValidLiver(String liverName){
        return new RssItem(String.format(biliUrlOnline, liverName)).getContext() != null;
    }

    public static String getLastLiveTime(String liverName){
        if (isValidLiver(liverName)){
            RssItem item = new RssItem(String.format(biliUrlLastLive, liverName));
            if (item.getEntry() != null){
                return DateUtil.parseDateTime(item.getEntry().getDescription().getValue().trim()).toString("yyyy-MM-dd HH:mm:ss");
            }
        }
        return null;
    }

    public static long getLiverId(String liverName){
        if (isValidLiver(liverName)){
            RssItem item = new RssItem(String.format(biliUrlOnline, liverName));
            return Long.parseLong(item.getEntry().getLink().replace("https://live.bilibili.com/", ""));
        }
        return 0;
    }

    public static String checkLiveStatus(String liverName){
        if (isValidLiver(liverName)){
            RssItem item = new RssItem(biliLiveStatus + getLiverId(liverName));
            if (item.getEntry() != null){
                return item.getTitle().replace("直播间开播状态", "").trim() + "开播了!\n"
                        + item.getEntry().getDescription().getValue().trim() + "\n"
                        + "直播间直达链接👉 " + item.getEntry().getLink();
            }
        }
        return null;
    }

    public static String getLiver(String liverName){
        if (isValidLiver(liverName)){
            RssItem item = new RssItem(biliUrlLastLive + getLiverId(liverName));
            if (item.getEntry() != null){
                return item.getTitle() + "\n"
                        + item.getEntry().getDescription().getValue().trim() + "\n"
                        + "直播间直达链接👉 " + item.getEntry().getLink();
            }
        }
        return "找不到主播";
    }
}
