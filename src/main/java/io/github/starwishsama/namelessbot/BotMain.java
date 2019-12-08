package io.github.starwishsama.namelessbot;

import cc.moecraft.icq.PicqBotX;
import cc.moecraft.icq.PicqConfig;
import cc.moecraft.icq.command.interfaces.IcqCommand;
import cc.moecraft.icq.sender.IcqHttpApi;
import cc.moecraft.logger.HyLogger;
import cc.moecraft.logger.environments.ColorSupportLevel;

import io.github.starwishsama.namelessbot.commands.*;
import io.github.starwishsama.namelessbot.config.*;
import net.kronos.rkon.core.Rcon;
import net.kronos.rkon.core.ex.AuthenticationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class BotMain {
    private static HyLogger logger;
    private static IcqHttpApi api;
    public static Rcon rcon;
    public static String jarPath;

    private static IcqCommand[] commands = new IcqCommand[]{
            new VersionCommand(),
            new DebugCommand(),
            new RefreshCacheCommand(),
            new ServerInfoCommand(),
            new CheckInCommand(),
            new InfoCommand(),
            new RConGroupCommand(),
            new MuteCommand(),
            new MusicCommand(),
            new RandomCommand(),
            new R6SCommand(),
            new BindCommand()
    };

    //private static IcqListener[] listeners = new IcqListener[]{
    //};

    public static void main(String[] args){
        try {
            jarPath = getPath();
            System.out.println("[Path] Bot 路径在 " + jarPath);
            System.out.println("[Path] 配置文件路径在 "+ jarPath + "config.json");
        } catch (Exception e) {
            e.printStackTrace();
        }


        PicqConfig cfg = new PicqConfig(BotCfg.cfg.getBotPort()).setUseAsyncCommands(true).setColorSupportLevel(ColorSupportLevel.OS_DEPENDENT);
        PicqBotX bot = new PicqBotX(cfg);
        cfg.setDebug(true);
        logger = bot.getLogger();
        bot.setUniversalHyExpSupport(true);
        bot.addAccount(BotCfg.cfg.getBotName(), BotCfg.cfg.getPostUrl(), BotCfg.cfg.getPostPort());
        bot.enableCommandManager(BotCfg.cfg.getCmdPrefix());
        bot.getCommandManager().registerCommands(commands);
        // bot.getEventManager().registerListeners(listeners);
        bot.startBot();
        if (bot.getAccountManager().getAccounts().size() != 0)
            api = bot.getAccountManager().getAccounts().get(0).getHttpApi();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            BotCfg.saveCfg();
            BotCfg.saveLang();
        }));

        logger.log("启动完成! 机器人运行在端口 " + BotCfg.cfg.getBotPort() + " 上.");

        if (BotCfg.cfg.getRconPwd() != null) {
            try {
                rcon = new Rcon(BotCfg.cfg.getRconUrl(), BotCfg.cfg.getRconPort(), BotCfg.cfg.getRconPwd());
                logger.log("[RCON] 已连接至服务器");
            } catch (IOException e) {
                logger.warning("[RCON] 连接至服务器时发生了错误, 错误信息: " + e.getMessage());
            } catch (AuthenticationException ae) {
                logger.warning("[RCON] RCON 密码有误, 请检查是否输入了正确的密码!");
            }
        }

        final Date d = Calendar.getInstance().getTime();

        Timer t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                BotCfg.saveCfg();
                logger.log("[Bot] 自动保存数据完成");
            }
        }, d, 1000 * 60 * BotCfg.cfg.getAutoSaveTime());

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        try {
            String[] line = in.readLine().split(" ");
            switch (line[0]){
                case "setowner":
                    if (line.length > 1) {
                        BotCfg.cfg.setOwnerID(Long.parseLong(line[1]));
                        logger.log("已设置 Bot 的所有者账号为 " + line[1]);
                    }
                    break;
                case "stop":
                    logger.log("正在关闭...");
                    System.exit(0);
                    break;
            }
        } catch (IOException ignored){
        }
    }

    // https://blog.csdn.net/df0128/article/details/90484684
    private static String getPath() {
        String path = BotMain.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        if (System.getProperty("os.name").toLowerCase().contains("dows")) {
            path = path.substring(1);
        }
        if (path.contains("jar")) {
            path = path.substring(0, path.lastIndexOf("."));
            return path;
        }
        return path.replace("target/classes/", "");
    }

    public static HyLogger getLogger(){
        return logger;
    }

    public static IcqHttpApi getApi() {
        return api;
    }
}
