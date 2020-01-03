package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.EverywhereCommand;
import cc.moecraft.icq.event.events.message.EventMessage;
import cc.moecraft.icq.sender.message.MessageBuilder;
import cc.moecraft.icq.sender.message.components.ComponentAt;
import cc.moecraft.icq.user.User;

import io.github.starwishsama.namelessbot.utils.BotUtils;
import io.github.starwishsama.namelessbot.utils.R6SUtils;

import java.util.ArrayList;

import com.gitlab.siegeinsights.r6tab.api.entity.player.Player;

public class R6SCommand implements EverywhereCommand {
    @Override
    public CommandProperties properties(){
        return new CommandProperties("r6s", "r6");
    }

    @Override
    public String run(EventMessage e, User user, String msg, ArrayList<String> args){
        // /r6s [UplayID]
        // /r6s [ID] [Platform]
        if (args.size() == 1){
            if (!args.get(0).isEmpty() && BotUtils.isLegitID(args.get(0))){
                Player p = R6SUtils.getR6SInfo(args.get(0));
                if (p != null) {
                    String reply = "===   R6S 战绩查询   ===\n" + p.getName() + " [" + p.getLevel() + "]级" +
                            "\n目前段位: " + p.getCurrentRank().getName() + "(" + p.getCurrentMmr() + "/" + p.getMaxMmr() + ")" +
                            "\nKD: " + p.getKd() +
                            "\n爆头率: " + p.getHeadshotAccuraccy();

                    return new MessageBuilder().add(new ComponentAt(user.getId())).newLine()
                            .add(reply).toString();
                }
            }
        }
        if (args.size() == 2 && BotUtils.isLegitID(args.get(1))){
            if (!args.get(0).isEmpty() || BotUtils.isLegitID(args.get(0)) || !args.get(1).isEmpty()){
                Player p = R6SUtils.getR6SInfo(args.get(0), args.get(1));
                if (p != null) {
                    String reply = "===   R6S 战绩查询   ===\n" + p.getName() + " [" + p.getLevel() + "]级" +
                            "\n目前段位: " + p.getCurrentRank().getName() + "(" + p.getCurrentMmr() + "/" + p.getMaxMmr() + ")" +
                            "\nKD: " + p.getKd() +
                            "\n爆头率: " + p.getHeadshotAccuraccy();

                    return new MessageBuilder().add(new ComponentAt(user.getId())).newLine()
                            .add(reply).toString();
                }
            }
        }
        return null;
    }
}
