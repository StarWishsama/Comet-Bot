package io.github.starwishsama.namelessbot.config;

import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.io.file.FileWriter;
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
import io.github.starwishsama.namelessbot.objects.groupconfig.GroupConfig;
import io.github.starwishsama.namelessbot.objects.groupconfig.GroupConfigManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static io.github.starwishsama.namelessbot.BotConstants.shopItems;
import static io.github.starwishsama.namelessbot.BotConstants.users;
import static io.github.starwishsama.namelessbot.BotConstants.cfg;
import static io.github.starwishsama.namelessbot.BotConstants.msg;

public class FileSetup {
    private static File userCfg = new File(BotMain.getJarPath() + "/users.json");
    private static File shopItemCfg = new File(BotMain.getJarPath() + "/items.json");
    private static File cfgFile = new File(BotMain.getJarPath() + "/config.json");
    private static File langCfg = new File(BotMain.getJarPath() + "/lang.json");
    private static File rssTemp = new File(BotMain.getJarPath() + "/temp.txt");
    private static File groupCfg = new File(BotMain.getJarPath() + "/groups.json");

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

                    FileWriter.create(cfgFile).write(gson.toJson(cfg));
                    FileWriter.create(userCfg).write(gson.toJson(users));
                    FileWriter.create(shopItemCfg).write(gson.toJson(shopItems));
                    FileWriter.create(groupCfg).write(gson.toJson(GroupConfigManager.getConfigMap()));

                    if (!rssTemp.createNewFile()){
                        System.out.println("[配置] 缓存文件已存在, 已自动忽略.");
                    }

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
            FileWriter.create(cfgFile).write(gson.toJson(cfg));
            FileWriter.create(userCfg).write(gson.toJson(users));
            FileWriter.create(shopItemCfg).write(gson.toJson(shopItems));
            FileWriter.create(rssTemp).write(BotMain.temp);
            FileWriter.create(groupCfg).write(gson.toJson(GroupConfigManager.getConfigMap()));
        } catch (Exception e){
            System.err.println("[配置] 在保存配置文件时发生了问题, 错误信息: ");
            e.printStackTrace();
        }
    }

    private static void load(){
        try {
            String userContent = FileReader.create(userCfg).readString();
            String configContent = FileReader.create(cfgFile).readString();
            String groupContent = FileReader.create(groupCfg).readString();
            BotMain.temp = FileReader.create(rssTemp).readString();

            JsonElement checkInParser = JsonParser.parseString(userContent);
            JsonElement configParser = JsonParser.parseString(configContent);
            if (!checkInParser.isJsonNull() && !configParser.isJsonNull()){
                cfg = gson.fromJson(configContent, Config.class);
                users = gson.fromJson(userContent, new TypeToken<Collection<BotUser>>(){}.getType());
                shopItems = gson.fromJson(FileReader.create(shopItemCfg).readString(), new TypeToken<Collection<ShopItem>>(){}.getType());
                GroupConfigManager.setConfigMap(gson.fromJson(groupContent, new TypeToken<Map<Long, GroupConfig>>(){}.getType()));
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
            FileWriter.create(langCfg).write(gson.toJson(msg));
        } else {
            JsonElement lang = JsonParser.parseString(FileReader.create(langCfg).readString());
            if (!lang.isJsonNull()) {
                msg = gson.fromJson(FileReader.create(langCfg).readString(), new TypeToken<List<BotLocalization>>(){}.getType());
            } else
                System.err.println("[配置] 在读取时发生了问题, JSON 文件为空");
        }
    }

    public static void saveLang(){
        FileWriter.create(langCfg).write(gson.toJson(msg));
    }

    public static void saveFiles(){
        BotMain.getLogger().log("[Bot] 自动保存数据完成");
        saveCfg();
        saveLang();
    }
}
