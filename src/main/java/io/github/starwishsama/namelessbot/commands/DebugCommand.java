package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.GroupCommand;
import cc.moecraft.icq.event.events.message.EventGroupMessage;
import cc.moecraft.icq.sender.returndata.ReturnStatus;
import cc.moecraft.icq.sender.returndata.returnpojo.get.RVersionInfo;
import cc.moecraft.icq.user.Group;
import cc.moecraft.icq.user.GroupUser;
import io.github.starwishsama.namelessbot.config.Config;
import io.github.starwishsama.namelessbot.config.Message;
import io.github.starwishsama.namelessbot.utils.BotUtils;

import java.util.ArrayList;
import java.util.Date;

import static com.sobte.cqp.jcq.event.JcqApp.CC;

public class DebugCommand implements GroupCommand {
    @Override
    public CommandProperties properties(){
        return new CommandProperties("debug");
    }

    @Override
    public String groupMessage(EventGroupMessage event, GroupUser sender, Group group, String cmd, ArrayList<String> args){
        switch (args.get(0)) {
            case "recall":
                RVersionInfo versionInfo = event.getHttpApi().getVersionInfo().getData();
                if (versionInfo.getCoolqEdition().equalsIgnoreCase("pro")) {
                    if (sender.isAdmin()) return Message.botPrefix + " 不好意思不好意思权限狗打扰了";
                    if (!event.isAdmin()) return Message.botPrefix + " 机器人不是管理员怎么撤回啊kora";
                    if (event.delete().getStatus() == ReturnStatus.ok) return Message.botPrefix + " 已撤回消息";
                    else return Message.botPrefix + " 撤回失败!";
                }
                break;
            case "getat":
                if (CC.getAt(args.get(1)) != 0){
                    return "Bot > 你正在尝试 @" + event.getGroupUser(CC.getAt(args.get(1))).getInfo().getNickname();
                }
                break;
            case "reload":
                Config.loadCfg();
                break;
            case "cd":
                if (!BotUtils.hasCoolDown(sender.getId())){
                    return "Bot > You don't have cooldown";
                } else
                    return "Bot > Debug > Has cooldown, Cooldown is: " + (new Date().getTime() - BotUtils.coolDown.get(sender.getId()));
            default:
                return "Bot > 命令不存在";
        }
        return null;
    }
}
