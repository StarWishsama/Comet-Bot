package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.EverywhereCommand;
import cc.moecraft.icq.event.events.message.EventMessage;
import cc.moecraft.icq.user.User;
import io.github.starwishsama.namelessbot.BotConstants;
import io.github.starwishsama.namelessbot.objects.user.BotUser;
import io.github.starwishsama.namelessbot.utils.BotUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InfoCommand implements EverywhereCommand {
    @Override
    public CommandProperties properties(){
        return new CommandProperties("info", "查询", "查", "cx");
    }

    @Override
    public String run(EventMessage event, User sender, String command, ArrayList<String> args) {
        try {
            if (args.size() == 0) {
                if (BotUser.isUserExist(sender.getId())) {
                    BotUser user = BotUser.getUser(sender.getId());
                    String reply = "[CQ:at,qq=" + sender.getId() + "]\n积分: " + String.format("%.1f", Objects.requireNonNull(user).getCheckInPoint())
                            + "\n累计连续签到了 " + user.getCheckInTime() + " 天"
                            + "\n上次签到于: " + new SimpleDateFormat("yyyy-MM-dd").format(user.getLastCheckInTime().getTime())
                            + "\n你的权限组: " + user.getLevel()
                            + "\n占卜次数: " + user.getRandomTime();
                    if (user.getBindServerAccount() != null) {
                        reply = reply + "绑定的游戏账号是: " + user.getBindServerAccount();
                    }
                    return reply;
                } else {
                    return BotUtils.getLocalMessage("msg.bot-prefix") + "你还没有签到过哦, 使用 /qd 签到一下吧~";
                }
            } else if (args.size() == 1 && args.get(0).equalsIgnoreCase("排行") || args.get(0).equalsIgnoreCase("ph")) {
                List<BotUser> users = (List<BotUser>) BotConstants.users;
                users.sort((u1, u2) -> (int) (u2.getCheckInPoint() - u1.getCheckInPoint()));
                StringBuilder sb = new StringBuilder();
                sb.append("积分排行榜").append("\n");

                for (int i = 0; i < 10; i++){
                    sb.append(i + 1).append(" ")
                            .append(event.getHttpApi().getStrangerInfo(users.get(i).getUserQQ()).getData().getNickname())
                            .append(" ").append(String.format("%.1f", users.get(i).getCheckInPoint())).append("\n");
                }

                return sb.toString().trim();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
