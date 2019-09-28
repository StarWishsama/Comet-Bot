package io.github.starwishsama.namelessbot.config;

import cc.moecraft.logger.HyLogger;
import com.alibaba.fastjson.JSONObject;
import io.github.starwishsama.namelessbot.BotMain;
import io.github.starwishsama.namelessbot.utils.FileProcess;

import java.io.File;
import java.io.IOException;

public class Message {
    private static HyLogger logger = BotMain.getLogger();
    public static String botPrefix;
    public static String noPermission;
    public static String bindSuccess;
    public static String noCheckInData;

    public static void loadLang(){
        if (!new File(Config.jarPath + "lang.json").exists()){
            JSONObject lang = new JSONObject();
            lang.put("botPrefix", "Bot > ");
            lang.put("noPermission", "你没有权限!");
            lang.put("bindSuccess", "绑定账号 %s 成功!");
            lang.put("noCheckInData", "你还没有签到过, 使用 /qd <游戏ID> 注册签到系统吧~");
            FileProcess.createFile(Config.jarPath + "lang.json", lang.toJSONString());
        }
        try {
            JSONObject lang = JSONObject.parseObject(FileProcess.readFile(Config.jarPath + "lang.json"));
            botPrefix = lang.getString("botPrefix");
            noPermission = lang.getString("noPermission");
            bindSuccess = lang.getString("bindSuccess");
            noCheckInData = lang.getString("noCheckInData");
        } catch (IOException e){
            System.err.println("[配置] 在保存时发生了问题, 错误信息: " + e.getMessage());
        }
    }

    public static void saveLang(){
        if (!new File(Config.jarPath + "lang.json").exists()){
            JSONObject lang = new JSONObject();
            lang.put("botPrefix", "Bot >");
            lang.put("noPermission", "你没有权限!");
            lang.put("bindSuccess", "绑定账号 %s 成功!");
            lang.put("noCheckInData", "你还没有签到过, 使用 /qd <游戏ID> 注册签到系统吧~");
            FileProcess.createFile(Config.jarPath + "lang.json", lang.toJSONString());
        } else {
            JSONObject lang = new JSONObject();
            lang.put("botPrefix", botPrefix);
            lang.put("noPermission", noPermission);
            lang.put("bindSuccess", bindSuccess);
            FileProcess.createFile(Config.jarPath + "lang.json", lang.toJSONString());
        }
    }
}
