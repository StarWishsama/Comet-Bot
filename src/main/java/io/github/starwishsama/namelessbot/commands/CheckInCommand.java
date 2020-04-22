package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.EverywhereCommand;
import cc.moecraft.icq.event.events.message.EventGroupMessage;
import cc.moecraft.icq.event.events.message.EventMessage;
import cc.moecraft.icq.user.User;
import io.github.starwishsama.namelessbot.objects.user.BotUser;
import io.github.starwishsama.namelessbot.utils.BotUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import static io.github.starwishsama.namelessbot.BotConstants.users;

/**
 * @author Nameless
 */

public class CheckInCommand implements EverywhereCommand {
    @Override
    public CommandProperties properties(){
        return new CommandProperties("checkin", "qd", "签到", "签");
    }

    @Override
    public String run(EventMessage em, User sender, String msg, ArrayList<String> args){
        long id = sender.getId();
        if (BotUtils.isNoCoolDown(id)) {
            if (!BotUser.isUserExist(id)) {
                users.add(new BotUser(id));
                return checkIn(sender, em);
            } else {
                if (!BotUtils.isChecked(BotUser.getUser(id))
                        || BotUser.getUser(id).getCheckInTime() == 0) {
                    return checkIn(sender, em);
                } else {
                    return "Bot > 你今天已经签到过了! 输入 /cx 可查询签到信息";
                }
            }
        }
        return null;
    }

    private String checkIn(User sender, EventMessage msg){
        BotUser user = BotUser.getUser(sender.getId());
        if (user != null) {

            double[] point = calculatePoint(user);

            String text = "Bot > Hi %s, 签到成功!\n" + "本次签到获得 " + point[0] + " 点积分. \n" + "今天是第 "
                    + user.getCheckInTime() + " 天连签了, 额外获得 " + point[1] + " 奖励分~\n截至今天您的账户余额共 "
                    + String.format("%.1f", user.getCheckInPoint()) + " 分.";

            if (msg instanceof EventGroupMessage) {
                user.setCheckInGroup(((EventGroupMessage) msg).getGroupId());
            }

            if (point[0] + point[1] == 0.0) {
                return "Bot > 签到成功!\n" + "今天运气不佳, 没有积分";
            } else {
                if (user.getBindServerAccount() != null) {
                    return String.format(text, user.getBindServerAccount());
                } else {
                    return String.format(text, sender.getInfo().getNickname());
                }
            }
        } else {
            return "Bot > 在签到时发生了异常, 请联系管理员";
        }
    }

    private double[] calculatePoint(BotUser user){
        Calendar c = Calendar.getInstance();
        // 计算连续签到次数，此处用了 Date 这个废弃的类，应换为 Calendar，too lazy to do so.
        if (user.getLastCheckInTime().get(Calendar.MONTH) == c.get(Calendar.MONTH)
                && user.getLastCheckInTime().get(Calendar.DATE) == c.get(Calendar.DATE) - 1) {
            user.setCheckInTime(user.getCheckInTime() + 1);
        } else if (user.getLastCheckInTime().get(Calendar.MONTH) < c.get(Calendar.MONTH)){
            user.setCheckInTime(user.getCheckInTime() + 1);
        }
        else {
            user.setCheckInTime(1);
        }

        user.setLastCheckInTime(c);

        // 只取小数点后一位，将最大 awardPoint 限制到 3 倍
        // refer to issue #40
        double awardProp = 0.15 * (user.getCheckInTime() - 1);
        int basePoint = new Random().nextInt(10);
        double awardPoint = awardProp < 3 ? Double.parseDouble(String.format("%.1f", awardProp * basePoint)) : 3 * basePoint;

        user.addPoint(basePoint + awardPoint);


        return new double[]{basePoint, awardPoint};
    }
}
