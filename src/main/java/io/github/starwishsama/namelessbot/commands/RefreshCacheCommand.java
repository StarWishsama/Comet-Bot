package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.EverywhereCommand;
import cc.moecraft.icq.event.events.message.EventMessage;
import cc.moecraft.icq.user.User;
import io.github.starwishsama.namelessbot.config.Message;

import java.util.ArrayList;

public class RefreshCacheCommand implements EverywhereCommand {
    @Override
    public CommandProperties properties(){
        return new CommandProperties("refreshcache", "rc");
    }

    @Override
    public String run(EventMessage e, User sender, String cmd, ArrayList<String> args){
        e.getBot().getAccountManager().refreshCache();
        return Message.botPrefix + "已手动刷新信息缓存.";
    }
}
