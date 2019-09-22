package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.EverywhereCommand;
import cc.moecraft.icq.event.events.message.EventMessage;
import cc.moecraft.icq.sender.message.MessageBuilder;
import cc.moecraft.icq.sender.message.components.ComponentAt;
import cc.moecraft.icq.user.User;
import io.github.starwishsama.namelessbot.config.Config;
import io.github.starwishsama.namelessbot.config.Message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class InfoCommand implements EverywhereCommand {
    @Override
    public CommandProperties properties(){
        return new CommandProperties("info", "查询", "查", "cx");
    }

    @Override
    public String run(EventMessage e, User user, String msg, ArrayList<String> args){
        if (Config.checkinUsers.containsKey(user.getId())) {
            return new MessageBuilder()
                    .add(new ComponentAt(user.getId())).newLine()
                    .add("积分: ").add(String.format("%.1f", Config.checkinUsers.get(user.getId()).getCheckInPoint())).newLine()
                    .add("累计连续签到了 ").add(Config.checkinUsers.get(user.getId()).getCheckInTime()).add(" 天").newLine()
                    .add("上次签到于: ").add(new SimpleDateFormat("yyyy-MM-dd").format(Config.checkinUsers.get(user.getId()).getLastCheckInTime())).newLine()
                    .add("绑定的游戏账号是: " + Config.checkinUsers.get(user.getId()).getBindServerAccount()).toString();
        } else
            return Message.botPrefix + "你还没有签到过哦";
    }
}
