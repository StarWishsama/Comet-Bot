package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.EverywhereCommand;
import cc.moecraft.icq.event.events.message.EventMessage;
import cc.moecraft.icq.user.User;
import cc.moecraft.utils.StringUtils;
import io.github.starwishsama.namelessbot.objects.draws.PCRCharacter;
import io.github.starwishsama.namelessbot.objects.user.BotUser;
import io.github.starwishsama.namelessbot.utils.BotUtils;
import io.github.starwishsama.namelessbot.utils.PCRUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class PCRCommand implements EverywhereCommand {
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
                    return BotUtils.sendLocalMessage("msg.bot-prefix", "/pcr [十连/单抽]");
                } else {
                    switch (args.get(0)) {
                        case "10":
                        case "十连":
                            if (user.getRandomTime() >= 9) {
                                user.decreaseTime(9);
                                return BotUtils.sendLocalMessage("msg.bot-prefix", getDrawResult(true));
                            } else {
                                return BotUtils.sendLocalMessage("msg.bot-prefix") + "今日抽卡次数已达上限, 如需增加次数请咨询机器人管理.";
                            }
                        case "单抽":
                        case "1":
                            return BotUtils.sendLocalMessage("msg.bot-prefix", getDrawResult(false));
                        default:
                            if (StringUtils.isNumeric(args.get(0))) {
                                int count = Integer.parseInt(args.get(0));
                                return BotUtils.sendLocalMessage("msg.bot-prefix", getCustomDrawResult(user, count));
                            } else {
                                return BotUtils.sendLocalMessage("msg.bot-prefix", "/pcr [十连/单抽/次数]");
                            }
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
        return new CommandProperties("pcr", "gzlj", "公主连结");
    }

    private String getCustomDrawResult(BotUser user, int time) {
        long startTime = System.currentTimeMillis();
        List<PCRCharacter> ops = new LinkedList<>();

        for (int i = 0; i < time; i++) {
            ops.add(PCRUtils.draw());
        }

        List<PCRCharacter> r3s = ops.stream().filter(c -> c.getStar() == 3).collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();
        for (PCRCharacter c : r3s) {
            if (user.getRandomTime() > 0) {
                user.decreaseTime();
                sb.append(c.getName()).append(" ");
            } else {
                break;
            }
        }

        return "抽卡次数: " + ops.size()
                + "\n三星角色: " + (sb.toString().trim().isEmpty() ? "未抽到" : sb.toString().trim())
                + "\n二星角色数: " + ops.stream().filter(c -> c.getStar() == 2).count()
                + "\n一星角色数: " + ops.stream().filter(c -> c.getStar() == 1).count()
                + "\n耗时: " + (System.currentTimeMillis() - startTime) + "ms";
    }

    private String getDrawResult(boolean isMultiply) {
        if (isMultiply) {
            List<PCRCharacter> ops = PCRUtils.tenTimesDraw();
            StringBuilder sb = new StringBuilder("十连结果:\n");
            for (PCRCharacter op : ops) {
                sb.append(op.getName()).append(" ").append(getStar(op.getStar())).append("\n");
            }
            return sb.toString().trim();
        } else {
            PCRCharacter op = PCRUtils.draw();
            return op.getName() + " " + getStar(op.getStar());
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
