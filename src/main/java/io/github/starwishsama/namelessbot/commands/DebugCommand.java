package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.GroupCommand;
import cc.moecraft.icq.event.events.message.EventGroupMessage;
import cc.moecraft.icq.sender.returndata.ReturnStatus;
import cc.moecraft.icq.sender.returndata.returnpojo.get.RVersionInfo;
import cc.moecraft.icq.user.Group;
import cc.moecraft.icq.user.GroupUser;

import io.github.starwishsama.namelessbot.config.FileSetup;
import io.github.starwishsama.namelessbot.objects.BotUser;
import io.github.starwishsama.namelessbot.utils.BotUtils;

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
                    if (sender.isAdmin())
                        reply = BotUtils.getLocalMessage("msg.bot-prefix") + " 不好意思不好意思权限狗打扰了";
                    else if (!event.isAdmin())
                        reply = BotUtils.getLocalMessage("msg.bot-prefix") + " 机器人不是管理员怎么撤回啊kora";
                    else if (event.delete().getStatus() == ReturnStatus.ok)
                        reply = "";
                    else
                        reply = BotUtils.getLocalMessage("msg.bot-prefix") + " 撤回失败!";
                }
                break;
            case "reload":
                FileSetup.loadCfg();
                FileSetup.loadLang();
                reply = BotUtils.getLocalMessage("msg.bot-prefix") + " 已重载配置文件";
                break;
            case "unbind":
                if (sender.isAdmin()) {
                    BotUser user = BotUtils.getUser(sender.getId());
                    if (user != null) {
                        if (user.getBindServerAccount() != null) {
                            user.setBindServerAccount(null);
                            reply = BotUtils.getLocalMessage("msg.bot-prefix") + "已解绑账号";
                        } else
                            reply = BotUtils.getLocalMessage("msg.bot-prefix") + "你还没绑定过账号";
                    }
                }
                break;
            default:
                reply = "Bot > 命令不存在";
                break;
        }
        return reply;
    }
}
