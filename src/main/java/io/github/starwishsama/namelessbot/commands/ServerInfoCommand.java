package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.EverywhereCommand;
import cc.moecraft.icq.command.interfaces.GroupCommand;
import cc.moecraft.icq.event.events.message.EventGroupMessage;
import cc.moecraft.icq.event.events.message.EventMessage;
import cc.moecraft.icq.user.Group;
import cc.moecraft.icq.user.GroupUser;
import cc.moecraft.icq.user.User;

import io.github.starwishsama.namelessbot.objects.groupconfig.GroupConfig;
import io.github.starwishsama.namelessbot.objects.groupconfig.GroupConfigManager;
import io.github.starwishsama.namelessbot.utils.BotUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;


public class ServerInfoCommand implements GroupCommand {
    @Override
    public CommandProperties properties(){
        return new CommandProperties("serverinfo", "sinfo");
    }

    @Override
    public String groupMessage(EventGroupMessage event, GroupUser sender, Group group, String command, ArrayList<String> args) {
        if (BotUtils.isNoCoolDown(sender.getId())) {
            if (GroupConfigManager.getConfig(sender.getId()) != null && GroupConfigManager.getConfig(sender.getId()).getMcServerAddress() != null){
                String[] address = GroupConfigManager.getConfig(sender.getId()).getMcServerAddress().split(":");
                if (address.length == 1){
                    return BotUtils.getServerInfo(address[0]);
                } else if (address.length == 2) {
                    return BotUtils.getServerInfo(address[0], Integer.parseInt(address[1]));
                } else {
                    return BotUtils.sendLocalMessage("msg.bot-prefix", "服务器地址有误, 请联系管理员修改.");
                }
            } else {
                if (args.size() == 1) {
                    return BotUtils.getServerInfo(args.get(0));
                } else if (args.size() == 2) {
                    if (StringUtils.isNumeric(args.get(1))) {
                        return BotUtils.getServerInfo(args.get(0), Integer.parseInt(args.get(1)));
                    } else
                        return BotUtils.getLocalMessage("msg.bot-prefix") + " 请填写正确的端口!";
                }
            }
        }
        return null;
    }
}
