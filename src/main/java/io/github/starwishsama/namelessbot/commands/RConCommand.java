package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.EverywhereCommand;
import cc.moecraft.icq.event.events.message.EventMessage;
import cc.moecraft.icq.user.User;
import io.github.starwishsama.namelessbot.BotMain;
import io.github.starwishsama.namelessbot.utils.BotUtils;

import java.util.ArrayList;

public class RConCommand implements EverywhereCommand {
    @Override
    public CommandProperties properties() {
        return new CommandProperties("rcon", "rc");
    }

    @Override
    public String run(EventMessage event, User sender, String command, ArrayList<String> args) {
        long id = sender.getId();
        if (BotUtils.isNoCoolDown(id)) {
            if (BotUtils.isBotOwner(sender.getId()) || BotUtils.isBotAdmin(sender.getId())) {
                if (BotMain.getRcon() != null) {
                    try {
                        StringBuilder sb = new StringBuilder();
                        for (String arg : args) {
                            sb.append(arg).append(" ");
                        }
                        String result = BotMain.getRcon().command(sb.toString().trim());
                        return BotUtils.getLocalMessage("msg.bot-prefix") + result;
                    } catch (Exception e) {
                        return BotUtils.getLocalMessage("msg.bot-prefix") + "在连接至服务器时发生了错误, 错误信息: " + e.getMessage();
                    }
                } else {
                    return BotUtils.getLocalMessage("msg.bot-prefix") + "RCON 未启用";
                }
            } else {
                return BotUtils.getLocalMessage("msg.bot-prefix") + BotUtils.getLocalMessage("msg.no-permission");
            }
        } else {
            return null;
        }
    }
}
