package io.github.starwishsama.namelessbot.listeners.commands;

import cc.moecraft.icq.event.EventHandler;
import cc.moecraft.icq.event.IcqListener;
import cc.moecraft.icq.event.events.message.EventGroupMessage;
import cc.moecraft.utils.StringUtils;

import io.github.starwishsama.namelessbot.session.Session;
import io.github.starwishsama.namelessbot.session.SessionManager;
import io.github.starwishsama.namelessbot.session.commands.guessnumber.GuessNumberPlayer;
import io.github.starwishsama.namelessbot.session.commands.guessnumber.GuessNumberSession;
import io.github.starwishsama.namelessbot.utils.BotUtils;

/**
 * @author Nameless
 */
public class GuessNumberListener extends IcqListener {
    @EventHandler
    public void onMessage(EventGroupMessage e){
        if (StringUtils.isNumeric(e.getMessage())) {
            GuessNumberSession session = null;
            if (SessionManager.getSessions() != null) {
                for (Session s : SessionManager.getSessions()) {
                    if (s instanceof GuessNumberSession){
                        if (((GuessNumberSession) s).getGroupId() == e.getGroupId()){
                            session = (GuessNumberSession) s;
                        }
                    }
                }

                long id = e.getSenderId();

                if (session != null) {
                    if (session.isExpire()) {
                        SessionManager.expireSession(session);
                    } else {
                        long answer = Long.parseLong(e.getMessage());
                        GuessNumberPlayer player = (GuessNumberPlayer) session.getUserById(id);
                        if (player == null) {
                            session.join(id);
                            player = (GuessNumberPlayer) session.getUserById(id);
                        }

                        if (answer < session.getAnswer()) {
                            e.respond(BotUtils.getLocalMessage("msg.bot-prefix") + "你猜的数字小了");
                            player.addGuessTime();
                            session.updateTime();
                        } else if (answer > session.getAnswer()) {
                            e.respond(BotUtils.getLocalMessage("msg.bot-prefix") + "你猜的数字大了");
                            player.addGuessTime();
                            session.updateTime();
                        } else {
                            e.respond(BotUtils.getLocalMessage("msg.bot-prefix")
                                    + e.getHttpApi().getStrangerInfo(id).getData().getNickname() + "猜对了!"
                                    + "\n次数: " + player.getGuessTime());
                            SessionManager.expireSession(session);
                        }
                    }
                }
            }
        }
    }
}
