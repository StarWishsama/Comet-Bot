package io.github.starwishsama.namelessbot.listeners;

import cc.moecraft.icq.event.EventHandler;
import cc.moecraft.icq.event.IcqListener;
import cc.moecraft.icq.event.events.message.EventPrivateMessage;
import cc.moecraft.icq.event.events.request.EventGroupInviteRequest;
import io.github.starwishsama.namelessbot.BotConstants;
import io.github.starwishsama.namelessbot.managers.SessionManager;
import io.github.starwishsama.namelessbot.session.Session;
import io.github.starwishsama.namelessbot.session.commands.RequestSession;

public class RequestListener extends IcqListener {
    @EventHandler
    public void onReceiveRequest(EventGroupInviteRequest e){
        if (e.getUserId() == BotConstants.cfg.getOwnerID()){
            e.accept();
        } else {
            if (BotConstants.cfg.getOwnerID() != 0) {
                Session session = SessionManager.getSession(e.getUserId());
                if (session != null) {
                    if (session instanceof RequestSession) {
                        RequestSession rs = (RequestSession) session;
                        if (rs.isAccept()){
                            e.accept();
                        } else if (!rs.isAccept()){
                            e.reject(null);
                        }
                    } else {
                        SessionManager.addSession(new RequestSession(BotConstants.cfg.getOwnerID()));
                        e.getHttpApi().sendPrivateMsg(BotConstants.cfg.getOwnerID(), "收到来自 "
                                + e.getHttpApi().getStrangerInfo(e.getUserId()).getData().getNickname()
                                + "(" + e.getUserId() + ")"
                                + "的入群邀请, 群号: " + e.getGroupId() + "\n是否接受? 回复 [是/否]");
                    }
                }
            }
        }
    }

    @EventHandler
    public void onHandleSession(EventPrivateMessage e){
        if (e.getSender().getId() == BotConstants.cfg.getOwnerID()) {
            if (e.getMessage().equals("是") || e.getMessage().equals("否")) {
                if (SessionManager.getSession(BotConstants.cfg.getOwnerID()) instanceof RequestSession) {
                    RequestSession session = (RequestSession) SessionManager.getSession(BotConstants.cfg.getOwnerID());
                    if (e.getMessage().equals("是")){
                        session.setAccept(true);
                    } else if (e.getMessage().equals("否")){
                        session.setAccept(false);
                    }
                    SessionManager.expireSession(session);
                }
            }
        }
    }
}
