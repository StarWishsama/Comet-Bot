package io.github.starwishsama.namelessbot.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import io.github.starwishsama.namelessbot.BotMain;
import io.github.starwishsama.namelessbot.objects.BotLocalization;
import io.github.starwishsama.namelessbot.objects.BotUser;
import io.github.starwishsama.namelessbot.objects.Config;
import io.github.starwishsama.namelessbot.objects.ShopItem;
import io.github.starwishsama.namelessbot.utils.FileProcess;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static io.github.starwishsama.namelessbot.BotConstants.shopItems;
import static io.github.starwishsama.namelessbot.BotConstants.users;
import static io.github.starwishsama.namelessbot.BotConstants.cfg;
import static io.github.starwishsama.namelessbot.BotConstants.msg;

public class FileSetup {
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
                    cfg.setRconPwd(null);
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
                users = gson.fromJson(FileProcess.readFile(userCfg.toString()), new TypeToken<Collection<BotUser>>(){}.getType());
                shopItems = gson.fromJson(FileProcess.readFile(shopItemCfg.toString()), new TypeToken<Collection<ShopItem>>(){}.getType());
            } else {
                System.err.println("[配置] 在加载配置文件时发生了问题, JSON 文件为空.");
            }
        } catch (Exception e) {
            System.err.println("[配置] 在加载配置文件时发生了问题, 错误信息: " + e);
        }
    }

    public static void loadLang(){
        if (!langCfg.exists()){
            msg.add(new BotLocalization("msg.bot-prefix", "Bot > "));
            msg.add(new BotLocalization("msg.no-permission", "你没有权限"));
            msg.add(new BotLocalization("msg.bind-success", "绑定账号 %s 成功!"));
            msg.add(new BotLocalization("checkin.first-time", "你还没有签到过, 先用 /qd 签到一次吧~"));
            FileProcess.createFile(langCfg.toString(), gson.toJson(msg));
        } else {
            try {
                JsonElement lang = new JsonParser().parse(FileProcess.readFile(langCfg.toString()));
                if (!lang.isJsonNull()) {
                    msg = gson.fromJson(FileProcess.readFile(langCfg.toString()), new TypeToken<List<BotLocalization>>(){}.getType());
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
