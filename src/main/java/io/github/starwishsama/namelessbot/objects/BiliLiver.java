package io.github.starwishsama.namelessbot.objects;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

@Data
public class BiliLiver {
    public int mid;
    public String uuid;
    public String uname;
    public int video;
    public int roomid;
    // 签名
    public String sign;
    @SerializedName("notice")
    // 直播间公告
    public String announcement;
    @SerializedName("face")
    public String faceImgLink;
    // 这是什么?
    public int rise;
    public String topPhoto;
    public int archiveView;
    public int follower;
    public int liveStatus;
    @SerializedName("recordNum")
    public int recordNumber;
    @SerializedName("guardNum")
    public int guardNumber;
    public LastLive lastLive;
    public int guardChange;
    public List<Integer> guardType;
    public int areaRank;
    public int online;
    @SerializedName("title")
    public String liveTitle;
    public long time;

    public boolean isStreaming(){
        return getLiveStatus() == 1;
    }
}
