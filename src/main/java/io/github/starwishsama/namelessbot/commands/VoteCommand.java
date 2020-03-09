package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.GroupCommand;
import cc.moecraft.icq.event.events.message.EventGroupMessage;
import cc.moecraft.icq.user.Group;
import cc.moecraft.icq.user.GroupUser;
import io.github.starwishsama.namelessbot.listeners.commands.VoteListener;
import io.github.starwishsama.namelessbot.managers.SessionManager;
import io.github.starwishsama.namelessbot.session.commands.vote.VoteSession;
import io.github.starwishsama.namelessbot.utils.BotUtils;

import java.util.ArrayList;

public class VoteCommand implements GroupCommand {
    @Override
    public String groupMessage(EventGroupMessage event, GroupUser sender, Group group, String command, ArrayList<String> args) {
        if (args.isEmpty()){
            return BotUtils.sendLocalMessage("msg.bot-prefix", "/vote create [投票内容]");
        } else {
            if (args.get(0).equalsIgnoreCase("create")){
                if (VoteListener.getVoteInProgress(group.getId()) == null) {
                    VoteSession session = new VoteSession(args.get(1), sender.getId(), group.getId());
                    if (args.size() == 2) {
                        SessionManager.addSession(session);
                    }
                    if (args.size() == 4) {
                        session.setCustomPosVote(args.get(2));
                        session.setCustomNegVote(args.get(3));
                        SessionManager.addSession(session);
                    }
                    return BotUtils.sendLocalMessage("msg.bot-prefix", sender.refreshInfo().getNickname() + " 发起了一个投票",
                            "投票内容: " + session.getVoteContext(),
                            "同意请回复 \"" + session.getCustomPosVote() + "\"",
                            "不同意请回复 \"" + session.getCustomNegVote() + "\""
                    );
                } else {
                    return BotUtils.sendLocalMessage("msg.bot-prefix", "有一个投票正在进行中!");
                }
            } else if (args.get(0).equalsIgnoreCase("cancel")){
                if (BotUtils.isBotAdmin(sender.getId())) {
                    if (VoteListener.getVoteInProgress(group.getId()) != null) {
                        SessionManager.expireSession(VoteListener.getVoteInProgress(group.getId()));
                        return BotUtils.sendLocalMessage("msg.bot-prefix", "成功");
                    } else {
                        return BotUtils.sendLocalMessage("msg.bot-prefix", "没有正在进行的投票");
                    }
                }
            }
        }
        return null;
    }

    @Override
    public CommandProperties properties() {
        return new CommandProperties("vote", "投票");
    }
}
