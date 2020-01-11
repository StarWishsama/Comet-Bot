package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.EverywhereCommand;
import cc.moecraft.icq.event.events.message.EventMessage;
import cc.moecraft.icq.sender.message.MessageBuilder;
import cc.moecraft.icq.sender.message.components.ComponentAt;
import cc.moecraft.icq.user.User;

import io.github.starwishsama.namelessbot.objects.BotUser;
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
        if (args.size() > 0 && BotUtils.isNoCoolDown(user.getId())){
            switch (args.get(0).toLowerCase()){
                case "info":
                    BotUser bu = BotUtils.getUser(user);
                    if (bu != null && bu.getR6sAccount() != null && args.size() == 1){
                        String result = R6SUtils.getR6SInfo(bu.getR6sAccount());
                        return new MessageBuilder().add(new ComponentAt(user.getId())).newLine().add(result).toString();
                    } else {
                        if (args.size() == 2 && !args.get(1).isEmpty() && BotUtils.isLegitID(args.get(1))) {
                            String result = R6SUtils.getR6SInfo(args.get(1));
                            return new MessageBuilder().add(new ComponentAt(user.getId())).newLine().add(result).toString();
                        }
                        if (args.size() == 3 && BotUtils.isLegitID(args.get(1))) {
                            if (!args.get(0).isEmpty() || BotUtils.isLegitID(args.get(1)) || !args.get(2).isEmpty()) {
                                String result = R6SUtils.getR6SInfo(args.get(1), args.get(2));
                                return new MessageBuilder().add(new ComponentAt(user.getId())).newLine().add(result).toString();
                            }
                        } else
                            return BotUtils.getLocalMessage("msg.bot-prefix") +
                                    "/r6s info [ID] 或 /r6s info [PC/PS4/XBOX] [ID]\n"
                                    + "立即绑定彩虹六号账号 /r6s info 快捷查询游戏数据";
                    }
                case "stats":
                    break;
                case "bind":
                    if (args.size() == 2 && args.get(1) != null){
                        if (BotUtils.isLegitID(args.get(1))){
                            if (BotUtils.isUserExist(user.getId())){
                                BotUser botUser = BotUtils.getUser(user.getId());
                                if (botUser != null && botUser.getR6sAccount() == null)
                                    botUser.setR6sAccount(args.get(1));
                                else
                                    return BotUtils.getLocalMessage("msg.bot-prefix") + "你已经绑定过账号了!";
                            } else
                                return BotUtils.getLocalMessage("msg.bot-prefix") + "使用 /qd 签到自动注册机器人系统";
                        } else
                            return BotUtils.getLocalMessage("msg.bot-prefix") + "ID 格式有误!";
                    }
                default:
                    return BotUtils.getLocalMessage("msg.bot-prefix") + "/r6s info [Uplay账号名]";
            }
        }
        return null;
    }
}
