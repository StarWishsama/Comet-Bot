package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.EverywhereCommand;
import cc.moecraft.icq.event.events.message.EventMessage;
import cc.moecraft.icq.sender.IcqHttpApi;
import cc.moecraft.icq.sender.returndata.returnpojo.get.RGroup;
import cc.moecraft.icq.user.User;
import cc.moecraft.utils.ArrayUtils;

import io.github.starwishsama.namelessbot.utils.BotUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

public class SayCommand implements EverywhereCommand {
    @Override
    public String run(EventMessage event, User sender, String command, ArrayList<String> args) {
       IcqHttpApi api = event.getHttpApi();
        if (BotUtils.isBotOwner(sender) && args.size() > 1) {
            switch (args.get(0).toLowerCase()){
                case "broadcast":
                case "公告":
                case "bc":
                    if (api.getGroupList() != null){
                        for (RGroup group : api.getGroupList().data){
                            String text = ArrayUtils.getTheRestArgsAsString(args, 1);
                            api.sendGroupMsg(group.getGroupId(), text);
                        }
                    } else {
                        return BotUtils.getLocalMessage("msg.bot-prefix") + "机器人还没有加入任何一个群!";
                    }
                    break;
                default:
                    if (StringUtils.isNumeric(args.get(0))){
                        String text = ArrayUtils.getTheRestArgsAsString(args, 1);
                        api.sendGroupMsg(Long.parseLong(args.get(0)), text);
                    }
            }
        }
        return null;
    }

    @Override
    public CommandProperties properties() {
        return new CommandProperties("say", "echo", "复读");
    }
}
