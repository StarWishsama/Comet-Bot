package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.GroupCommand;
import cc.moecraft.icq.event.events.message.EventGroupMessage;
import cc.moecraft.icq.user.Group;
import cc.moecraft.icq.user.GroupUser;
import io.github.starwishsama.namelessbot.BotConstants;
import io.github.starwishsama.namelessbot.utils.BotUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

public class AdminCommand implements GroupCommand {
    @Override
    public String groupMessage(EventGroupMessage event, GroupUser sender, Group group, String command, ArrayList<String> args) {
        if (BotUtils.isBotOwner(sender.getId()) || BotConstants.cfg.getBotAdmins().contains(sender.getId())){
            if (args.size() > 0) {
                switch (args.get(0)) {
                    case "set":
                        if (args.size() == 2) {
                            if (BotUtils.isBotOwner(sender.getId()) && args.get(0) != null){
                                long qq = StringUtils.isNumeric(args.get(0)) ? Long.parseLong(args.get(1)) : BotUtils.parseAt(args.get(1));
                                if (qq != -1000L) {
                                    if (BotConstants.cfg.getBotAdmins() == null) {
                                        BotConstants.cfg.setBotAdmins(new ArrayList<>());
                                    }
                                    if (BotUtils.isBotAdmin(sender.getId())) {
                                        BotConstants.cfg.getBotAdmins().remove(qq);
                                        return BotUtils.getLocalMessage("msg.bot-prefix") + "删除机器人管理员成功!";
                                    } else {
                                        BotConstants.cfg.getBotAdmins().add(qq);
                                        return BotUtils.getLocalMessage("msg.bot-prefix") + "添加机器人管理员成功!";
                                    }
                                } else
                                    return BotUtils.getLocalMessage("msg.bot-prefix") + "请检查QQ号是否正确!";

                            }
                        }
                        break;
                    case "help":
                        return "Nothing here now";
                    default:
                        return null;
                }
            } else
                return BotUtils.getLocalMessage("msg.bot-prefix") + "/admin help";
        }
        return null;
    }

    @Override
    public CommandProperties properties() {
        return new CommandProperties("admin", "管理");
    }
}
