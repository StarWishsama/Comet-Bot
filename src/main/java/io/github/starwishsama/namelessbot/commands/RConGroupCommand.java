package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.GroupCommand;
import cc.moecraft.icq.event.events.message.EventGroupMessage;
import cc.moecraft.icq.user.Group;
import cc.moecraft.icq.user.GroupUser;
import io.github.starwishsama.namelessbot.BotMain;
import io.github.starwishsama.namelessbot.config.Config;
import io.github.starwishsama.namelessbot.config.Message;

import java.util.ArrayList;

public class RConGroupCommand implements GroupCommand {
    @Override
    public CommandProperties properties(){
        return new CommandProperties("rcon");
    }

    @Override
    public String groupMessage(EventGroupMessage event, GroupUser sender, Group group, String msg, ArrayList<String> args){
        long fromQQ = sender.getId();
        if (Config.botAdmins.contains(fromQQ) || Config.ownerID == fromQQ) {
            try {
                StringBuilder sb = new StringBuilder();
                for (String arg : args) {
                    sb.append(arg).append(" ");
                }
                String result = BotMain.rcon.command(sb.toString().trim());
                return Message.botPrefix + result;
            } catch (Exception e) {
                return Message.botPrefix + "在连接至服务器时发生了错误, 错误信息: " + e.getMessage();
            }
        } else
            return Message.botPrefix + Message.noPermission;
    }
}
