package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.PrivateCommand;
import cc.moecraft.icq.event.events.message.EventPrivateMessage;
import cc.moecraft.icq.user.User;
import cc.moecraft.utils.ArrayUtils;
import cc.moecraft.utils.StringUtils;
import io.github.starwishsama.namelessbot.BotConstants;
import io.github.starwishsama.namelessbot.objects.user.BotUser;
import io.github.starwishsama.namelessbot.objects.user.RandomResult;
import io.github.starwishsama.namelessbot.utils.BotUtils;

import java.util.ArrayList;

public class UnderCoverCommand implements PrivateCommand {
    @Override
    public String privateMessage(EventPrivateMessage event, User sender, String command, ArrayList<String> args) {
        if (BotUser.isBotAdmin(sender) || BotUser.isBotOwner(sender)){
            if (args.size() > 0){
                if (StringUtils.isNumeric(args.get(0))){
                    double v = Double.parseDouble(args.get(0));
                    if (v > 0 && v < 1) {
                        BotConstants.underCovers.add(new RandomResult(sender.getId(), Double.parseDouble(args.get(0)), ArrayUtils.getTheRestArgsAsString(args, 1)));
                        return BotUtils.getLocalMessage("msg.bot-prefix") + "已添加黑幕占卜内容";
                    } else
                        return BotUtils.getLocalMessage("msg.bot-prefix") + "期望值范围区间: (0,1)";
                } else
                    return BotUtils.getLocalMessage("msg.bot-prefix") + "/uc [概率小数] [占卜内容]";
            } else
                return BotUtils.getLocalMessage("msg.bot-prefix") + "/uc [概率小数] [占卜内容]";
        }
        return null;
    }

    @Override
    public CommandProperties properties() {
        return new CommandProperties("uc", "undercover", "黑幕");
    }
}
