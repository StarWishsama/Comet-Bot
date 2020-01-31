package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.GroupCommand;
import cc.moecraft.icq.event.events.message.EventGroupMessage;
import cc.moecraft.icq.user.Group;
import cc.moecraft.icq.user.GroupUser;
import cc.moecraft.utils.StringUtils;
import io.github.starwishsama.namelessbot.BotConstants;
import io.github.starwishsama.namelessbot.session.SessionManager;
import io.github.starwishsama.namelessbot.session.commands.GuessNumberSession;
import io.github.starwishsama.namelessbot.utils.BotUtils;

import java.util.ArrayList;
import java.util.Random;

public class GuessNumberCommand implements GroupCommand {
    @Override
    public String groupMessage(EventGroupMessage event, GroupUser sender, Group group, String command, ArrayList<String> args) {
        if (BotUtils.isNoCoolDown(sender.getId())) {
            if (args.size() == 0) {
                if (!SessionManager.isValidSession(sender.getId())) {
                    SessionManager.addSession(sender.getId(), new GuessNumberSession(sender.getId(), new Random().nextInt(BotConstants.cfg.getMaxNumber())));
                    return BotUtils.getLocalMessage("msg.bot-prefix") + "请猜一个数字 (范围: [0," + BotConstants.cfg.getMaxNumber() + "])";
                } else
                    return BotUtils.getLocalMessage("msg.bot-prefix") + "你还没猜出数字呢!";
            } else {
                if (BotUtils.isBotAdmin(sender.getId())) {
                    if (args.size() == 2) {
                        if (StringUtils.isNumeric(args.get(1))) {
                            BotConstants.cfg.setMaxNumber(Integer.parseInt(args.get(1)));
                            return BotUtils.getLocalMessage("msg.bot-prefix") + "设置最大值为 " + args.get(1);
                        } else
                            return BotUtils.getLocalMessage("msg.bot-prefix") + "请输入一个正确的数字!";
                    } else {
                        return BotUtils.getLocalMessage("msg.bot-prefix") + "/guess set [最大数]";
                    }
                }
            }
        }
        return null;
    }

    @Override
    public CommandProperties properties() {
        return new CommandProperties("猜数字", "guess");
    }
}
