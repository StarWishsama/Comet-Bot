package io.github.starwishsama.namelessbot.objects.dtos;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

@Deprecated
@Data
public class BiliBiliUser {
    private int mid;
    private String uuid;
    @SerializedName("uname")
    private String userName;
    private int video;
    private int roomid;
    private String sign;
    private String notice;
    private String face;
    private int rise;
    private String topPhoto;
    private int archiveView;
    private int follower;
    private int liveStatus;
    private int recordNum;
    private int guardNum;
    private LastLiveBean lastLive;
    private int guardChange;
    private int areaRank;
    private int online;
    private String title;
    private long time;
    private List<Integer> guardType;

    @Data
    public static class LastLiveBean {
        private int online;
        private long time;
    }
}
