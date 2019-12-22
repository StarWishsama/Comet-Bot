package io.github.starwishsama.namelessbot.config;

import com.google.gson.*;
import io.github.starwishsama.namelessbot.BotMain;
import io.github.starwishsama.namelessbot.utils.FileProcess;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class BotCfg {
    public static Users users = new Users();
    public static BotLocalization msg = new BotLocalization();
    public static ShopItems shopItems = new ShopItems();
    public static Config cfg = new Config();

    private static File userCfg = new File(BotMain.getJarPath() + "/users.json");
    private static File shopItemCfg = new File(BotMain.getJarPath() + "/items.json");
    private static File cfgFile = new File(BotMain.getJarPath() + "/config.json");
    private static File langCfg = new File(BotMain.getJarPath() + "/lang.json");

    private static Gson gson = new GsonBuilder().serializeNulls().create();

    public static void loadCfg(){
        if (BotMain.getJarPath() != null) {
            if (userCfg.exists() && cfgFile.exists()){
                load();
            } else {
                try {
                    cfg.setOwnerID(0);
                    cfg.setAutoSaveTime(15);
                    cfg.setBotAdmins(new ArrayList<>());
                    cfg.setPostPort(5700);
                    cfg.setPostUrl("127.0.0.1");
                    cfg.setBotName("Bot");
                    cfg.setBotPort(5702);
                    cfg.setRconUrl("127.0.0.1");
                    cfg.setRconPort(25575);
                    cfg.setRconPwd("password".getBytes());
                    cfg.setNetEaseApi("http://localhost:3000/");
                    cfg.setCmdPrefix(new String[]{"/", "#"});
                    cfg.setBindMCAccount(false);

                    FileProcess.createFile(cfgFile.toString(), gson.toJson(cfg));
                    FileProcess.createFile(userCfg.toString(), gson.toJson(users));
                    FileProcess.createFile(shopItemCfg.toString(), gson.toJson(shopItems));

                    load();
                    System.out.println("[配置] 已自动生成新的配置文件.");
                } catch (Exception e){
                    System.err.println("[配置] 在生成配置文件时发生了错误, 错误信息: " + e.getMessage());
                }
            }
        }
    }

    public static void saveCfg(){
        try {
            FileProcess.createFile(userCfg.toString(), gson.toJson(users));
            FileProcess.createFile(shopItemCfg.toString(), gson.toJson(shopItems));
            FileProcess.createFile(cfgFile.toString(), gson.toJson(cfg));
        } catch (Exception e){
            System.err.println("[配置] 在保存配置文件时发生了问题, 错误信息: ");
            e.printStackTrace();
        }
    }

    private static void load(){
        try {
            JsonElement checkInParser = new JsonParser().parse(FileProcess.readFile(userCfg.toString()));
            JsonElement configParser = new JsonParser().parse(FileProcess.readFile(cfgFile.toString()));
            if (!checkInParser.isJsonNull() && !configParser.isJsonNull()){
                cfg = gson.fromJson(FileProcess.readFile(cfgFile.toString()), Config.class);
                users = gson.fromJson(FileProcess.readFile(userCfg.toString()), Users.class);
                shopItems = gson.fromJson(FileProcess.readFile(shopItemCfg.toString()), ShopItems.class);
            } else {
                System.err.println("[配置] 在加载配置文件时发生了问题, JSON 文件为空.");
            }
        } catch (Exception e) {
            System.err.println("[配置] 在加载配置文件时发生了问题, 错误信息: " + e);
        }
    }

    public static void loadLang(){
        if (!langCfg.exists()){
            msg.setBotPrefix("Bot > ");
            msg.setNoPermission("你没有权限!");
            msg.setBindSuccess("绑定账号 %s 成功!");
            msg.setNoCheckInData("你还没有签到过, 使用 /qd <游戏ID> 注册签到系统吧~");
            FileProcess.createFile(langCfg.toString(), gson.toJson(msg));
        } else {
            try {
                JsonElement lang = new JsonParser().parse(FileProcess.readFile(langCfg.toString()));
                if (!lang.isJsonNull()) {
                    msg = gson.fromJson(FileProcess.readFile(langCfg.toString()), BotLocalization.class);
                } else
                    System.err.println("[配置] 在读取时发生了问题, JSON 文件为空");
            } catch (IOException e) {
                System.err.println("[配置] 在读取时发生了问题, 错误信息: " + e);
            }
        }
    }

    public static void saveLang(){
        FileProcess.createFile(langCfg.toString(), gson.toJson(msg));
    }
}
