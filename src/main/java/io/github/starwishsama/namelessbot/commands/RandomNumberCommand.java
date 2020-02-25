package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.EverywhereCommand;
import cc.moecraft.icq.event.events.message.EventMessage;
import cc.moecraft.icq.user.User;
import cc.moecraft.utils.StringUtils;
import cn.hutool.core.util.RandomUtil;
import io.github.starwishsama.namelessbot.utils.BotUtils;

import java.util.ArrayList;

public class RandomNumberCommand implements EverywhereCommand {
    int maxValue = 100;
    @Override
    public String run(EventMessage event, User sender, String command, ArrayList<String> args) {
        if (args.isEmpty()){
            return BotUtils.getLocalMessage("msg.bot-prefix") + "生成的随机数是: " + RandomUtil.randomInt(maxValue);
        } else if (args.size() == 1){
            if (StringUtils.isNumeric(args.get(0))) {
                return BotUtils.getLocalMessage("msg.bot-prefix") + "生成的随机数是: " + RandomUtil.randomInt(Integer.parseInt(args.get(0)));
            } else
                return BotUtils.getLocalMessage("msg.bot-prefix") + "请输入有效的数字!";
        } else if (args.size() == 2){
            if (StringUtils.isNumeric(args.get(0)) && StringUtils.isNumeric(args.get(1))) {
                return BotUtils.getLocalMessage("msg.bot-prefix") + "生成的随机数是: " + RandomUtil.randomInt(Integer.parseInt(args.get(0)), Integer.parseInt(args.get(1)));
            } else if (args.get(0).equalsIgnoreCase("setmax") && StringUtils.isNumeric(args.get(1))){
                maxValue = Integer.parseInt(args.get(1));
                return BotUtils.getLocalMessage("msg.bot-prefix") + "默认最大值已设为: " + args.get(1);
            } else
                return BotUtils.getLocalMessage("msg.bot-prefix") + "请输入有效的数字!";
        } else
            return BotUtils.getLocalMessage("msg.bot-prefix") + "/rn [最小值] [最大值]";
    }

    @Override
    public CommandProperties properties() {
        return new CommandProperties("rn", "randomnumber", "randomn", "随机数");
    }
}
