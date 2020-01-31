package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.EverywhereCommand;
import cc.moecraft.icq.event.events.message.EventMessage;
import cc.moecraft.icq.user.User;

import io.github.starwishsama.namelessbot.utils.BotUtils;

import java.text.NumberFormat;
import java.util.*;

public class RandomCommand implements EverywhereCommand {
    Map<String, Double> events = new HashMap<>();

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
            if (events.isEmpty() || events.get(randomEventName) == null){
                if (randomEventName.length() < 30 && BotUtils.isEmojiCharacter(randomEventName)) {
                    double i = new Random().nextDouble();
                    events.put(randomEventName, i);
                    return getRate(randomEventName, i);
                } else
                    return BotUtils.getLocalMessage("msg.bot-prefix") + "需要占卜的东西太长了或者含有非法字符!";
            } else {
                return getRate(randomEventName, events.get(randomEventName));
            }
        }
        return null;
    }

    private enum EventRate {
        HIGHEST("大吉"),
        HIGH("中吉"),
        NORMAL("小吉"),
        LOW("末吉"),
        LOWEST("凶"),
        NEVER("大凶");

        private String event;

        EventRate(String event) {
            this.event = event;
        }
    }

    private String getRate(String eventName, double chance){
        NumberFormat nf = NumberFormat.getPercentInstance();
        nf.setMaximumIntegerDigits(3);
        nf.setMinimumFractionDigits(2);
        String finalRate = nf.format(chance);
        if (chance > 0.8 && chance <= 1.0){
            return "结果是" + EventRate.HIGHEST.event + " (" + finalRate + "), 今天非常适合" + eventName + "哦!";
        } else if (chance > 0.6 && chance <= 0.8){
            return "结果是" + EventRate.HIGH.event + " (" + finalRate + "), 今天很适合" + eventName + "哦!";
        } else if (chance > 0.5 && chance <= 0.6){
            return "结果是" + EventRate.NORMAL.event + " (" + finalRate + "), 今天适合" + eventName + "哦!";
        } else if (chance > 0.3 && chance <= 0.5){
            return "结果是" + EventRate.LOW.event + " (" + finalRate + "), 今天不太适合" + eventName + "...";
        } else if (chance > 0.1 && chance <= 0.3){
            return "结果是" + EventRate.LOWEST.event + " (" + finalRate + "), 今天最好不要" + eventName + "了...";
        } else if (chance <= 0.1){
            return "结果是" + EventRate.NEVER.event + " (" + finalRate + "), 千万别" + eventName + "!";
        } else
            return "你要占卜的东西有点怪呢, 我无法占卜出结果哦.";
    }
}
