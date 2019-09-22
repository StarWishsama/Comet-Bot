package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.EverywhereCommand;
import cc.moecraft.icq.event.events.message.EventMessage;
import cc.moecraft.icq.user.User;
import io.github.starwishsama.namelessbot.config.Config;
import io.github.starwishsama.namelessbot.entities.BotUser;
import io.github.starwishsama.namelessbot.utils.BotUtils;

import java.util.*;

public class CheckInCommand implements EverywhereCommand {
    @Override
    public CommandProperties properties(){
        return new CommandProperties("checkin", "qd", "签到", "qiandao");
    }

    @Deprecated
    @Override
    public String run(EventMessage em, User sender, String msg, ArrayList<String> args){
        Map<Long, BotUser> checkinUsers = Config.checkinUsers;
        Long fromQQ = sender.getId();
        if (!checkinUsers.containsKey(fromQQ) && args.get(0).isEmpty()) {
            return "Bot > 你还没有注册无名 Bot 账号! 第一次请使用 /qd <游戏ID> 注册～";
        } else {
            if (!checkinUsers.containsKey(fromQQ)) {
                BotUser newUser = new BotUser();
                newUser.setUserQQ(fromQQ);
                newUser.setBindServerAccount(args.get(0));
                checkinUsers.put(fromQQ, newUser);
                return "Bot > 已绑定账号 " + args.get(0) + " ，以后可以直接输入 /qd 签到了! ";
            }
            if (BotUtils.isCheckInReset(new Date(), checkinUsers.get(fromQQ).getLastCheckInTime()) || checkinUsers.get(fromQQ).getCheckInTime() == 0) {
                BotUser user = checkinUsers.get(fromQQ);
                // 计算连续签到次数，此处用了 Date 这个废弃的类，应换为 Calendar，too lazy to do so.
                if (user.getLastCheckInTime().getMonth() == new Date().getMonth()
                        && user.getLastCheckInTime().getDate() == new Date().getDate() - 1)
                    user.setCheckInTime(user.getCheckInTime() + 1);
                else
                    user.setCheckInTime(1);
                user.setLastCheckInTime(new Date());

                // 只取小数点后一位，将最大 awardPoint 限制到 3 倍
                double awardProp = 0.15 * (user.getCheckInTime() - 1); // refer to issue #40
                int basePoint = new Random(Calendar.getInstance().getTimeInMillis()).nextInt(10);
                double awardPoint = awardProp < 3 ? Double.parseDouble(String.format("%.1f", awardProp * basePoint)) : 3 * basePoint;

                user.setCheckInPoint(checkinUsers.get(fromQQ).getCheckInPoint() + basePoint + awardPoint);

                if (basePoint + awardPoint == 0.0) {
                    return "Bot > 签到成功!\n" + "今天运气不佳, 没有积分";
                } else
                   return  "Bot > Hi " + checkinUsers.get(fromQQ).getBindServerAccount()
                            + ", 签到成功!\n" + "本次签到获得 " + basePoint + " 点积分. \n" + "今天是第 "
                            + user.getCheckInTime() + " 天连签了, 额外获得 " + awardPoint + " 奖励分~\n截至今天您的账户余额共 "
                            + String.format("%.1f", checkinUsers.get(fromQQ).getCheckInPoint()) + "分.";
            } else
               return "Bot > 你今天已经签到过了! 输入 /cx 可查询签到信息";
        }
    }
}
