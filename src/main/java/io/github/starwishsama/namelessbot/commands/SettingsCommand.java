package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.PrivateCommand;
import cc.moecraft.icq.event.events.message.EventPrivateMessage;
import cc.moecraft.icq.user.User;

import java.util.ArrayList;

public class SettingsCommand implements PrivateCommand {
    @Override
    public String privateMessage(EventPrivateMessage event, User sender, String command, ArrayList<String> args) {
        return null;
    }

    @Override
    public CommandProperties properties() {
        return new CommandProperties("设置", "setting");
    }
}
