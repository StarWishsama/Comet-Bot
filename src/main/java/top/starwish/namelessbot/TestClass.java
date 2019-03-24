package top.starwish.namelessbot;

import com.alibaba.fastjson.JSONObject;

// 此 Class 仅供调试使用
public class TestClass {
    static RssStatus solidot = new RssStatus();
    static RssStatus jikeWakeUp = new RssStatus();
    static String msg = "!mute 1448839220 1h10m";
    static boolean isAdmin = true;
    static boolean botStatus = false;
    static long fromGroup = 0;
    static String configPath= System.getProperty("user.dir") + "\\build\\myfile.json";

    static long lastUseTime = 0L;
    static long currentTime = System.currentTimeMillis();

    public static void main(String[] args) {
        if (currentTime - lastUseTime >= 10000) {
            lastUseTime = currentTime;
            System.out.println("success");
        } else {
            System.out.println("You have to wait: " + (10000 - (currentTime - lastUseTime)) + " more ms!");
        }
    }

    
    public static void saveConf() {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("botStatus", botStatus);
        jsonObject.put("solidot", solidot.getStatus());
        jsonObject.put("jikeWakeUp", jikeWakeUp.getStatus());
        if (!FileProcess.createFile(configPath, jsonObject.toJSONString()))
            System.out.println("[ERROR] "+ "Encountered an error when saving configuration!");

    }
}