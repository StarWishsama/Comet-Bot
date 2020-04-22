package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.EverywhereCommand;
import cc.moecraft.icq.event.events.message.EventMessage;
import cc.moecraft.icq.sender.message.MessageBuilder;
import cc.moecraft.icq.sender.message.components.ComponentMusic;
import cc.moecraft.icq.user.User;
import io.github.starwishsama.namelessbot.BotConstants;
import io.github.starwishsama.namelessbot.utils.BotUtils;
import io.github.starwishsama.namelessbot.utils.MusicIDUtils;

import java.util.ArrayList;

public class MusicCommand implements EverywhereCommand {

    public enum MusicType {
        QQ,
        NETEASE
    }

    @Override
    public CommandProperties properties(){
        return new CommandProperties("music", "歌", "点歌", "song");
    }

    @Override
    public String run(EventMessage e, User user, String msg, ArrayList<String> args){
        if (BotUtils.isNoCoolDown(user.getId())) {
            if (args.size() > 0) {
                StringBuilder sb = new StringBuilder();
                for (String arg : args) {
                    sb.append(arg).append(" ");
                }
                String songName = sb.toString().trim();
                if (BotConstants.cfg.getApi().equals(MusicType.NETEASE)) {
                    if (BotConstants.cfg.getNetEaseApi() != null) {
                        if (MusicIDUtils.getNetEaseSongID(songName) != -1)
                            return new MessageBuilder().add(new ComponentMusic(MusicIDUtils.getNetEaseSongID(songName), ComponentMusic.MusicSourceType.netease)).toString();
                        else
                            return BotUtils.getLocalMessage("msg.bot-prefix") + "找不到歌曲 " + songName;
                    } else
                        return BotUtils.getLocalMessage("msg.bot-prefix") + "网易云 API 地址还没有设置";
                } else if (BotConstants.cfg.getApi().equals(MusicType.QQ)) {
                    if (MusicIDUtils.getQQMusicSongID(songName) != -1)
                        return new MessageBuilder().add(new ComponentMusic(MusicIDUtils.getQQMusicSongID(songName), ComponentMusic.MusicSourceType.qq)).toString();
                    else
                        return BotUtils.getLocalMessage("msg.bot-prefix") + "找不到歌曲 " + songName;
                } else
                    return BotUtils.getLocalMessage("msg.bot-prefix") + "在点歌时发生了异常, 请联系管理员";
            }
        }
        return null;
    }
}
