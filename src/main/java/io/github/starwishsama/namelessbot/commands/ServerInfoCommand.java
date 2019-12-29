package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.EverywhereCommand;
import cc.moecraft.icq.event.events.message.EventMessage;
import cc.moecraft.icq.user.User;

import io.github.starwishsama.namelessbot.utils.BotUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;


public class ServerInfoCommand implements EverywhereCommand {
    @Override
    public CommandProperties properties(){
        return new CommandProperties("serverinfo", "sinfo");
    }

    @Override
    public String run(EventMessage em, User sender, String msg, ArrayList<String> args){
        if (!BotUtils.isCoolDown(sender.getId())) {
            int size = args.size();
            if (size == 1) {
                return BotUtils.getServerInfo(args.get(0));
            } else if (size == 2) {
                if (StringUtils.isNumeric(args.get(1))) {
                    return BotUtils.getServerInfo(args.get(0), Integer.parseInt(args.get(1)));
                } else
                    return BotUtils.getLocalMessage("msg.bot-prefix") + " 请填写正确的端口!";
            }
        }
        return null;
    }
}
