package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.EverywhereCommand;
import cc.moecraft.icq.event.events.message.EventMessage;
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
                String reply;
                if (botUser != null) {
                    reply = "[CQ:at,qq=" + user.getId() + "]\n积分: " + String.format("%.1f", botUser.getCheckInPoint())
                            + "\n累计连续签到了 " + botUser.getCheckInTime() + " 天"
                            + "上次签到于: " + new SimpleDateFormat("yyyy-MM-dd").format(botUser.getLastCheckInTime());
                    if (botUser.getBindServerAccount() != null) {
                        reply = reply + "绑定的游戏账号是: " + botUser.getBindServerAccount();
                    }
                } else
                    reply = BotCfg.msg.getBotPrefix() + "你还没有签到过哦";
                return reply;
            } else
                return BotCfg.msg.getBotPrefix() + "你还没有签到过哦";
        } else
            return null;
    }
}
