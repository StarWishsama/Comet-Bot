package io.github.starwishsama.namelessbot.utils;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import io.github.starwishsama.namelessbot.enums.LiveStatus;
import io.github.starwishsama.namelessbot.objects.dtos.LiveInfo;
import lombok.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Nameless
 * B站部分信息获取
 * 建议异步调用
 */

public class BiliUtils {
    private static final String API_URL = "https://api.live.bilibili.com/room/v1/Room/getRoomInfoOld?mid=";
    private static final String ROOM_INFO_URL = "https://api.live.bilibili.com/room/v1/Room/room_init?id=";
    private static final String USER_INFO_URL = "http://api.bilibili.com/x/space/acc/info?mid=";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().create();

    // liveStatus = 1
    public static LiveStatus isLiveNow(LiveInfo info){
        if (info != null){
            return info.getData().getLiveStatus() == 1 ? LiveStatus.ONLINE : LiveStatus.OFFLINE;
        }
        return LiveStatus.NOT_FOUND;
    }

    public static LiveInfo getLiveInfo(@NonNull Long mid){
        HttpResponse response = HttpRequest.get(API_URL + mid).timeout(5000).executeAsync();
        if (response.header("Content-Type").contains("application/json") && JsonParser.parseString(response.body()).isJsonObject()){
            return GSON.fromJson(response.body(), LiveInfo.class);
        }
        return null;
    }

    public static String getLiveStartTime(LiveInfo liveInfo){
        if (liveInfo != null){
            HttpResponse response = HttpRequest.get(ROOM_INFO_URL + liveInfo.getData().getRoomid()).timeout(150_000).executeAsync();
            if (response.header("Content-Type").equals("application/json") && JsonParser.parseString(response.body()).isJsonObject()){
                long time = JsonParser.parseString(response.body()).getAsJsonObject().get("data").getAsJsonObject().get("live_time").getAsLong();
                if (time > 0) {
                    return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(time * 1000));
                } else
                    return "没有记录 (受B站API限制, 只有开播后才能查看开播时间~)";
            }
        }
        return "";
    }

    public static String getNameById(Long mid){
        HttpResponse response = HttpRequest.get(USER_INFO_URL + mid).timeout(150_000).executeAsync();
        if (response.header("Content-Type").contains("application/json") && JsonParser.parseString(response.body()).isJsonObject()){
            return JsonParser.parseString(response.body()).getAsJsonObject().get("data").getAsJsonObject().get("name").getAsString();
        }
        return "";
    }

    public static String getStatusText(Long mid){
        try {
            LiveInfo info = getLiveInfo(mid);

            if (info != null) {
                String name = getNameById(mid);
                switch (isLiveNow(info)) {
                    case ONLINE:
                        return name + "正在直播!\n开播时间 " + getLiveStartTime(info) + "\n直播间标题 " + info.getData().getTitle() + "\n";
                    case OFFLINE:
                        return name + "还没有开播";
                    case NOT_FOUND:
                        return "找不到这个用户";
                    default:
                        return "发生了意料之外的错误";
                }
            } else {
                return "这个主播不存在";
            }
        } catch (Exception e){
            e.printStackTrace();
            return "在获取时发生了问题";
        }
    }
}
