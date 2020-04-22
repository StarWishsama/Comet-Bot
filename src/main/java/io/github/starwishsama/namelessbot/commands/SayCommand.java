package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.EverywhereCommand;
import cc.moecraft.icq.event.events.message.EventMessage;
import cc.moecraft.icq.sender.IcqHttpApi;
import cc.moecraft.icq.sender.returndata.returnpojo.get.RGroup;
import cc.moecraft.icq.user.User;
import cc.moecraft.utils.ArrayUtils;
import io.github.starwishsama.namelessbot.objects.user.BotUser;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

public class SayCommand implements EverywhereCommand {
    @Override
    public String run(EventMessage event, User sender, String command, ArrayList<String> args) {
       IcqHttpApi api = event.getHttpApi();
        if (BotUser.isBotOwner(sender) && args.size() > 0) {
            switch (args.get(0).toLowerCase()){
                case "broadcast":
                case "公告":
                case "bc":
                    String text = ArrayUtils.getTheRestArgsAsString(args, 1);
                    for (RGroup group : event.getHttpApi().getGroupList().data){
                        api.sendGroupMsg(group.getGroupId(), text);
                    }
                    break;
                default:
                    if (StringUtils.isNumeric(args.get(0))){
                        String text1 = ArrayUtils.getTheRestArgsAsString(args, 1);
                        api.sendGroupMsg(Long.parseLong(args.get(0)), text1);
                    }
            }
        }
        return null;
    }

    @Override
    public CommandProperties properties() {
        return new CommandProperties("say", "echo");
    }
}
