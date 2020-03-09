package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.EverywhereCommand;
import cc.moecraft.icq.event.events.message.EventMessage;
import cc.moecraft.icq.user.User;
import io.github.starwishsama.namelessbot.utils.BotUtils;

import java.util.ArrayList;

public class RSSCommand implements EverywhereCommand {
    @Override
    public String run(EventMessage event, User sender, String command, ArrayList<String> args) {
        if (BotUtils.isNoCoolDown(sender)){
            if (args.size() > 0){
                if (BotUtils.isBotAdmin(sender) || BotUtils.isBotOwner(sender)) {
                    switch (args.get(0).toLowerCase()) {
                        default:
                            return BotUtils.getLocalMessage("msg.bot-prefix") + "/订阅 [类别]";
                    }
                } else {
                    return BotUtils.getLocalMessage("msg.bot-prefix") + BotUtils.getLocalMessage("msg.no-permission");
                }
            }
        }
        return null;
    }

    @Override
    public CommandProperties properties() {
        return new CommandProperties("rss", "订阅");
    }
}
