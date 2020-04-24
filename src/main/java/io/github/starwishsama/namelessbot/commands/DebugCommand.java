package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.EverywhereCommand;
import cc.moecraft.icq.event.events.message.EventMessage;
import cc.moecraft.icq.user.User;
import cc.moecraft.utils.StringUtils;
import io.github.starwishsama.namelessbot.BotConstants;
import io.github.starwishsama.namelessbot.config.FileSetup;
import io.github.starwishsama.namelessbot.objects.user.BotUser;
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
            BotUser user = BotUser.getUser(sender.getId());
            switch (args.get(0)) {
                case "reload":
                    FileSetup.loadCfg();
                    FileSetup.loadLang();
                    return BotUtils.getLocalMessage("msg.bot-prefix") + " 已重载配置文件";
                case "unbind":
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
                case "resetcount":
                    if (BotUser.isBotOwner(sender.getId())) {
                        for (BotUser botUser : BotConstants.users) {
                            botUser.setRandomTime(80);
                        }
                        return "Bot > Reset random time successful";
                    }
                    break;
                case "set":
                    if (args.size() > 1) {
                        if (StringUtils.isNumeric(args.get(1))) {
                            user.setRandomTime(Integer.parseInt(args.get(1)));
                            return "Bot > Success";
                        } else if (BotUtils.parseAt(args.get(1)) != -1000L) {
                            BotUser bu = BotUser.getUser(BotUtils.parseAt(args.get(1)));
                            if (bu != null) {
                                if (StringUtils.isNumeric(args.get(2))) {
                                    bu.setRandomTime(Integer.parseInt(args.get(2)));
                                    return "Bot > Success";
                                } else {
                                    return "Bot > /debug set [@] [point]";
                                }
                            } else {
                                return "Bot > Can't find user you mentioned";
                            }
                        } else {
                            return "Bot > /debug set [@] [point]";
                        }
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
