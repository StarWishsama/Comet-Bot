package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.GroupCommand;
import cc.moecraft.icq.event.events.message.EventGroupMessage;
import cc.moecraft.icq.user.Group;
import cc.moecraft.icq.user.GroupUser;
import io.github.starwishsama.namelessbot.utils.BotUtils;

import java.util.ArrayList;

public class WereWolfKillCommand implements GroupCommand {
    // WIP
    @Override
    public String groupMessage(EventGroupMessage event, GroupUser sender, Group group, String command, ArrayList<String> args) {
        if (BotUtils.isNoCoolDown(sender.getId())) {
            if (args.isEmpty()) {
                return "WIP";
            } else {
                switch (args.size()) {
                    case 1:
                        switch (args.get(0)) {
                            case "start":
                            case "开始":
                                return "";
                            case "setting":
                            case "设置":
                                if (BotUtils.isBotAdmin(sender.getId())) {

                                } else
                                    return BotUtils.sendLocalMessage("msg.bot-prefix", "你没有权限");
                        }
                        break;
                    case 2:
                        break;
                    default:
                        return BotUtils.sendLocalMessage("msg.bot-prefix", "命令格式错误!");
                }
            }
        }
        return null;
    }

    @Override
    public CommandProperties properties() {
        return new CommandProperties("lrs", "狼人杀", "wwk");
    }
}
