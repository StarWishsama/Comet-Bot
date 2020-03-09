package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.GroupCommand;
import cc.moecraft.icq.event.events.message.EventGroupMessage;
import cc.moecraft.icq.user.Group;
import cc.moecraft.icq.user.GroupUser;
import io.github.starwishsama.namelessbot.managers.GroupConfigManager;
import io.github.starwishsama.namelessbot.objects.groupconfig.GroupConfig;
import io.github.starwishsama.namelessbot.utils.BotUtils;

import java.util.ArrayList;

public class SettingsCommand implements GroupCommand {
    @Override
    public String groupMessage(EventGroupMessage event, GroupUser sender, Group group, String command, ArrayList<String> args) {
        if (BotUtils.isBotAdmin(sender.getId()) || BotUtils.isBotOwner(sender.getId())) {
            if (args.isEmpty()) {
                return "/config help";
            } else {
                switch (args.get(0)) {
                    default:
                        return BotUtils.sendLocalMessage("msg.bot-prefix", "可用的命令参数: create, remove, mcserver, autoaccept");
                    case "create":
                        if (!GroupConfigManager.isValidGroupConfig(group.getId())){
                            GroupConfigManager.addConfig(group.getId(), new GroupConfig(group.getId()));
                            return BotUtils.sendLocalMessage("msg.bot-prefix", "创建群配置成功!");
                        } else {
                            return BotUtils.sendLocalMessage("msg.bot-prefix", "群配置已经创建过了!");
                        }
                    case "remove":
                        if (GroupConfigManager.isValidGroupConfig(group.getId())){
                            GroupConfigManager.removeConfig(group.getId());
                            return BotUtils.sendLocalMessage("msg.bot-prefix", "删除群配置成功!");
                        } else {
                            return BotUtils.sendLocalMessage("msg.bot-prefix", "群配置还没有创建!");
                        }
                    case "mcserver":
                        if (args.size() > 1){
                            if (!args.get(1).isEmpty() && GroupConfigManager.isValidGroupConfig(group.getId())){
                                GroupConfigManager.getConfig(group.getId()).setMcServerAddress(args.get(1));
                                return BotUtils.sendLocalMessage("msg.bot-prefix", "设置成功!");
                            } else
                                return BotUtils.sendLocalMessage("msg.bot-prefix", "请先创建群配置!");
                        } else {
                            return BotUtils.sendLocalMessage("msg.bot-prefix", "/config mcserver [服务器地址]", "如果地址带端口请用冒号, 例如 [服务器地址]:[端口号]");
                        }
                    case "autoaccept":
                        if (GroupConfigManager.isValidGroupConfig(group.getId())){
                            GroupConfig cfg = GroupConfigManager.getConfig(group.getId());
                            cfg.setAutoAccept(!cfg.isAutoAccept());
                            return BotUtils.sendLocalMessage("msg.bot-prefix", "已将自动接受加群请求设置为 " + cfg.isAutoAccept());
                        } else {
                            return BotUtils.sendLocalMessage("msg.bot-prefix", "群配置还没有创建!");
                        }
                }
            }
        }
        return null;
    }

    @Override
    public CommandProperties properties() {
        return new CommandProperties("config", "设置");
    }
}
