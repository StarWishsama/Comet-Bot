package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.EverywhereCommand;
import cc.moecraft.icq.event.events.message.EventMessage;
import cc.moecraft.icq.user.User;

import io.github.starwishsama.namelessbot.utils.BotUtils;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Random;

public class RandomCommand implements EverywhereCommand {
    @Override
    public CommandProperties properties() {
        return new CommandProperties("random", "占卜", "zb");
    }

    @Override
    public String run(EventMessage event, User sender, String command, ArrayList<String> args) {
        if (BotUtils.isNoCoolDown(sender.getId()) && !args.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String arg : args) {
                sb.append(arg).append(" ");
            }
            String randomEventName = sb.toString().trim();
            if (randomEventName.length() < 16 && BotUtils.isEmojiCharacter(randomEventName)) {
                double i = new Random().nextDouble();
                EventRate rate = getRate(i);
                NumberFormat nf = NumberFormat.getPercentInstance();
                nf.setMaximumIntegerDigits(3);
                nf.setMinimumFractionDigits(2);
                String finalRate = nf.format(i);
                switch (rate) {
                    case HIGHEST:
                        return "结果是" + EventRate.HIGHEST.event + " (" + finalRate + "), 今天非常适合" + randomEventName + "哦!";
                    case HIGH:
                        return "结果是" + EventRate.HIGH.event + " (" + finalRate + "), 今天很适合" + randomEventName + "哦!";
                    case NORMAL:
                        return "结果是" + EventRate.NORMAL.event + " (" + finalRate + "), 今天适合" + randomEventName + "哦!";
                    case LOW:
                        return "结果是" + EventRate.LOW.event + " (" + finalRate + "), 今天不太适合" + randomEventName + "...";
                    case LOWEST:
                        return "结果是" + EventRate.LOWEST.event + " (" + finalRate + "), 今天最好不要" + randomEventName + "了...";
                    case NEVER:
                        return "结果是" + EventRate.NEVER.event + " (" + finalRate + "), 千万别" + randomEventName + "!";
                }
            } else
                return BotUtils.getLocalMessage("msg.bot-prefix") + "需要占卜的东西太长了或者含有非法字符!";
        }
        return null;
    }

    private enum EventRate {
        HIGHEST("大吉"),
        HIGH("中吉"),
        NORMAL("小吉"),
        LOW("末吉"),
        LOWEST("凶"),
        NEVER("大凶"),
        UNKNOWN("未知");

        private String event;

        EventRate(String event) {
            this.event = event;
        }
    }

    private EventRate getRate(double chance){
        if (chance > 0.8 && chance <= 1.0){
            return EventRate.HIGHEST;
        } else if (chance > 0.6 && chance <= 0.8){
            return EventRate.HIGH;
        } else if (chance > 0.5 && chance <= 0.6){
            return EventRate.NORMAL;
        } else if (chance > 0.3 && chance <= 0.5){
            return EventRate.LOW;
        } else if (chance > 0.1 && chance <= 0.3){
            return EventRate.LOWEST;
        } else if (chance <= 0.1){
            return EventRate.NEVER;
        } else
            return EventRate.UNKNOWN;
    }
}
