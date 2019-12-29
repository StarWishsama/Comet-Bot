package io.github.starwishsama.namelessbot;

import cc.moecraft.icq.PicqBotX;
import cc.moecraft.icq.PicqConfig;
import cc.moecraft.icq.command.interfaces.IcqCommand;
import cc.moecraft.icq.event.IcqListener;
import cc.moecraft.icq.sender.IcqHttpApi;
import cc.moecraft.logger.HyLogger;
import cc.moecraft.logger.environments.ColorSupportLevel;

import io.github.starwishsama.namelessbot.commands.*;
import io.github.starwishsama.namelessbot.config.*;
import io.github.starwishsama.namelessbot.listeners.*;

import lombok.Getter;
import net.kronos.rkon.core.Rcon;
import net.kronos.rkon.core.ex.AuthenticationException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BotMain {
    @Getter
    private static HyLogger logger;
    @Getter
    private static IcqHttpApi api;
    @Getter
    private static String jarPath;
    @Getter
    private static Rcon rcon;
    @Getter
    private static IcqCommand[] commands = new IcqCommand[]{
            new BindCommand(),
            new CheckInCommand(),
            new DebugCommand(),
            new HelpCommand(),
            new InfoCommand(),
            new MusicCommand(),
            new MuteCommand(),
            new RandomCommand(),
            new RConGroupCommand(),
            new RefreshCacheCommand(),
            new ServerInfoCommand(),
            new VersionCommand()
    };

    private static IcqListener[] listeners = new IcqListener[]{
            new ExceptionListener(),
            new SpamListener()
    };

    public static void main(String[] args){
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        try {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                FileSetup.saveCfg();
                FileSetup.saveLang();
            }));

            startBot();

            // 自动保存 Timer
            Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(() -> {
                FileSetup.saveCfg();
                FileSetup.saveLang();
                BotMain.getLogger().log("[Bot] 自动保存数据完成");
            }, 0, BotConstants.cfg.getAutoSaveTime(), TimeUnit.MINUTES);

            /**
             * @TODO: RSS Push
            Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(() -> {
            }, 0, 15, TimeUnit.MINUTES);
             */

            if (BotConstants.cfg.getRconPwd() != null && BotConstants.cfg.getRconPort() != 0) {
                try {
                    rcon = new Rcon(BotConstants.cfg.getRconUrl(), BotConstants.cfg.getRconPort(), BotConstants.cfg.getRconPwd());
                    logger.log("[RCON] 已连接至服务器");
                } catch (IOException e) {
                    logger.warning("[RCON] 连接至服务器时发生了错误, 错误信息: " + e);
                } catch (AuthenticationException ae) {
                    logger.warning("[RCON] RCON 密码有误, 请检查是否输入了正确的密码!");
                }
            }

            String[] line = in.readLine().split(" ");
            switch (line[0]){
                case "setowner":
                    if (line.length > 1) {
                        BotConstants.cfg.setOwnerID(Long.parseLong(line[1]));
                        logger.log("已设置 Bot 的所有者账号为 " + line[1]);
                    }
                    break;
                case "stop":
                    logger.log("正在关闭...");
                    Runtime.getRuntime().exit(0);
                    break;
                default:
                    logger.log("未知指令.");
                    break;
            }
        } catch (IOException e){
            logger.log("[定时任务] 在执行定时任务时发生了问题, 错误信息: " + e);
        }
    }

    // https://blog.csdn.net/df0128/article/details/90484684
    private static String getPath() {
        String path = BotMain.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        if (System.getProperty("os.name").toLowerCase().contains("dows")) {
            path = path.substring(1);
        }
        if (path.contains("jar")) {
            path = path.substring(0, path.lastIndexOf("/"));
            return path;
        }

        File location = new File(path.replace("target/classes/", ""));
        return location.getPath();
    }

    private static void startBot(){
        try {
            jarPath = getPath();
            FileSetup.loadCfg();
            FileSetup.loadLang();
        } catch (Exception e) {
            e.printStackTrace();
        }

        PicqConfig cfg = new PicqConfig(BotConstants.cfg.getBotPort()).setColorSupportLevel(ColorSupportLevel.OS_DEPENDENT);
        PicqBotX bot = new PicqBotX(cfg);
        logger = bot.getLogger();
        bot.setUniversalHyExpSupport(true);
        bot.addAccount(BotConstants.cfg.getBotName(), BotConstants.cfg.getPostUrl(), BotConstants.cfg.getPostPort());
        if (bot.getAccountManager().getAccounts().size() != 0)
            api = bot.getAccountManager().getAccounts().get(0).getHttpApi();
        bot.enableCommandManager(BotConstants.cfg.getCmdPrefix());
        bot.getCommandManager().registerCommands(commands);
        bot.getEventManager().registerListeners(listeners);
        bot.startBot();
        logger.log("启动完成! 机器人运行在端口 " + BotConstants.cfg.getBotPort() + " 上.");
    }
}
