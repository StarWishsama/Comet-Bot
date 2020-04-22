package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.GroupCommand;
import cc.moecraft.icq.event.events.message.EventGroupMessage;
import cc.moecraft.icq.sender.returndata.returnpojo.get.RGroupMemberInfo;
import cc.moecraft.icq.user.Group;
import cc.moecraft.icq.user.GroupUser;
import io.github.starwishsama.namelessbot.BotConstants;
import io.github.starwishsama.namelessbot.BotMain;
import io.github.starwishsama.namelessbot.objects.user.ClockInData;
import io.github.starwishsama.namelessbot.utils.BotUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class ClockInCommand implements GroupCommand {

    @Override
    public String groupMessage(EventGroupMessage event, GroupUser sender, Group group, String command, ArrayList<String> args) {
        if (BotConstants.data.containsKey(group.getId())){
            ClockInData data = BotConstants.data.get(group.getId());
            if (!isChecked(sender.getId(), data)){
                return clockIn(sender, data);
            } else {
                return BotUtils.sendLocalMessage("msg.bot-prefix", "你已经打卡过了!");
            }
        }
        return null;
    }

    @Override
    public CommandProperties properties() {
        return new CommandProperties("clockin", "打卡", "dk");
    }

    private static boolean isChecked(Long id, ClockInData data){
        if (data.getCheckedUser().isEmpty()){
            return false;
        }

        for (RGroupMemberInfo i: data.getCheckedUser()){
            if (i.getUserId().equals(id)){
                return true;
            }
        }

        for (RGroupMemberInfo i: data.getLateUser()){
            if (i.getUserId().equals(id)){
                return true;
            }
        }
        return false;
    }

    private static String clockIn(GroupUser sender, ClockInData data){
        try {
            LocalDateTime clockTime = LocalDateTime.now();
            RGroupMemberInfo info = null;

            for (RGroupMemberInfo i : data.getList()){
                if (i.getUserId() == sender.getId()){
                    info = i;
                }
            }
            if (info != null) {
                if (clockTime.isAfter(data.getEndTime())) {
                    data.getLateUser().add(info);
                }

                data.getCheckedUser().add(info);

                return "打卡成功!\n打卡时间: " + clockTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        + "\n打卡状态: " + (clockTime.isAfter(data.getEndTime()) ? "迟到" : "成功");
            } else {
                return "发生了意外";
            }

        } catch (Exception e) {
            BotMain.getLogger().error(e);
            return "发生了意外";
        }
    }
}
