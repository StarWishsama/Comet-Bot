package io.github.starwishsama.namelessbot.config;

import com.google.gson.*;

import io.github.starwishsama.namelessbot.BotMain;
import io.github.starwishsama.namelessbot.utils.FileProcess;

import java.io.File;
import java.io.IOException;

public class Message {
    public static String botPrefix;
    public static String noPermission;
    public static String bindSuccess;
    public static String noCheckInData;

    private static File langCfg = new File(BotMain.jarPath + "lang.json");

    public static void loadLang(){
        if (!langCfg.exists()){
            JsonObject lang = new JsonObject();
            lang.addProperty("botPrefix", "Bot > ");
            lang.addProperty("noPermission", "你没有权限!");
            lang.addProperty("bindSuccess", "绑定账号 %s 成功!");
            lang.addProperty("noCheckInData", "你还没有签到过, 使用 /qd <游戏ID> 注册签到系统吧~");
            System.out.println(new Gson().toJson(lang));
            FileProcess.createFile(langCfg.toString(), new Gson().toJson(lang));

            //JSONObject lang = new JSONObject();
            //lang.put("botPrefix", "Bot > ");
            //lang.put("noPermission", "你没有权限!");
            //lang.put("bindSuccess", "绑定账号 %s 成功!");
            //lang.put("noCheckInData", "你还没有签到过, 使用 /qd <游戏ID> 注册签到系统吧~");
            //FileProcess.createFile(langCfg.toString(), lang.toJSONString());
        } else {
            try {
                JsonParser langParser = new JsonParser();
                JsonElement lang = langParser.parse(FileProcess.readFile(langCfg.toString()));

                if (!lang.isJsonNull()) {
                    botPrefix = lang.getAsJsonObject().get("botPrefix").getAsString();
                    noPermission = lang.getAsJsonObject().get("noPermission").getAsString();
                    bindSuccess = lang.getAsJsonObject().get("bindSuccess").getAsString();
                    noCheckInData = lang.getAsJsonObject().get("noCheckInData").getAsString();
                } else
                    System.err.println("[配置] 在读取时发生了问题, JSON 文件为空");

                //JSONObject lang = JSONObject.parseObject(FileProcess.readFile(langCfg.toString()));
                //botPrefix = lang.getString("botPrefix");
                //noPermission = lang.getString("noPermission");
                //bindSuccess = lang.getString("bindSuccess");
                //noCheckInData = lang.getString("noCheckInData");
            } catch (IOException e) {
                System.err.println("[配置] 在读取时发生了问题, 错误信息: " + e);
            }
        }
    }

    public static void saveLang(){
        if (!langCfg.exists()){
            JsonObject lang = new JsonObject();
            Gson gson = new Gson();
            lang.addProperty("botPrefix", "Bot >");
            lang.addProperty("noPermission", "你没有权限!");
            lang.addProperty("bindSuccess", "绑定账号 %s 成功!");
            lang.addProperty("noCheckInData", "你还没有签到过, 使用 /qd <游戏ID> 注册签到系统吧~");

            //JSONObject lang = new JSONObject();
            //lang.put("botPrefix", "Bot >");
            //lang.put("noPermission", "你没有权限!");
            //lang.put("bindSuccess", "绑定账号 %s 成功!");
            //lang.put("noCheckInData", "你还没有签到过, 使用 /qd <游戏ID> 注册签到系统吧~");
            FileProcess.createFile(langCfg.toString(), gson.toJson(lang));
        } else {
            JsonObject lang = new JsonObject();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            lang.addProperty("botPrefix", botPrefix);
            lang.addProperty("noPermission", noPermission);
            lang.addProperty("bindSuccess", bindSuccess);
            lang.addProperty("noCheckInData", noCheckInData);
            //JSONObject lang = new JSONObject();
            //lang.put("botPrefix", botPrefix);
            //lang.put("noPermission", noPermission);
            //lang.put("bindSuccess", bindSuccess);
            FileProcess.createFile(langCfg.toString(), gson.toJson(lang));
        }
    }
}
