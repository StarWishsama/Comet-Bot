package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.EverywhereCommand;
import cc.moecraft.icq.event.events.message.EventMessage;
import cc.moecraft.icq.user.User;
import io.github.starwishsama.namelessbot.RSSPusher;
import io.github.starwishsama.namelessbot.utils.BotUtils;

import java.util.ArrayList;
import java.util.Date;

public class RSSCommand implements EverywhereCommand {
    @Override
    public CommandProperties properties(){
        return new CommandProperties("rss");
    }

    @Override
    public String run(EventMessage e, User sender, String cmd, ArrayList<String> args){
        if (BotUtils.hasCoolDown(sender.getId())) {
            if (args.get(0).equalsIgnoreCase("paperclip")) {
                return RSSPusher.getLatestVideo();
            }
        }
        return null;
    }
}
