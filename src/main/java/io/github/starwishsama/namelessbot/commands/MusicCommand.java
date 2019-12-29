package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.EverywhereCommand;
import cc.moecraft.icq.event.events.message.EventMessage;
import cc.moecraft.icq.sender.message.MessageBuilder;
import cc.moecraft.icq.sender.message.components.ComponentMusic;
import cc.moecraft.icq.user.User;

import io.github.starwishsama.namelessbot.utils.BotUtils;
import io.github.starwishsama.namelessbot.utils.MusicID;

import java.util.ArrayList;

public class MusicCommand implements EverywhereCommand {
    @Override
    public CommandProperties properties(){
        return new CommandProperties("music", "点歌");
    }

    @Override
    public String run(EventMessage e, User user, String msg, ArrayList<String> args){
        if (BotUtils.isNoCoolDown(user.getId())) {
            if (args.size() > 1) {
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i < args.size(); i++) {
                    sb.append(args.get(i)).append(" ");
                }
                if (args.get(0).equalsIgnoreCase("wy") || args.get(0).equalsIgnoreCase("网易")) {
                    if (MusicID.getNetEaseSongID(sb.toString().trim()) > -1)
                        return new MessageBuilder().add(new ComponentMusic(MusicID.getNetEaseSongID(sb.toString().trim()), ComponentMusic.MusicSourceType.netease)).toString();
                    else
                        return BotUtils.getLocalMessage("msg.bot-prefix") + "找不到歌曲 " + sb.toString().trim();
                }
                else if (args.get(0).equalsIgnoreCase("qq")) {
                    if (MusicID.getQQMusicSongID(sb.toString().trim()) > 0)
                        return new MessageBuilder().add(new ComponentMusic(MusicID.getQQMusicSongID(sb.toString().trim()), ComponentMusic.MusicSourceType.qq)).toString();
                    else
                        return BotUtils.getLocalMessage("msg.bot-prefix") + "找不到歌曲 " + sb.toString().trim();
                }
                else
                    return BotUtils.getLocalMessage("msg.bot-prefix") + "用法: /点歌 [网易/QQ] [歌名]";
            } else
                return BotUtils.getLocalMessage("msg.bot-prefix") + "用法: /点歌 [网易/QQ] [歌名]";
        }
        else
            return null;
    }
}
