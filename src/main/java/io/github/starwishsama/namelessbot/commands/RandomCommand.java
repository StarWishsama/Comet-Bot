package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.EverywhereCommand;
import cc.moecraft.icq.event.events.message.EventMessage;
import cc.moecraft.icq.user.User;

import cn.hutool.core.util.RandomUtil;
import io.github.starwishsama.namelessbot.BotConstants;
import io.github.starwishsama.namelessbot.objects.RandomResult;
import io.github.starwishsama.namelessbot.utils.BotUtils;

import java.util.*;

public class RandomCommand implements EverywhereCommand {
    List<RandomResult> events = new LinkedList<>();

    @Override
    public CommandProperties properties() {
        return new CommandProperties("random", "占卜", "zb");
    }

    @Override
    public String run(EventMessage event, User sender, String command, ArrayList<String> args) {
        if (BotUtils.isNoCoolDown(sender.getId()) && !args.isEmpty()) {
            RandomResult underCover = getResultFromList(BotConstants.underCovers, sender.getId());
            if (underCover == null) {
                StringBuilder sb = new StringBuilder();
                for (String arg : args) {
                    sb.append(arg).append(" ");
                }
                String randomEventName = sb.toString().trim();
                if (!isDuplicate(randomEventName)) {
                    if (randomEventName.length() < 30 && BotUtils.containsEmoji(randomEventName)) {
                        RandomResult result = new RandomResult(-1000, RandomUtil.randomDouble(0, 1), randomEventName);
                        events.add(result);
                        return RandomResult.getChance(result);
                    } else {
                        return BotUtils.getLocalMessage("msg.bot-prefix") + "需要占卜的东西太长了或者含有非法字符!";
                    }
                } else {
                    RandomResult result = getResult(randomEventName);
                    if (result != null) {
                        return RandomResult.getChance(result.getEventName(), result.getChance());
                    } else {
                        if (randomEventName.length() < 30 && BotUtils.containsEmoji(randomEventName)) {
                            RandomResult result1 = new RandomResult(-1000, RandomUtil.randomDouble(0, 1), randomEventName);
                            events.add(result1);
                            return RandomResult.getChance(result1);
                        } else {
                            return BotUtils.getLocalMessage("msg.bot-prefix") + "需要占卜的东西太长了或者含有非法字符!";
                        }
                    }
                }
            } else {
                events.add(underCover);
                BotConstants.underCovers.remove(underCover);
                return RandomResult.getChance(underCover);
            }
        }
        return null;
    }

    private boolean isDuplicate(String eventName){
        return getResult(eventName) != null;
    }

    private RandomResult getResult(String eventName){
        if (!events.isEmpty()){
            for (RandomResult result: events){
                if (result.getEventName().equals(eventName)){
                    return result;
                }
            }
        }
        return null;
    }

    private RandomResult getResultFromList(List<RandomResult> results, long id){
        if (!results.isEmpty()){
            for (RandomResult result: results){
                if (result.getId() == id){
                    return result;
                }
            }
        }
        return null;
    }
}
