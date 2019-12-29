package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.EverywhereCommand;
import cc.moecraft.icq.event.events.message.EventMessage;
import cc.moecraft.icq.user.User;

import io.github.starwishsama.namelessbot.utils.BotUtils;

import java.util.ArrayList;
import java.util.Random;

public class RandomCommand implements EverywhereCommand {
    @Override
    public CommandProperties properties() {
        return new CommandProperties("random", "占卜", "zb");
    }

    @Override
    public String run(EventMessage event, User sender, String command, ArrayList<String> args) {
        if (BotUtils.isCoolDown(sender.getId())) {
            if (!args.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (String arg : args) {
                    sb.append(arg).append(" ");
                }
                String randomEventName = sb.toString().trim();
                int i = new Random().nextInt(245);
                EventRate rate = getRate(i);
                switch (rate) {
                    case HIGHEST:
                        return "结果是" + EventRate.HIGHEST.event + " (" + i + "/255), 今天非常适合" + randomEventName + "哦!";
                    case HIGH:
                        return "结果是" + EventRate.HIGH.event + " (" + i + "/255), 今天很适合" + randomEventName + "哦!";
                    case NORMAL:
                        return "结果是" + EventRate.NORMAL.event + " (" + i + "/255), 今天适合" + randomEventName + "哦!";
                    case LOW:
                        return "结果是" + EventRate.LOW.event + " (" + i + "/255), 今天不太适合" + randomEventName + "...";
                    case LOWEST:
                        return "结果是" + EventRate.LOWEST.event + " (" + i + "/255), 今天最好不要" + randomEventName + "了...";
                    case NEVER:
                        return "结果是" + EventRate.NEVER.event + " (" + i + "/255), 千万别" + randomEventName + "!";
                }
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
        NEVER("大凶"),
        UNKNOWN("未知");

        private String event;

        EventRate(String event) {
            this.event = event;
        }
    }

    private EventRate getRate(int chance){
        if (chance > 200 && chance <= 245){
            return EventRate.HIGHEST;
        } else if (chance > 150 && chance <= 200){
            return EventRate.HIGH;
        } else if (chance > 80 && chance <= 150){
            return EventRate.NORMAL;
        } else if (chance > 40 && chance <= 80){
            return EventRate.LOW;
        } else if (chance > 20 && chance <= 40){
            return EventRate.LOWEST;
        } else if (chance <= 20){
            return EventRate.NEVER;
        } else
            return EventRate.UNKNOWN;
    }
}
