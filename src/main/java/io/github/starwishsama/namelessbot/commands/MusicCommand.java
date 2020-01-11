package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.EverywhereCommand;
import cc.moecraft.icq.event.events.message.EventMessage;
import cc.moecraft.icq.sender.message.MessageBuilder;
import cc.moecraft.icq.sender.message.components.ComponentMusic;
import cc.moecraft.icq.user.User;

import io.github.starwishsama.namelessbot.BotConstants;
import io.github.starwishsama.namelessbot.BotMain;
import io.github.starwishsama.namelessbot.utils.BotUtils;
import io.github.starwishsama.namelessbot.utils.MusicIDUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;

public class MusicCommand implements EverywhereCommand {

    public enum MusicType {
        QQ,
        NETEASE
    }

    @Override
    public CommandProperties properties(){
        return new CommandProperties("music", "点歌");
    }

    @Override
    public String run(EventMessage e, User user, String msg, ArrayList<String> args){
        if (BotUtils.isNoCoolDown(user.getId())) {
            if (args.size() > 1) {
                if (args.get(0).equalsIgnoreCase("set") || args.get(0).equalsIgnoreCase("设置")) {
                    if (args.size() == 2) {
                        switch (args.get(1).toLowerCase()) {
                            case "网易":
                            case "wy":
                            case "netease":
                                BotConstants.cfg.setApi(MusicType.NETEASE);
                                break;
                            case "qq":
                                BotConstants.cfg.setApi(MusicType.QQ);
                                break;
                        }
                    } else
                        return BotUtils.getLocalMessage("msg.bot-prefix") + "/music set [音乐API]";
                } else {
                    StringBuilder sb = new StringBuilder();
                    for (String arg : args) {
                        sb.append(arg).append(" ");
                    }
                    if (BotConstants.cfg.getApi().equals(MusicType.NETEASE)) {
                        if (BotConstants.cfg.getNetEaseApi() != null) {
                            boolean isAvailable = false;
                            try {
                                isAvailable = InetAddress.getByName(BotConstants.cfg.getNetEaseApi()).isReachable(1000);
                            } catch (IOException ex) {
                                BotMain.getLogger().warning("在获取网易云音乐时发生了一个问题: " + ex.getMessage());
                            }

                            if (isAvailable) {
                                if (MusicIDUtils.getNetEaseSongID(sb.toString().trim()) > -1)
                                    return new MessageBuilder().add(new ComponentMusic(MusicIDUtils.getNetEaseSongID(sb.toString().trim()), ComponentMusic.MusicSourceType.netease)).toString();
                                else
                                    return BotUtils.getLocalMessage("msg.bot-prefix") + "找不到歌曲 " + sb.toString().trim();
                            } else
                                return BotUtils.getLocalMessage("msg.bot-prefix") + "无法连接至网易云音乐 API";
                        } else
                            return BotUtils.getLocalMessage("msg.bot-prefix") + "网易云 API 地址还没有设置";
                    } else if (BotConstants.cfg.getApi().equals(MusicType.QQ)) {
                        if (MusicIDUtils.getQQMusicSongID(sb.toString().trim()) > 0)
                            return new MessageBuilder().add(new ComponentMusic(MusicIDUtils.getQQMusicSongID(sb.toString().trim()), ComponentMusic.MusicSourceType.qq)).toString();
                        else
                            return BotUtils.getLocalMessage("msg.bot-prefix") + "找不到歌曲 " + sb.toString().trim();
                    } else
                        return BotUtils.getLocalMessage("msg.bot-prefix") + "用法: /点歌 [网易/QQ] [歌名]";
                }
            }
        }
        return null;
    }
}
