package io.github.starwishsama.namelessbot.listeners;

import cc.moecraft.icq.event.EventHandler;
import cc.moecraft.icq.event.IcqListener;
import cc.moecraft.icq.event.events.message.EventGroupMessage;
import io.github.starwishsama.namelessbot.config.BotCfg;
import io.github.starwishsama.namelessbot.objects.BotUser;
import io.github.starwishsama.namelessbot.utils.BotUtils;

public class SpamListener extends IcqListener {
    @EventHandler
    public void onGroupChat(EventGroupMessage e){
        Long id = e.senderId;
        if (System.currentTimeMillis() - e.getGroupUser(id).getInfo().getLastSentTime() < 3000){
            if (!BotUtils.isUserExist(id)){
                BotUser newUser = new BotUser(id);
                newUser.setMsgVL(1);
                BotCfg.users.getUsers().add(newUser);

            } else {

            }
        }
    }
}
