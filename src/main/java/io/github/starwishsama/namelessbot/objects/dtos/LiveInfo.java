package io.github.starwishsama.namelessbot.objects.dtos;

import lombok.Data;

@Data
public class LiveInfo {

    /**
     * code : 0
     * message : 0
     * ttl : 1
     * data : {"roomStatus":1,"roundStatus":1,"liveStatus":0,"url":"https://live.bilibili.com/21752686","title":"桐生可可Official的投稿视频","cover":"http://i0.hdslb.com/bfs/live/new_room_cover/cd2bad4b6ab83cd4bd4c85e625aad90b8adf719c.jpg","online":5297,"roomid":21752686,"broadcast_type":0,"online_hidden":0}
     */

    private int code;
    private String message;
    private int ttl;
    private DataBean data;

    @Data
    public static class DataBean {
        /**
         * roomStatus : 1
         * roundStatus : 1
         * liveStatus : 0
         * url : https://live.bilibili.com/21752686
         * title : 桐生可可Official的投稿视频
         * cover : http://i0.hdslb.com/bfs/live/new_room_cover/cd2bad4b6ab83cd4bd4c85e625aad90b8adf719c.jpg
         * online : 5297
         * roomid : 21752686
         * broadcast_type : 0
         * online_hidden : 0
         */

        private int roomStatus;
        private int roundStatus;
        private int liveStatus;
        private String url;
        private String title;
        private String cover;
        private int online;
        private int roomid;
        private int broadcast_type;
        private int online_hidden;
    }
}
