package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.GroupCommand;
import cc.moecraft.icq.event.events.message.EventGroupMessage;
import cc.moecraft.icq.sender.returndata.ReturnStatus;
import cc.moecraft.icq.sender.returndata.returnpojo.get.RGroupMemberInfo;
import cc.moecraft.icq.user.Group;
import cc.moecraft.icq.user.GroupUser;
import cn.hutool.core.util.RandomUtil;
import io.github.starwishsama.namelessbot.objects.user.BotUser;
import io.github.starwishsama.namelessbot.utils.BotUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class MuteCommand implements GroupCommand {
    @Override
    public CommandProperties properties(){
        return new CommandProperties("mute", "禁言", "禁");
    }

    @Override
    public String groupMessage(EventGroupMessage event, GroupUser sender, Group group, String msg, ArrayList<String> args){
        if (sender.isAdmin() || BotUser.isBotAdmin(sender.getId())) {
            if (event.isAdmin()) {
                if (args.size() > 0) {
                    switch (args.get(0)) {
                        case "all":
                            if (event.getHttpApi().setGroupWholeBan(group.getId(), true).getStatus().equals(ReturnStatus.failed)) {
                                return BotUtils.getLocalMessage("msg.bot-prefix") + "禁言失败";
                            }
                            break;
                        case "random":
                            List<RGroupMemberInfo> data = event.getHttpApi().getGroupMemberList(group.getId()).getData();
                            Long id = data.get(RandomUtil.randomInt(data.size())).getUserId();

                            while (event.isAdmin(id)) {
                                id = data.get(RandomUtil.randomInt(data.size())).getUserId();
                            }

                            int time = RandomUtil.randomInt(60, 3600);
                            if (event.getHttpApi().setGroupBan(group.getId(), id, time).getStatus().equals(ReturnStatus.failed)) {
                                return BotUtils.getLocalMessage("msg.bot-prefix") + "禁言失败";
                            } else {
                                return BotUtils.sendLocalMessage("msg.bot-prefix", "恭喜 [CQ:at,qq=" + id + "] 喜提 " + time / 60L + " 分钟禁言!");
                            }
                        default:
                            /**
                             * @author Stiven.Ding
                             * 禁言逻辑
                             */
                            try {
                                long banQQ = StringUtils.isNumeric(args.get(0)) ? Integer.parseInt(args.get(0)) : BotUtils.parseAt(args.get(0));
                                if (banQQ != -1000) {
                                    long banTime = 0; // 此处单位为秒
                                    if (args.size() == 1)
                                        banTime = 10 * 60;
                                    else {
                                        String tempTime = args.get(1);
                                        if (tempTime.indexOf('d') != -1) {
                                            banTime += Integer.parseInt(tempTime.substring(0, tempTime.indexOf('d'))) * 24
                                                    * 60 * 60;
                                            tempTime = tempTime.substring(tempTime.indexOf('d') + 1);
                                        } else if (tempTime.contains("天")) {
                                            banTime += Integer.parseInt(tempTime.substring(0, tempTime.indexOf('天'))) * 24
                                                    * 60 * 60;
                                            tempTime = tempTime.substring(tempTime.indexOf('天') + 1);
                                        }
                                        if (tempTime.indexOf('h') != -1) {
                                            banTime += Integer.parseInt(tempTime.substring(0, tempTime.indexOf('h'))) * 60
                                                    * 60;
                                            tempTime = tempTime.substring(tempTime.indexOf('h') + 1);
                                        } else if (tempTime.contains("小时")) {
                                            banTime += Integer.parseInt(tempTime.substring(0, tempTime.indexOf("小时"))) * 60
                                                    * 60;
                                            tempTime = tempTime.substring(tempTime.indexOf("小时") + 1);
                                        }
                                        if (tempTime.indexOf('m') != -1) {
                                            banTime += Integer.parseInt(tempTime.substring(0, tempTime.indexOf('m'))) * 60;
                                        } else if (tempTime.contains("分钟")) {
                                            banTime += Integer.parseInt(tempTime.substring(0, tempTime.indexOf("分钟"))) * 60;
                                        }
                                    }
                                    if (banTime < 0)
                                        throw new NumberFormatException("Equal or less than 0");
                                    if (banTime <= 30 * 24 * 60 * 60 && banTime >= 60) {
                                        if (event.getHttpApi().setGroupBan(group.getId(), banQQ, banTime).getStatus().equals(ReturnStatus.failed))
                                            return BotUtils.getLocalMessage("msg.bot-prefix") + "禁言失败";
                                    } else if (banTime == 0){
                                        if (event.getHttpApi().setGroupBan(group.getId(), banQQ, banTime).getStatus().equals(ReturnStatus.failed))
                                            return BotUtils.getLocalMessage("msg.bot-prefix") + "禁言失败";
                                    } else
                                        return BotUtils.getLocalMessage("msg.bot-prefix") + "时间长度有误, 范围 [1分, 30天]";
                                } else
                                    return BotUtils.getLocalMessage("msg.bot-prefix") + "该群成员不存在!";
                            } catch (NumberFormatException e) {
                                return BotUtils.getLocalMessage("msg.bot-prefix") + "命令格式有误! 用法: /mute [@/QQ号] [dhm]";
                            } catch (StringIndexOutOfBoundsException e) {
                                return BotUtils.getLocalMessage("msg.bot-prefix") + "在解析时发生了意料之外的问题";
                            }
                            break;
                    }
                } else
                    return BotUtils.getLocalMessage("msg.bot-prefix") + "用法: /禁 [QQ/@] <时间>";
            } else
                return BotUtils.getLocalMessage("msg.bot-prefix") + "机器人不是管理员!";
        } else {
            if (BotUtils.isNoCoolDown(sender.getId())) {
                return BotUtils.getLocalMessage("msg.bot-prefix") + "你没有权限!";
            }
        }
        return null;
    }
}
