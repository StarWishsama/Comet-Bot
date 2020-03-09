package io.github.starwishsama.namelessbot.listeners.commands;

import cc.moecraft.icq.event.EventHandler;
import cc.moecraft.icq.event.IcqListener;
import cc.moecraft.icq.event.events.message.EventGroupMessage;
import io.github.starwishsama.namelessbot.managers.SessionManager;
import io.github.starwishsama.namelessbot.session.Session;
import io.github.starwishsama.namelessbot.session.commands.vote.VoteSession;
import io.github.starwishsama.namelessbot.utils.BotUtils;

public class VoteListener extends IcqListener {
    @EventHandler
    public void onGroupMessage(EventGroupMessage e){
        VoteSession session = getVoteInProgress(e.getGroupId());
        final long id = e.getSenderId();
        if (session != null){
            if (System.currentTimeMillis() - session.getCreatedTime() < 60 * 1000) {
                if (BotUtils.isNoCoolDown(id)) {
                    final String response = e.getMessage();
                    if (response.contains(session.getCustomPosVote())) {
                        if (session.isVoted(id)) {
                            e.respond(session.getVoteUser(id).toString());
                            e.respond(BotUtils.sendLocalMessage("msg.bot-prefix", "你已经投过票了!"));
                        } else {
                            session.addPositiveVote();
                            session.addVoteUser(id);
                        }
                    } else if (response.contains(session.getCustomNegVote())) {
                        if (session.isVoted(id)) {
                            e.respond(BotUtils.sendLocalMessage("msg.bot-prefix", "你已经投过票了!"));
                        } else {
                            session.addNegativeVote();
                            session.addVoteUser(id);
                        }
                    }
                }
            } else {
                e.respond(BotUtils.sendLocalMessage("msg.bot-prefix", "投票已结束!",
                        "选择 " + session.getCustomPosVote() + " 的人数: " + session.getPositiveVote(),
                        "选择 " + session.getCustomNegVote() + " 的人数: " + session.getNegativeVote()));
                SessionManager.expireSession(session);
            }
        }
    }

    public static VoteSession getVoteInProgress(long groupId){
        if (!SessionManager.getSessions().isEmpty()){
            for (Session session: SessionManager.getSessions()){
                if (session instanceof VoteSession && session.getGroupId() == groupId){
                    return (VoteSession) session;
                }
            }
        }
        return null;
    }
}
