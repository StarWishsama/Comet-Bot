package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.GroupCommand;
import cc.moecraft.icq.event.events.message.EventGroupMessage;
import cc.moecraft.icq.user.Group;
import cc.moecraft.icq.user.GroupUser;
import cn.hutool.core.util.RandomUtil;
import io.github.starwishsama.namelessbot.listeners.RepeatListener;
import io.github.starwishsama.namelessbot.utils.BotUtils;

import java.text.NumberFormat;
import java.util.ArrayList;

public class RepeatCommand implements GroupCommand {
    @Override
    public String groupMessage(EventGroupMessage event, GroupUser sender, Group group, String command, ArrayList<String> args) {
        if (BotUtils.isNoCoolDown(sender.getId())){
            NumberFormat nf = NumberFormat.getPercentInstance();
            nf.setMaximumIntegerDigits(3);
            nf.setMinimumFractionDigits(2);
            if (!RepeatListener.getReady2repeat().containsKey(sender.getId())) {
                double chance = RandomUtil.randomDouble(0, 1);
                int time = RandomUtil.randomInt(1, 30);
                RepeatListener.getReady2repeat().put(sender.getId(), new RepeatListener.RepeatData(chance, time));
                return BotUtils.sendLocalMessage("msg.bot-prefix", "[CQ:at,qq=" + sender.getId() + "], 你当前未被复读的次数是 " + time + ". 下一次消息被复读的概率是 " + nf.format(chance) + ".\n当你被复读后次数会被重置.");
            } else {
                RepeatListener.RepeatData data = RepeatListener.getReady2repeat().get(sender.getId());
                return BotUtils.sendLocalMessage("msg.bot-prefix", "[CQ:at,qq=" + sender.getId() + "], 你当前未被复读的次数是 " + data.getTime() + ". 下一次消息被复读的概率是 " + nf.format(data.getChance()) + ".\n当你被复读后次数会被重置.");
            }
        }
        return null;
    }

    @Override
    public CommandProperties properties() {
        return new CommandProperties("repeat", "复读");
    }
}
