package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.GroupCommand;
import cc.moecraft.icq.event.events.message.EventGroupMessage;
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
        switch (args.get(0)) {
            case "reload":
                FileSetup.loadCfg();
                FileSetup.loadLang();
                return BotUtils.getLocalMessage("msg.bot-prefix") + " 已重载配置文件";
            case "unbind":
                if (sender.isAdmin()) {
                    BotUser user = BotUtils.getUser(sender.getId());
                    if (user != null) {
                        if (user.getBindServerAccount() != null) {
                            user.setBindServerAccount(null);
                            return BotUtils.getLocalMessage("msg.bot-prefix") + "已解绑账号";
                        } else
                            return BotUtils.getLocalMessage("msg.bot-prefix") + "你还没绑定过账号";
                    }
                }
                break;
            default:
                return "Bot > 命令不存在";
        }
        return null;
    }
}
