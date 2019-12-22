package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.GroupCommand;
import cc.moecraft.icq.event.events.message.EventGroupMessage;
import cc.moecraft.icq.user.Group;
import cc.moecraft.icq.user.GroupUser;

import io.github.starwishsama.namelessbot.BotMain;
import io.github.starwishsama.namelessbot.config.BotCfg;
import io.github.starwishsama.namelessbot.utils.BotUtils;

import java.util.ArrayList;

public class RConGroupCommand implements GroupCommand {
    @Override
    public CommandProperties properties(){
        return new CommandProperties("rcon");
    }

    @Override
    public String groupMessage(EventGroupMessage event, GroupUser sender, Group group, String msg, ArrayList<String> args){
        long fromQQ = sender.getId();
        if (!BotUtils.isCoolDown(fromQQ)) {
            if (BotCfg.cfg.getBotAdmins().contains(fromQQ) || BotCfg.cfg.getOwnerID() == fromQQ) {
                try {
                    StringBuilder sb = new StringBuilder();
                    for (String arg : args) {
                        sb.append(arg).append(" ");
                    }
                    String result = BotMain.getRcon().command(sb.toString().trim());
                    return BotCfg.msg.getBotPrefix() + result;
                } catch (Exception e) {
                    return BotCfg.msg.getBotPrefix() + "在连接至服务器时发生了错误, 错误信息: " + e.getMessage();
                }
            } else {
                return BotCfg.msg.getBotPrefix() + BotCfg.msg.getNoPermission();
            }
        } else
            return null;
    }
}
