package io.github.starwishsama.namelessbot;

import cc.moecraft.icq.PicqBotX;
import cc.moecraft.icq.PicqConfig;
import cc.moecraft.icq.command.interfaces.IcqCommand;
import cc.moecraft.icq.event.IcqListener;
import cc.moecraft.icq.sender.IcqHttpApi;
import cc.moecraft.logger.HyLogger;
import cc.moecraft.logger.environments.ColorSupportLevel;


import io.github.starwishsama.namelessbot.commands.*;
import io.github.starwishsama.namelessbot.config.FileSetup;
import io.github.starwishsama.namelessbot.listeners.ExceptionListener;
import io.github.starwishsama.namelessbot.listeners.SpamListener;

import io.github.starwishsama.namelessbot.objects.BiliLiver;
import io.github.starwishsama.namelessbot.utils.LiveUtils;

import lombok.Getter;

import net.kronos.rkon.core.Rcon;
import net.kronos.rkon.core.ex.AuthenticationException;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Nameless
 */
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
            new AdminCommand(),
            new BindCommand(),
            new CheckInCommand(),
            new DebugCommand(),
            new HelpCommand(),
            new InfoCommand(),
            new MusicCommand(),
            new MuteCommand(),
            new R6SCommand(),
            new RandomCommand(),
            new RSSCommand(),
            new RConGroupCommand(),
            new ServerInfoCommand(),
            new VersionCommand()
    };

    private static IcqListener[] listeners = new IcqListener[]{
            new ExceptionListener()
    };

    public static void main(String[] args){
        startBot();

        // 自动保存 Timer
        Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(() -> {
            FileSetup.saveCfg();
            FileSetup.saveLang();
            BotMain.getLogger().log("[Bot] 自动保存数据完成");
        }, 0, BotConstants.cfg.getAutoSaveTime(), TimeUnit.MINUTES);

        Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(() -> {
            try {
                if (BotConstants.livers != null && !BotConstants.livers.isEmpty()){
                    List<BiliLiver> allLiver = LiveUtils.getBiliLivers();
                    for (BiliLiver liver : allLiver){
                        for (String liverName : BotConstants.livers){
                            if (liver.getVtuberName().equals(liverName)){
                                if (liver.isStreaming()) {
                                    getApi().sendPrivateMsg(BotConstants.cfg.getOwnerID(),
                                            "bilibili 直播开播提醒\n"
                                                    + liverName + " 开始直播了!\n"
                                                    + "开播时间: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(liver.getLastLive().getTime()) + "\n"
                                                    + "☞单击直达直播 " + "https://live.bilibili.com/" + liver.getRoomid()
                                    );
                                }
                            }
                        }
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }, 0, 10, TimeUnit.MINUTES);

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
    }

    // From https://blog.csdn.net/df0128/article/details/90484684
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

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            FileSetup.saveCfg();
            FileSetup.saveLang();
        }));

        PicqConfig cfg = new PicqConfig(BotConstants.cfg.getBotPort())
                .setColorSupportLevel(ColorSupportLevel.OS_DEPENDENT)
                .setLogFileName("Nameless-Bot-Log")
                .setUseAsyncCommands(true);
        PicqBotX bot = new PicqBotX(cfg);
        logger = bot.getLogger();
        bot.setUniversalHyExpSupport(true);
        bot.addAccount(BotConstants.cfg.getBotName(), BotConstants.cfg.getPostUrl(), BotConstants.cfg.getPostPort());
        if (bot.getAccountManager().getAccounts().size() != 0) {
            api = bot.getAccountManager().getNonAccountSpecifiedApi();
        }
        bot.enableCommandManager(BotConstants.cfg.getCmdPrefix());
        bot.getCommandManager().registerCommands(commands);
        bot.getEventManager().registerListeners(listeners);
        if (BotConstants.cfg.isAntiSpam()){
            bot.getEventManager().registerListener(new SpamListener());
        }
        bot.startBot();
        logger.log("启动完成! 机器人运行在端口 " + BotConstants.cfg.getBotPort() + " 上.");
    }
}
