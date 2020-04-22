package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.EverywhereCommand;
import cc.moecraft.icq.event.events.message.EventMessage;
import cc.moecraft.icq.user.User;
import io.github.starwishsama.namelessbot.objects.draws.ArkNightOperator;
import io.github.starwishsama.namelessbot.objects.user.BotUser;
import io.github.starwishsama.namelessbot.utils.ArkNightUtils;
import io.github.starwishsama.namelessbot.utils.BotUtils;

import java.util.ArrayList;
import java.util.List;

public class ArknightCommand implements EverywhereCommand {
    @Override
    public String run(EventMessage event, User sender, String command, ArrayList<String> args) {
        if (BotUtils.isNoCoolDown(sender.getId())){
            BotUser user = BotUser.getUser(sender.getId());
            if (user == null){
                 user = BotUser.quickRegister(sender.getId());
            }

            if (user.getRandomTime() > 0) {
                user.decreaseTime();

                if (args.isEmpty()) {
                    return BotUtils.sendLocalMessage("msg.bot-prefix", "/mrfz [十连/单抽]");
                } else {
                    switch (args.get(0)) {
                        case "10":
                        case "十连":
                            user.decreaseTime(9);
                            return BotUtils.sendLocalMessage("msg.bot-prefix", getDrawResult(true));
                        case "单抽":
                        case "1":
                            return BotUtils.sendLocalMessage("msg.bot-prefix", getDrawResult(false));
                        default:
                            return BotUtils.sendLocalMessage("msg.bot-prefix", "/mrfz [十连/单抽]");
                    }
                }
            } else {
                return BotUtils.sendLocalMessage("msg.bot-prefix") + "今日抽卡次数已达上限, 如需增加次数请咨询机器人管理.";
            }
        }
        return null;
    }

    @Override
    public CommandProperties properties() {
        return new CommandProperties("arknight", "mrfz", "明日方舟", "舟游");
    }

    private String getDrawResult(boolean isMultiply){
        if (isMultiply){
            List<ArkNightOperator> ops = ArkNightUtils.tenTimeDraw();
            StringBuilder sb = new StringBuilder("十连结果:\n");
            for (ArkNightOperator op: ops){
                sb.append(op.getName()).append(" ").append(getStar(op.getRare())).append("\n");
            }
            return sb.toString().trim();
        } else {
            ArkNightOperator op = ArkNightUtils.draw();
            return op.getName() + " " + getStar(op.getRare());
        }
    }

    private String getStar(int rare){
        StringBuilder sb = new StringBuilder("★");
        for (int i = 1; i < rare; i++){
            sb.append("★");
        }
        return sb.toString();
    }
}
