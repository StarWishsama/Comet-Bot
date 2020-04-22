package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.EverywhereCommand;
import cc.moecraft.icq.event.events.message.EventMessage;
import cc.moecraft.icq.user.User;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.extra.emoji.EmojiUtil;
import io.github.starwishsama.namelessbot.BotConstants;
import io.github.starwishsama.namelessbot.enums.UserLevel;
import io.github.starwishsama.namelessbot.objects.user.BotUser;
import io.github.starwishsama.namelessbot.objects.user.RandomResult;
import io.github.starwishsama.namelessbot.utils.BotUtils;

import java.util.ArrayList;
import java.util.List;

public class RandomCommand implements EverywhereCommand {
    @Override
    public CommandProperties properties() {
        return new CommandProperties("random", "占卜", "zb");
    }

    @Override
    public String run(EventMessage event, User sender, String command, ArrayList<String> args) {
        if (BotUtils.isNoCoolDown(sender.getId()) && !args.isEmpty()) {
            RandomResult underCover = getResultFromList(BotConstants.underCovers, sender.getId());
            if (underCover == null) {
                BotUser user = BotUser.getUser(sender);

                if (user == null){
                    user = BotUser.quickRegister(sender.getId());
                }

                if (user.getRandomTime() > 0 || user.getLevel() != UserLevel.USER) {
                    StringBuilder sb = new StringBuilder();
                    for (String arg : args) {
                        sb.append(arg).append(" ");
                    }
                    String randomEventName = sb.toString().trim();
                    if (randomEventName.length() < 30 && !EmojiUtil.containsEmoji(randomEventName)) {
                        RandomResult result = new RandomResult(-1000, RandomUtil.randomDouble(0, 1), randomEventName);
                        BotUser.getUser(sender).decreaseTime();
                        return RandomResult.getChance(result);
                    } else {
                        return BotUtils.getLocalMessage("msg.bot-prefix") + "需要占卜的东西太长了或者含有非法字符!";
                    }
                } else {
                    return BotUtils.sendLocalMessage("msg.bot-prefix") + "今日占卜次数已达上限, 如需增加次数请咨询机器人管理.";
                }
            } else {
                BotConstants.underCovers.remove(underCover);
                return RandomResult.getChance(underCover);
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
