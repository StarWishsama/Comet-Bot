package io.github.starwishsama.namelessbot;

import io.github.starwishsama.namelessbot.entities.RssItem;

public class RSSPusher {
    private static RssItem paperClip = new RssItem("https://rsshub.app/bilibili/user/video/258150656");

    public static String getLatestVideo(){
        if (paperClip.getContext() != null){
            return paperClip.getContext();
        } else
            return "无法获取最新视频";
    }
}
