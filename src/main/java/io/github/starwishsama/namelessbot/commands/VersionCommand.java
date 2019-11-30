package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.EverywhereCommand;
import cc.moecraft.icq.event.events.message.EventMessage;
import cc.moecraft.icq.sender.message.MessageBuilder;
import cc.moecraft.icq.sender.returndata.returnpojo.get.RStatus;
import cc.moecraft.icq.user.User;
import io.github.starwishsama.namelessbot.utils.BotUtils;

import java.util.ArrayList;
import java.util.Date;

public class VersionCommand implements EverywhereCommand {

    @Override
    public CommandProperties properties(){
        return new CommandProperties("v", "version", "版本");
    }

    @Override
    public String run(EventMessage event, User sender, String cmd, ArrayList<String> args){
        if (!BotUtils.hasCoolDown(sender.getId())) {
            RStatus status = event.getHttpApi().getStatus().getData();
            return new MessageBuilder()
                    .add("无名Bot v1.0.0-HTTPAPI").newLine()
                    .add("运行状态: ").add(status.getGood() ? "√" : "X")
                    .toString();
        } else
            return null;
    }
}
