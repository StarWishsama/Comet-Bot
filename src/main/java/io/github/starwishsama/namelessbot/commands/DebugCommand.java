package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.GroupCommand;
import cc.moecraft.icq.event.events.message.EventGroupMessage;
import cc.moecraft.icq.user.Group;
import cc.moecraft.icq.user.GroupUser;

import io.github.starwishsama.namelessbot.BotMain;
import io.github.starwishsama.namelessbot.config.FileSetup;
import io.github.starwishsama.namelessbot.objects.BiliLiver;
import io.github.starwishsama.namelessbot.objects.BotUser;
import io.github.starwishsama.namelessbot.utils.BotUtils;
import io.github.starwishsama.namelessbot.utils.LiveUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class DebugCommand implements GroupCommand {
    @Override
    public CommandProperties properties(){
        return new CommandProperties("debug");
    }

    @Override
    public String groupMessage(EventGroupMessage event, GroupUser sender, Group group, String cmd, ArrayList<String> args){
        if (BotUtils.isBotOwner(sender.getId()) || BotUtils.isBotAdmin(sender.getId())) {
            switch (args.get(0)) {
                case "reload":
                    FileSetup.loadCfg();
                    FileSetup.loadLang();
                    return BotUtils.getLocalMessage("msg.bot-prefix") + " 已重载配置文件";
                case "unbind":
                    BotUser user = BotUtils.getUser(sender.getId());
                    if (user != null) {
                        if (user.getBindServerAccount() != null) {
                            user.setBindServerAccount(null);
                            return BotUtils.getLocalMessage("msg.bot-prefix") + "已解绑账号";
                        } else
                            return BotUtils.getLocalMessage("msg.bot-prefix") + "你还没绑定过账号";
                    }
                    break;
                case "rc":
                case "refreshcache":
                    if (BotUtils.isBotAdmin(sender.getId()) || BotUtils.isBotOwner(sender.getId())) {
                        event.getBot().getAccountManager().refreshCache();
                        return BotUtils.getLocalMessage("msg.bot-prefix") + "已手动刷新信息缓存.";
                    }
                case "raw":
                    return args.toString();
                case "vtuber":
                    try {
                        if (args.size() == 2) {
                            BiliLiver liver = LiveUtils.getBiliLiver(args.get(1));
                            if (liver != null) {
                                return "bilibili 主播信息\n"
                                        + "主播名: " + liver.getVtuberName() + "\n"
                                        + "上次开播时间: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(liver.getLastLive().getTime()) + "\n"
                                        + "直播房间地址: " + "https://live.bilibili.com/" + liver.getRoomid() + "\n"
                                        + "直播状态:" + (liver.isStreaming() ? "√" : "X");
                            }
                        }
                    } catch (IOException e){
                        BotMain.getLogger().warning("在获取主播信息时发生了一个错误");
                    }
                case "getat":
                    if (args.size() == 2){
                        return "" + BotUtils.parseAt(args.get(1));
                    }
                default:
                    return "Bot > 命令不存在";
            }
        }
        return null;
    }
}
