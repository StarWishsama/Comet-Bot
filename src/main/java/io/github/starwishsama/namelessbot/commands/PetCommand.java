package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.EverywhereCommand;
import cc.moecraft.icq.event.events.message.EventMessage;
import cc.moecraft.icq.user.User;
import cc.moecraft.utils.ArrayUtils;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import io.github.starwishsama.namelessbot.objects.user.BotUser;
import io.github.starwishsama.namelessbot.objects.user.Pet;
import io.github.starwishsama.namelessbot.utils.BotUtils;

import java.util.ArrayList;

public class PetCommand implements EverywhereCommand {
    @Override
    public String run(EventMessage event, User sender, String command, ArrayList<String> args) {
        if (BotUtils.isNoCoolDown(sender.getId())){
            BotUser user = BotUser.getUser(sender.getId());
            if (user != null){
                if (args.size() > 0) {
                    switch (args.get(0)) {
                        case "info":
                        case "cx":
                            if (user.getPet() != null) {
                                Pet pet = user.getPet();
                                return pet.getName() + "\n经验值: " + pet.getExp() + "\n等级: " + pet.getLevel();
                            } else {
                                return BotUtils.sendLocalMessage("msg.bot-prefix", "你还没有领取宠物!");
                            }
                        case "get":
                        case "领取":
                        case "lq":
                            if (user.getPet() == null) {
                                if (args.size() > 1) {
                                    String name = ArrayUtils.getTheRestArgsAsString(args, 1);
                                    if (name.length() < 20 && StrUtil.isNotEmpty(name)) {
                                        user.setPet(new Pet(name, sender.getId()));
                                        return BotUtils.sendLocalMessage("msg.bot-prefix", "领取成功! 取名为 " + name);
                                    } else {
                                        return BotUtils.sendLocalMessage("msg.bot-prefix", "宠物的名字太长了!");
                                    }
                                } else {
                                    return BotUtils.sendLocalMessage("msg.bot-prefix", "给你的宠物取个名吧, /cw lq <要取的名字>");
                                }
                            } else {
                                return BotUtils.sendLocalMessage("msg.bot-prefix", "你已经领取过了");
                            }
                        case "打坐":
                        case "dz":
                            if (user.getPet() != null) {
                                Pet op = user.getPet();
                                if (op.getPracticeTime() > 0) {
                                    int exp = RandomUtil.randomInt(5, 30);
                                    op.addExp(exp);
                                    return BotUtils.sendLocalMessage("msg.bot-prefix", "打坐一次获得了 " + exp + " 点经验");
                                } else {
                                    return BotUtils.sendLocalMessage("msg.bot-prefix", "行动点数已经用完了!");
                                }
                            } else {
                                return BotUtils.sendLocalMessage("msg.bot-prefix", "你还没有领取宠物!");
                            }
                        case "打坐全部":
                        case "dzqb":
                            if (user.getPet() != null) {
                                Pet op = user.getPet();
                                if (op.getPracticeTime() > 0) {
                                    int exp = 0;
                                    for (int i = 0; i < op.getPracticeTime(); i++) {
                                        exp = exp + RandomUtil.randomInt(5, 30);
                                    }
                                    op.setPracticeTime(0);
                                    op.addExp(exp);
                                    return BotUtils.sendLocalMessage("msg.bot-prefix", "消耗所有行动点数获得了 " + exp + " 点经验");
                                }
                            } else {
                                return BotUtils.sendLocalMessage("msg.bot-prefix", "你还没有领取宠物!");
                            }
                        case "sj":
                        case "升级":
                            //TODO: 升级系统
                            break;
                    }
                } else {
                    return BotUtils.sendLocalMessage("msg.bot-prefix") + "/pet help!";
                }
            } else {
                return BotUtils.sendLocalMessage("msg.bot-prefix") + "需要先签到才能使用宠物功能!";
            }
        }
        return null;
    }

    @Override
    public CommandProperties properties() {
        return new CommandProperties("pet", "宠物", "cw");
    }
}
