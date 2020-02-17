package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.EverywhereCommand;
import cc.moecraft.icq.event.events.message.EventMessage;
import cc.moecraft.icq.user.User;

import io.github.starwishsama.namelessbot.BotConstants;
import io.github.starwishsama.namelessbot.objects.BotUser;
import io.github.starwishsama.namelessbot.utils.BotUtils;

import java.util.ArrayList;
import java.util.Objects;

public class BindCommand implements EverywhereCommand {
    @Override
    public String run(EventMessage event, User sender, String command, ArrayList<String> args) {
        if (BotUtils.isNoCoolDown(sender.getId())) {
            if (BotConstants.cfg.isBindMCAccount()) {
                BotUser user = BotUtils.getUser(sender.getId());
                if (user != null) {
                    if (user.getBindServerAccount() == null) {
                        if (args.size() > 0) {
                            if (args.get(0) != null && BotUtils.isLegitId(args.get(0)) && args.get(0).length() < 17) {
                                Objects.requireNonNull(BotUtils.getUser(sender.getId())).setBindServerAccount(args.get(0));
                                return "Bot > 已绑定账号 " + args.get(0);
                            } else
                                return BotUtils.getLocalMessage("msg.bot-prefix") + "ID 不符合规范";
                        } else
                            return BotUtils.getLocalMessage("msg.bot-prefix") + "/bind [Minecraft用户名]";
                    } else
                        return BotUtils.getLocalMessage("msg.bot-prefix") + "你已经绑定过账号了!";
                } else
                    return BotUtils.getLocalMessage("msg.bot-prefix") + "请先使用 /qd 签到一次!";
            } else
                return BotUtils.getLocalMessage("msg.bot-prefix") + "绑定账号暂未开放";
        }
        return null;
    }

    @Override
    public CommandProperties properties() {
        return new CommandProperties("bind", "绑", "绑定");
    }
}
