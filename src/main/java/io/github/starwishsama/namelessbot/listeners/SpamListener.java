package io.github.starwishsama.namelessbot.listeners;

import cc.moecraft.icq.event.EventHandler;
import cc.moecraft.icq.event.IcqListener;
import cc.moecraft.icq.event.events.message.EventGroupMessage;

import cc.moecraft.icq.sender.message.MessageBuilder;
import cc.moecraft.icq.sender.message.components.ComponentAt;

import io.github.starwishsama.namelessbot.config.FileSetup;
import io.github.starwishsama.namelessbot.BotConstants;
import io.github.starwishsama.namelessbot.objects.BotUser;
import io.github.starwishsama.namelessbot.utils.BotUtils;

import java.util.Collection;

public class SpamListener extends IcqListener {
    @EventHandler
    public void onGroupChat(EventGroupMessage e) {
        if (BotConstants.cfg.isAntiSpam()) {
            Long id = e.getSenderId();
            if (System.currentTimeMillis() - e.getGroupUser(id).getInfo().getLastSentTime() < 3000) {
                Collection<BotUser> users = BotConstants.users;
                BotUser user = BotUtils.getUser(id);
                if (user == null) {
                    BotUser newUser = new BotUser(id);
                    newUser.setMsgVL(1);
                    users.add(newUser);
                } else {
                    if (!e.isAdmin(id)) {
                        if (user.getMsgVL() != 5) {
                            user.setMsgVL(user.getMsgVL() + 1);
                        } else {
                            e.ban(BotConstants.cfg.getSpamMuteTime());
                            e.getHttpApi().sendGroupMsg(e.getGroupId(), BotUtils.getLocalMessage("msg.bot-prefix")
                                    + new MessageBuilder().add(new ComponentAt(id)).toString()
                                    + " 因为刷屏被禁言");
                        }
                    }
                }
            }
        }
    }
}
