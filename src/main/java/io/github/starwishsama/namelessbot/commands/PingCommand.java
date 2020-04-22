package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.EverywhereCommand;
import cc.moecraft.icq.event.events.message.EventMessage;
import cc.moecraft.icq.user.User;
import cn.hutool.core.net.NetUtil;
import io.github.starwishsama.namelessbot.utils.BotUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class PingCommand implements EverywhereCommand {
    @Override
    public String run(EventMessage event, User sender, String command, ArrayList<String> args) {
        if (BotUtils.isNoCoolDown(sender.getId(), 30)){
            try {
                InetAddress address = InetAddress.getByName(args.get(0));
                long startTime = System.currentTimeMillis();
                if (NetUtil.ping(args.get(0), 5000)){
                    return BotUtils.sendLocalMessage("msg.bot-prefix", address.getHostAddress() + " 连接延迟 " + (System.currentTimeMillis() - startTime) + "ms");
                } else {
                    return BotUtils.sendLocalMessage("msg.bot-prefix", "无法连接至 " + address.getHostAddress());
                }
            } catch (UnknownHostException ue){
                return BotUtils.sendLocalMessage("msg.bot-prefix", "域名有误");
            }
        }
        return null;
    }

    @Override
    public CommandProperties properties() {
        return new CommandProperties("ping");
    }
}
