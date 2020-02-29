package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.EverywhereCommand;
import cc.moecraft.icq.event.events.message.EventMessage;
import cc.moecraft.icq.user.User;

import io.github.starwishsama.namelessbot.enums.UserLevel;
import io.github.starwishsama.namelessbot.objects.BotUser;
import io.github.starwishsama.namelessbot.utils.BotUtils;

import java.util.ArrayList;

public class ShopCommand implements EverywhereCommand {
    @Override
    public String run(EventMessage event, User sender, String command, ArrayList<String> args) {
        if (BotUtils.isNoCoolDown(sender.getId())) {
            BotUser user = BotUtils.getUser(sender.getId());
            if (user != null) {
                if (user.getLevel() != UserLevel.USER){
                    return BotUtils.sendLocalMessage("msg.bot-prefix", "你不需要加次数了!");
                }

                if (user.getCheckInPoint() > 2) {
                    if (user.updateTime()) {
                        user.setCheckInPoint(user.getCheckInPoint() - 3);
                        return BotUtils.sendLocalMessage("msg.bot-prefix", "已花费 3 点积分增加一次占卜次数, 目前次数: " + user.getRandomTime());
                    } else {
                        return BotUtils.sendLocalMessage("msg.bot-prefix", "已达到占卜次数购买上限");
                    }
                } else {
                    return BotUtils.sendLocalMessage("msg.bot-prefix", "积分不足, 需要 3 积分");
                }
            } else {
                return BotUtils.sendLocalMessage("msg.bot-prefix","用户不存在");
            }
        }
        return null;
    }

    @Override
    public CommandProperties properties() {
        return new CommandProperties("shop");
    }
}
