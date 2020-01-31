package io.github.starwishsama.namelessbot.listeners.commands;

import cc.moecraft.icq.event.EventHandler;
import cc.moecraft.icq.event.IcqListener;
import cc.moecraft.icq.event.events.message.EventGroupMessage;
import io.github.starwishsama.namelessbot.session.SessionManager;
import io.github.starwishsama.namelessbot.session.commands.GuessNumberSession;
import io.github.starwishsama.namelessbot.utils.BotUtils;
import org.apache.commons.lang3.StringUtils;

public class GuessNumberListener extends IcqListener {
    @EventHandler
    public void onMessage(EventGroupMessage e){
        if (StringUtils.isNumeric(e.getMessage()) && SessionManager.isValidSession(e.getSenderId())) {
            GuessNumberSession session = (GuessNumberSession) SessionManager.getSession(e.getSenderId());
            if (session.isValid()) {
                SessionManager.expireSession(e.getSenderId());
            } else {
                long answer = Long.parseLong(e.getMessage());

                if (session.getGuessTime() < 15) {
                    if (answer < session.getAnswer()) {
                        e.respond(BotUtils.getLocalMessage("msg.bot-prefix") + "你猜的数字小了");
                        session.addCount();
                    } else if (answer > session.getAnswer()) {
                        e.respond(BotUtils.getLocalMessage("msg.bot-prefix") + "你猜的数字大了");
                        session.addCount();
                    } else {
                        e.respond(BotUtils.getLocalMessage("msg.bot-prefix") + "你猜对了!\n尝试次数: " + session.getGuessTime());
                        SessionManager.expireSession(e.getSenderId());
                    }
                } else {
                    e.respond(BotUtils.getLocalMessage("msg.bot-prefix") + "你输了!\n正确答案是: " + session.getAnswer());
                    SessionManager.expireSession(e.getSenderId());
                }
            }
        }
    }
}
