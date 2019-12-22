package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.GroupCommand;
import cc.moecraft.icq.event.events.message.EventGroupMessage;
import cc.moecraft.icq.user.Group;
import cc.moecraft.icq.user.GroupUser;

import io.github.starwishsama.namelessbot.config.BotCfg;
import io.github.starwishsama.namelessbot.utils.BotUtils;
import org.apache.commons.lang3.StringUtils;

import static com.sobte.cqp.jcq.event.JcqApp.CC;

import java.util.ArrayList;

public class MuteCommand implements GroupCommand {
    @Override
    public CommandProperties properties(){
        return new CommandProperties("mute", "禁言", "禁");
    }

    @Override
    public String groupMessage(EventGroupMessage event, GroupUser sender, Group group, String msg, ArrayList<String> args){
        String reply = null;
        if (sender.isAdmin()){
            if (!event.isAdmin()) {
                if (args.size() == 1) {
                    try {
                        long banQQ = StringUtils.isNumeric(args.get(0)) ? Integer.parseInt(args.get(0)) : CC.getAt(args.get(0));
                        event.getHttpApi().setGroupBan(group.getId(), banQQ, 600);
                    } catch (Exception ignored) {
                        reply = BotCfg.msg.getBotPrefix() + "请@你需要禁言的人或者输入TA的QQ号!";
                    }
                } else if (args.size() == 2) {
                    try {
                        long banQQ = StringUtils.isNumeric(args.get(0)) ? Integer.parseInt(args.get(0)) : CC.getAt(args.get(0));
                        long banTime = 0; // 此处单位为秒
                        if (args.get(1).equals(""))
                            banTime = 10 * 60;
                        else {
                            String tempTime = args.get(1);
                            if (tempTime.indexOf('d') != -1) {
                                banTime += Integer.parseInt(tempTime.substring(0, tempTime.indexOf('d'))) * 24
                                        * 60 * 60;
                                tempTime = tempTime.substring(tempTime.indexOf('d') + 1);
                            }
                            if (tempTime.indexOf('h') != -1) {
                                banTime += Integer.parseInt(tempTime.substring(0, tempTime.indexOf('h'))) * 60
                                        * 60;
                                tempTime = tempTime.substring(tempTime.indexOf('h') + 1);
                            }
                            if (tempTime.indexOf('m') != -1)
                                banTime += Integer.parseInt(tempTime.substring(0, tempTime.indexOf('m'))) * 60;
                        }
                        if (banTime < 1)
                            throw new NumberFormatException("Equal or less than 0");
                        if (banTime <= 30 * 24 * 60 * 60) {
                            event.getHttpApi().setGroupBan(group.getId(), banQQ, banTime);
                            reply = BotCfg.msg.getBotPrefix() + "已禁言 " + event.getGroupUser(banQQ).getInfo().getNickname() + "(" + banQQ + ") " + banTime / 60 + "分钟.";
                        } else
                            reply = BotCfg.msg.getBotPrefix() + "时间长度太大了！";
                    } catch (Exception e) {
                        reply = BotCfg.msg.getBotPrefix() + "命令格式有误! 用法: /mute [@/QQ号] [dhm]";
                    }
                }
            } else
                reply = BotCfg.msg.getBotPrefix() + "机器人不是管理员!";
        } else {
            if (!BotUtils.isCoolDown(sender.getId())) {
                reply = BotCfg.msg.getBotPrefix() + "你没有权限!";
            }
        }
        return reply;
    }
}
