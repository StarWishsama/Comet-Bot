package io.github.starwishsama.namelessbot.listeners;

import cc.moecraft.icq.event.EventHandler;
import cc.moecraft.icq.event.IcqListener;
import cc.moecraft.icq.event.events.message.EventGroupMessage;
import cc.moecraft.icq.sender.message.MessageBuilder;
import cc.moecraft.icq.sender.message.components.ComponentAt;

import io.github.starwishsama.namelessbot.BotConstants;
import io.github.starwishsama.namelessbot.objects.BotUser;
import io.github.starwishsama.namelessbot.utils.BotUtils;

public class SpamListener extends IcqListener {
    @EventHandler
    public void onGroupChat(EventGroupMessage e) {
        Long id = e.getSenderId();
        e.getHttpApi().getBot().getAccountManager().refreshCache();
        if (System.currentTimeMillis() - e.getGroupUser(id).getInfo().getLastSentTime() < 3 * 1000) {
            BotUser user = BotUtils.getUser(id);
            if (user == null) {
                BotUser newUser = new BotUser(id);
                newUser.setMsgVL(1);
                BotConstants.users.add(newUser);
            } else {
                long systemId = 1000000L;
                if (!e.isAdmin(id) && !BotUtils.isBotOwner(id) && !BotUtils.isBotAdmin(id) && id != systemId) {
                    if (user.getMsgVL() != 5) {
                        user.setMsgVL(user.getMsgVL() + 1);
                        e.getHttpApi().sendGroupMsg(e.getGroupId(), BotUtils.getLocalMessage("msg.bot-prefix")
                                + new MessageBuilder().add(new ComponentAt(id)).toString()
                                + " 你发言的速度太快了, 请放慢一点哦~");
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
