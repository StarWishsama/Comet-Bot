package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.EverywhereCommand;
import cc.moecraft.icq.command.interfaces.GroupCommand;
import cc.moecraft.icq.event.events.message.EventGroupMessage;
import cc.moecraft.icq.event.events.message.EventMessage;
import cc.moecraft.icq.user.Group;
import cc.moecraft.icq.user.GroupUser;

import cc.moecraft.icq.user.User;
import io.github.starwishsama.namelessbot.BotConstants;
import io.github.starwishsama.namelessbot.config.FileSetup;
import io.github.starwishsama.namelessbot.objects.BotUser;
import io.github.starwishsama.namelessbot.objects.RssItem;
import io.github.starwishsama.namelessbot.utils.BotUtils;
import io.github.starwishsama.namelessbot.utils.LiveUtils;

import java.util.ArrayList;

public class DebugCommand implements EverywhereCommand {
    @Override
    public CommandProperties properties(){
        return new CommandProperties("debug");
    }

    /** Need rework */

    @Override
    public String run(EventMessage event, User sender, String command, ArrayList<String> args) {
        if (BotUtils.isBotOwner(sender.getId()) || BotUtils.isBotAdmin(sender.getId())) {
            switch (args.get(0)) {
                case "reload":
                    FileSetup.loadCfg();
                    FileSetup.loadLang();
                    return BotUtils.getLocalMessage("msg.bot-prefix") + " 已重载配置文件";
                case "unbind":
                    BotUser user = BotUtils.getUser(sender.getId());
                    if (user != null) {
                        if (user.getBindServerAccount() != null) {
                            user.setBindServerAccount(null);
                            return BotUtils.getLocalMessage("msg.bot-prefix") + "已解绑账号";
                        } else
                            return BotUtils.getLocalMessage("msg.bot-prefix") + "你还没绑定过账号";
                    }
                    break;
                case "rc":
                case "refreshcache":
                    if (BotUtils.isBotOwner(sender.getId())) {
                        event.getBot().getAccountManager().refreshCache();
                        return BotUtils.getLocalMessage("msg.bot-prefix") + "已手动刷新信息缓存.";
                    }
                    break;
                case "vtuber":
                    if (args.size() > 1) {
                        return LiveUtils.getLiver(args.get(1));
                    }
                    break;
                default:
                    return "Bot > 命令不存在" +
                            "\n请注意: 这里的命令随时会被删除.";
            }
        }
        return null;
    }
}
