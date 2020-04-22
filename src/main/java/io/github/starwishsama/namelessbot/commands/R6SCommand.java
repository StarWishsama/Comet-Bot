package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.EverywhereCommand;
import cc.moecraft.icq.event.events.message.EventMessage;
import cc.moecraft.icq.sender.message.MessageBuilder;
import cc.moecraft.icq.sender.message.components.ComponentAt;
import cc.moecraft.icq.user.User;
import cn.hutool.core.util.StrUtil;
import io.github.starwishsama.namelessbot.objects.user.BotUser;
import io.github.starwishsama.namelessbot.utils.BotUtils;
import io.github.starwishsama.namelessbot.utils.R6SUtils;

import java.util.ArrayList;

public class R6SCommand implements EverywhereCommand {
    @Override
    public CommandProperties properties(){
        return new CommandProperties("r6s", "r6", "彩六");
    }

    @Override
    public String run(EventMessage e, User user, String msg, ArrayList<String> args){
        if (args.size() > 0 && BotUtils.isNoCoolDown(user.getId(), 30)){
            switch (args.get(0).toLowerCase()){
                case "info":
                case "查询":
                    BotUser bu = BotUser.getUser(user);
                    if (bu != null && bu.getR6sAccount() != null && args.size() == 1) {
                        e.respond(BotUtils.sendLocalMessage("msg.bot-prefix", "查询中..."));
                        String result = R6SUtils.getR6SInfo(bu.getR6sAccount());
                        return new MessageBuilder().add(new ComponentAt(user.getId())).newLine().add(result).toString();
                    } else {
                        if (args.size() == 2 && !args.get(1).isEmpty() && BotUtils.isLegitId(args.get(1))) {
                            e.respond(BotUtils.sendLocalMessage("msg.bot-prefix", "查询中..."));
                            String result = R6SUtils.getR6SInfo(args.get(1));
                            return new MessageBuilder().add(new ComponentAt(user.getId())).newLine().add(result).toString();
                        } else {
                            return BotUtils.getLocalMessage("msg.bot-prefix") +
                                    "/r6 查询 [ID] 或者 /r6 绑定 [id]\n"
                                    + "绑定彩虹六号账号 无需输入ID快捷查询游戏数据";
                        }
                    }
                case "绑定":
                case "bind":
                    if (StrUtil.isNotEmpty(args.get(1)) && args.size() == 2){
                        if (BotUtils.isLegitId(args.get(1))){
                            if (BotUser.isUserExist(user.getId())){
                                BotUser botUser1 = BotUser.getUser(user.getId());
                                if (botUser1 != null) {
                                    botUser1.setR6sAccount(args.get(1));
                                    return BotUtils.getLocalMessage("msg.bot-prefix") + "绑定成功!";
                                }
                            } else
                                return BotUtils.getLocalMessage("msg.bot-prefix") + "使用 /qd 签到自动注册机器人系统";
                        } else
                            return BotUtils.getLocalMessage("msg.bot-prefix") + "ID 格式有误!";
                    }
                    break;
                default:
                    return BotUtils.getLocalMessage("msg.bot-prefix") + "/r6s info [Uplay账号名]";
            }
        }
        return null;
    }
}
