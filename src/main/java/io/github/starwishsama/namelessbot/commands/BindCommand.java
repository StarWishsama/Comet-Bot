package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.EverywhereCommand;
import cc.moecraft.icq.event.events.message.EventMessage;
import cc.moecraft.icq.user.User;

import io.github.starwishsama.namelessbot.config.BotCfg;
import io.github.starwishsama.namelessbot.utils.BotUtils;

import java.util.ArrayList;
import java.util.Objects;

public class BindCommand implements EverywhereCommand {
    @Override
    public String run(EventMessage event, User sender, String command, ArrayList<String> args) {
        if (BotUtils.isUserExist(sender.getId())){
            if (args.size() > 0) {
                if (args.get(0) != null && BotUtils.isLegitID(args.get(0))) {
                    Objects.requireNonNull(BotUtils.getUser(sender.getId())).setBindServerAccount(args.get(0));
                    return "Bot > 已绑定账号 " + args.get(0);
                } else
                    return BotCfg.msg.getBotPrefix() + "ID 不符合规范";
            } else
                return BotCfg.msg.getBotPrefix() + "ID 不能为空";
        } else
            return BotCfg.msg.getBotPrefix() + "请先使用 /qd 签到一次!";
    }

    @Override
    public CommandProperties properties() {
        return new CommandProperties("bind", "绑", "绑定");
    }
}
