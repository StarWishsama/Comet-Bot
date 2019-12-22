package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.EverywhereCommand;
import cc.moecraft.icq.event.events.message.EventMessage;
import cc.moecraft.icq.sender.message.MessageBuilder;
import cc.moecraft.icq.sender.message.components.ComponentAt;
import cc.moecraft.icq.user.User;

import io.github.starwishsama.namelessbot.config.BotCfg;
import io.github.starwishsama.namelessbot.objects.BotUser;
import io.github.starwishsama.namelessbot.utils.BotUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class InfoCommand implements EverywhereCommand {
    @Override
    public CommandProperties properties(){
        return new CommandProperties("info", "查询", "查", "cx");
    }

    @Override
    public String run(EventMessage e, User user, String msg, ArrayList<String> args){
        if (!BotUtils.hasCoolDown(user.getId())) {
            if (BotUtils.isUserExist(user.getId())) {
                BotUser botUser = BotUtils.getUser(user.getId());
                MessageBuilder mb;
                if (botUser != null) {
                    mb = new MessageBuilder()
                            .add(new ComponentAt(user.getId())).newLine()
                            .add("积分: ").add(String.format("%.1f", botUser.getCheckInPoint())).newLine()
                            .add("累计连续签到了 ").add(botUser.getCheckInTime()).add(" 天").newLine()
                            .add("上次签到于: ").add(new SimpleDateFormat("yyyy-MM-dd").format(botUser.getLastCheckInTime())).newLine();
                    if (botUser.getBindServerAccount() != null) {
                        mb.add("绑定的游戏账号是: " + botUser.getBindServerAccount());
                    }
                } else
                    mb = new MessageBuilder().add(new ComponentAt(user.getId())).newLine().add("你还没有签到过哦");
                return mb.toString();
            } else
                return BotCfg.msg.getBotPrefix() + "你还没有签到过哦";
        } else
            return null;
    }
}
