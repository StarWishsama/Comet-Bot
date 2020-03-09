package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.GroupCommand;
import cc.moecraft.icq.event.events.message.EventGroupMessage;
import cc.moecraft.icq.user.Group;
import cc.moecraft.icq.user.GroupUser;
import cc.moecraft.utils.StringUtils;
import cn.hutool.core.util.RandomUtil;
import io.github.starwishsama.namelessbot.BotConstants;
import io.github.starwishsama.namelessbot.BotMain;
import io.github.starwishsama.namelessbot.managers.SessionManager;
import io.github.starwishsama.namelessbot.session.commands.guessnumber.GuessNumberSession;
import io.github.starwishsama.namelessbot.utils.BotUtils;

import java.util.ArrayList;
import java.util.Random;

/**
 * @author Nameless
 */
public class GuessNumberCommand implements GroupCommand {
    @Override
    public String groupMessage(EventGroupMessage event, GroupUser sender, Group group, String command, ArrayList<String> args) {
        if (BotUtils.isNoCoolDown(sender.getId())) {
            if (SessionManager.getSessionByGroup(group.getId()) != null){
                return BotUtils.getLocalMessage("msg.bot-prefix") + "你还没猜出数字呢!";
            } else {
                if (args.isEmpty()) {
                    event.respond(BotUtils.getLocalMessage("msg.bot-prefix") + "正在生成数字...");
                    GuessNumberSession session = new GuessNumberSession(sender.getId(), event.getGroupId(), new Random().nextInt(BotConstants.cfg.getMaxNumber()));
                    SessionManager.addSession(session);
                    BotMain.getLogger().log("[猜数字] 已生成随机数: " + session.getAnswer() + " 群聊: " + group.getId());
                    return BotUtils.getLocalMessage("msg.bot-prefix") + "请猜一个数字 (范围: [0," + BotConstants.cfg.getMaxNumber() + "])";
                } else {
                    if (args.size() > 1) {
                        if (StringUtils.isNumeric(args.get(0)) && StringUtils.isNumeric(args.get(1))) {
                            double min = Double.parseDouble(args.get(0));
                            double max = Double.parseDouble(args.get(1));
                            if (args.get(0).contains(".") || args.get(1).contains(".")) {
                                return BotUtils.getLocalMessage("msg.bot-prefix") + "不支持小数";
                            } else if (min == max){
                                return BotUtils.sendLocalMessage("msg.bot-prefix" + "两个数字不能相等!");
                            } else {
                                GuessNumberSession session = new GuessNumberSession(sender.getId(), event.getGroupId(), RandomUtil.randomInt(Integer.parseInt(args.get(0)), Integer.parseInt(args.get(1))));
                                SessionManager.addSession(session);
                                BotMain.getLogger().log("[猜数字] 已生成随机数: " + session.getAnswer() + " 群聊: " + group.getId());
                                return BotUtils.getLocalMessage("msg.bot-prefix") + "请猜一个数字 (范围: [" + args.get(0) + "," + args.get(1) + "])";
                            }
                        } else {
                            return BotUtils.getLocalMessage("msg.bot-prefix") + "请输入有效数字!";
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public CommandProperties properties() {
        return new CommandProperties("guess", "猜数字");
    }
}
