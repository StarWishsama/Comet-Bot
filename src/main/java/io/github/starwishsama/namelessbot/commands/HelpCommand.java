package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.EverywhereCommand;
import cc.moecraft.icq.command.interfaces.IcqCommand;
import cc.moecraft.icq.event.events.message.EventMessage;
import cc.moecraft.icq.sender.message.MessageBuilder;
import cc.moecraft.icq.user.User;
import io.github.starwishsama.namelessbot.BotMain;

import java.util.ArrayList;

public class HelpCommand implements EverywhereCommand {
    @Override
    public String run(EventMessage event, User sender, String command, ArrayList<String> args) {
        MessageBuilder builder = new MessageBuilder().add("= 无名 Bot 命令列表 =").newLine();
        for (IcqCommand cmd : BotMain.getCommands()) {
            builder.add("/").add(cmd.properties().getName()).newLine();
        }
        return builder.toString().trim();
    }

    @Override
    public CommandProperties properties() {
        return new CommandProperties("help", "?");
    }
}
