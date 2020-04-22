package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.EverywhereCommand;
import cc.moecraft.icq.event.events.message.EventMessage;
import cc.moecraft.icq.user.User;
import io.github.starwishsama.namelessbot.BotConstants;
import io.github.starwishsama.namelessbot.config.FileSetup;
import io.github.starwishsama.namelessbot.objects.dtos.BiliBiliUser;
import io.github.starwishsama.namelessbot.objects.user.BotUser;
import io.github.starwishsama.namelessbot.utils.BiliUtilsOld;
import io.github.starwishsama.namelessbot.utils.BotUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class DebugCommand implements EverywhereCommand {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Override
    public CommandProperties properties(){
        return new CommandProperties("debug");
    }

    @Override
    public String run(EventMessage event, User sender, String command, ArrayList<String> args) {
        if (BotUser.isBotOwner(sender.getId()) || BotUser.isBotAdmin(sender.getId())) {
            switch (args.get(0)) {
                case "reload":
                    FileSetup.loadCfg();
                    FileSetup.loadLang();
                    return BotUtils.getLocalMessage("msg.bot-prefix") + " 已重载配置文件";
                case "unbind":
                    BotUser user = BotUser.getUser(sender.getId());
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
                    if (BotUser.isBotOwner(sender.getId())) {
                        event.getBot().getAccountManager().refreshCache();
                        return BotUtils.getLocalMessage("msg.bot-prefix") + "已手动刷新信息缓存.";
                    }
                    break;
                case "vtuber":
                    if (args.size() > 1) {
                        if (BiliUtilsOld.getUserByName(args.get(1)) != null) {
                            BiliBiliUser biliUser = BiliUtilsOld.getUserByName(args.get(1));
                            if (BiliUtilsOld.getLiveStatus(biliUser)) {
                                return "Bot > " + biliUser.getUserName() + " 正在直播" +
                                        "\n直播间直达链接: https://live.bilibili.com/" + biliUser.getRoomid() +
                                        "\n直播间标题: " + biliUser.getTitle() +
                                        "\n直播开始时间" + dateFormat.format(biliUser.getTime());
                            } else {
                                return "Bot > " + biliUser.getUserName() + " 没有在直播" +
                                        "\n上次直播数据:" +
                                        "\n时间" + dateFormat.format(biliUser.getLastLive().getTime()) +
                                        "\n人气: " + biliUser.getLastLive().getOnline();
                            }
                        } else {
                            return "Bot > 用户不存在";
                        }
                    }
                    break;
                case "resetcount":
                    if (BotUser.isBotOwner(sender.getId())){
                        for (BotUser botUser : BotConstants.users){
                            botUser.setRandomTime(20);
                        }
                        return "Bot > Reset random time successful";
                    }
                    break;
                default:
                    return "Bot > 命令不存在" +
                            "\n请注意: 这里的命令随时会被删除.";
            }
        }
        return null;
    }
}
