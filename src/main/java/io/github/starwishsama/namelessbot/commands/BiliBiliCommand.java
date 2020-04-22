package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.EverywhereCommand;
import cc.moecraft.icq.event.events.message.EventMessage;
import cc.moecraft.icq.user.User;
import io.github.starwishsama.namelessbot.objects.user.BotUser;
import io.github.starwishsama.namelessbot.utils.BiliUtils;
import io.github.starwishsama.namelessbot.utils.BotUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedList;

public class BiliBiliCommand implements EverywhereCommand {
    @Override
    public String run(EventMessage event, User sender, String command, ArrayList<String> args) {
        if (BotUtils.isNoCoolDown(sender)) {
            if (BotUser.isUserExist(sender.getId())) {
                BotUser user = BotUser.getUser(sender);
                if (user.getRandomTime() > 0 || BotUser.isBotAdmin(sender)) {
                    user.decreaseTime();
                    if (args.isEmpty()) {
                        if (user.getBiliSubs() != null && !user.getBiliSubs().isEmpty()){
                            StringBuilder sb = new StringBuilder("监控室列表:\n");
                            for (long mid: user.getBiliSubs()){
                                sb.append(BiliUtils.getNameById(mid)).append(": ").append(BiliUtils.isLiveNow(BiliUtils.getLiveInfo(mid)).getStatus()).append("\n");
                            }

                            return sb.toString().trim();
                        } else {
                            return BotUtils.sendLocalMessage("msg.bot-prefix", "你还没有任何关注的人, 使用 /bili add [mid] 订阅");
                        }
                    } else {
                        switch (args.get(0)) {
                            case "add":
                                if (args.size() > 1 && StringUtils.isNumeric(args.get(1))) {
                                    if (user.getBiliSubs() == null){
                                        user.setBiliSubs(new LinkedList<>());
                                    }

                                    user.getBiliSubs().add(Long.parseLong(args.get(1)));
                                    return BotUtils.sendLocalMessage("msg.bot-prefix", "成功将账号 " + BiliUtils.getNameById(Long.parseLong(args.get(1))) + " 加入订阅列表");
                                } else {
                                    return BotUtils.sendLocalMessage("msg.bot-prefix", "请输入有效的 mid!");
                                }
                            case "remove":
                                if (args.size() > 1 && StringUtils.isNumeric(args.get(1))) {
                                    if (user.getBiliSubs().isEmpty() || !user.getBiliSubs().contains(Long.parseLong(args.get(1)))) {
                                        return BotUtils.sendLocalMessage("msg.bot-prefix", "你还没有任何单推的人");
                                    } else {
                                        user.getBiliSubs().remove(Long.parseLong(args.get(1)));
                                        return BotUtils.sendLocalMessage("msg.bot-prefix", "成功将 " + BiliUtils.getNameById(Long.parseLong(args.get(1))) + " 移出订阅列表");
                                    }
                                } else {
                                    return BotUtils.sendLocalMessage("msg.bot-prefix", "请输入有效的 mid!");
                                }
                            default:
                                if (StringUtils.isNumeric(args.get(0))) {
                                    return BiliUtils.getStatusText(Long.parseLong(args.get(0)));
                                } else
                                    return BotUtils.sendLocalMessage("msg.bot-prefix", "请输入有效的 mid!");
                        }
                    }
                } else {
                    return BotUtils.sendLocalMessage("msg.bot-prefix", "今日命令使用条数已达上限");
                }
            } else {
                return BotUtils.sendLocalMessage("msg.bot-prefix", "请先签到注册机器人账号!");
            }
        }
        return null;
    }

    @Override
    public CommandProperties properties() {
        return new CommandProperties("bili", "b站");
    }
}
