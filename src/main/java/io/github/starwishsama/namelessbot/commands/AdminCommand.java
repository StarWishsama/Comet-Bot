package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.GroupCommand;
import cc.moecraft.icq.event.events.message.EventGroupMessage;
import cc.moecraft.icq.user.Group;
import cc.moecraft.icq.user.GroupUser;
import cc.moecraft.utils.ArrayUtils;
import io.github.starwishsama.namelessbot.BotConstants;
import io.github.starwishsama.namelessbot.utils.BotUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

/**
 * @author Nameless
 */
public class AdminCommand implements GroupCommand {
    @Override
    public String groupMessage(EventGroupMessage event, GroupUser sender, Group group, String command, ArrayList<String> args) {
        if (BotUtils.isBotOwner(sender.getId()) || BotConstants.cfg.getBotAdmins().contains(sender.getId())){
            if (args.size() > 0) {
                switch (args.get(0)) {
                    case "set":
                        if (args.size() == 2) {
                            if (BotUtils.isBotOwner(sender.getId()) && args.get(1) != null){
                                long qq = StringUtils.isNumeric(args.get(1)) ? Long.parseLong(args.get(1)) : BotUtils.parseAt(args.get(1));
                                if (qq != -1000L) {
                                    if (BotConstants.cfg.getBotAdmins() == null) {
                                        BotConstants.cfg.setBotAdmins(new ArrayList<>());
                                        BotConstants.cfg.getBotAdmins().add(qq);
                                        return BotUtils.getLocalMessage("msg.bot-prefix") + "添加机器人管理员成功!";
                                    }
                                    if (BotUtils.isBotAdmin(sender.getId())) {
                                        BotConstants.cfg.getBotAdmins().remove(qq);
                                        return BotUtils.getLocalMessage("msg.bot-prefix") + "删除机器人管理员成功!";
                                    } else {
                                        BotConstants.cfg.getBotAdmins().add(qq);
                                        return BotUtils.getLocalMessage("msg.bot-prefix") + "添加机器人管理员成功!";
                                    }
                                } else {
                                    return BotUtils.getLocalMessage("msg.bot-prefix") + "请检查QQ号是否正确!";
                                }
                            }
                        }
                        break;
                    case "filter":
                        if (args.size() > 1){
                            BotConstants.cfg.getFilterWords().add(ArrayUtils.getTheRestArgsAsString(args, 1));
                            return BotUtils.getLocalMessage("msg.bot-prefix") + "添加屏蔽词成功";
                        }
                        break;
                    case "help":
                        return "Nothing here now";
                    case "musicapi":
                        if (args.size() == 2){
                            switch (args.get(1).toLowerCase()) {
                                case "网易":
                                case "wy":
                                case "netease":
                                    BotConstants.cfg.setApi(MusicCommand.MusicType.NETEASE);
                                    return BotUtils.getLocalMessage("msg.bot-prefix") + "音乐 API 已设置为网易";
                                case "qq":
                                    BotConstants.cfg.setApi(MusicCommand.MusicType.QQ);
                                    return BotUtils.getLocalMessage("msg.bot-prefix") + "音乐 API 已设置为 QQ";
                                default:
                                    return BotUtils.getLocalMessage("msg.bot-prefix") + "/debug setapi [QQ/网易]";
                            }
                        } else
                            return BotUtils.getLocalMessage("msg.bot-prefix") + "/debug setapi [音乐API]";
                    default:
                        return null;
                }
            } else {
                return BotUtils.getLocalMessage("msg.bot-prefix") + "/admin help";
            }
        }
        return null;
    }

    @Override
    public CommandProperties properties() {
        return new CommandProperties("admin", "管理");
    }
}
