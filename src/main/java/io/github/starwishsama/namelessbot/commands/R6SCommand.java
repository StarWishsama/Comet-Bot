package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.EverywhereCommand;
import cc.moecraft.icq.event.events.message.EventMessage;
import cc.moecraft.icq.user.User;

import java.util.ArrayList;

public class R6SCommand implements EverywhereCommand {
    @Override
    public CommandProperties properties(){
        return new CommandProperties("r6s", "r6");
    }

    @Override
    public String run(EventMessage e, User user, String msg, ArrayList<String> args){
        return null;
    }
}
