package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.GroupCommand;
import cc.moecraft.icq.event.events.message.EventGroupMessage;
import cc.moecraft.icq.sender.returndata.ReturnStatus;
import cc.moecraft.icq.sender.returndata.returnpojo.get.RVersionInfo;
import cc.moecraft.icq.user.Group;
import cc.moecraft.icq.user.GroupUser;

import io.github.starwishsama.namelessbot.config.BotCfg;

import java.util.ArrayList;

public class DebugCommand implements GroupCommand {
    @Override
    public CommandProperties properties(){
        return new CommandProperties("debug");
    }

    @Override
    public String groupMessage(EventGroupMessage event, GroupUser sender, Group group, String cmd, ArrayList<String> args){
        String reply = "";
        switch (args.get(0)) {
            case "recall":
                RVersionInfo versionInfo = event.getHttpApi().getVersionInfo().getData();
                if (versionInfo.getCoolqEdition().equalsIgnoreCase("pro")) {
                    if (sender.isAdmin()) reply = BotCfg.msg.getBotPrefix() + " 不好意思不好意思权限狗打扰了";
                    else if (!event.isAdmin()) reply = BotCfg.msg.getBotPrefix() + " 机器人不是管理员怎么撤回啊kora";
                    else if (event.delete().getStatus() == ReturnStatus.ok) reply = BotCfg.msg.getBotPrefix() + " 已撤回消息";
                    else reply = BotCfg.msg.getBotPrefix() + " 撤回失败!";
                }
                break;
            case "reload":
                BotCfg.loadCfg();
                BotCfg.loadLang();
                reply = BotCfg.msg.getBotPrefix() + " Configuration has been reloaded.";
                break;
            default:
                reply = "Bot > 命令不存在";
                break;
        }
        return reply;
    }
}
