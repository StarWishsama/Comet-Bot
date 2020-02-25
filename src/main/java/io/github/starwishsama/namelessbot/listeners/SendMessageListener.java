package io.github.starwishsama.namelessbot.listeners;

import cc.moecraft.icq.event.EventHandler;
import cc.moecraft.icq.event.IcqListener;
import cc.moecraft.icq.event.events.local.EventLocalSendMessage;
import io.github.starwishsama.namelessbot.BotConstants;


public class SendMessageListener extends IcqListener {
    public static boolean botSwitch = true;

    @EventHandler
    public void onSendMessage(EventLocalSendMessage els){
        if (botSwitch) {
            boolean filter = isFilterWord(els.getMessage());
            if (filter) {
                // 拦截含有关键词的信息
                els.setMessage(null);
            }
        } else {
            els.setCancelled(true);
        }
    }

    private boolean isFilterWord(String context){
        if (BotConstants.cfg.getFilterWords() != null && !BotConstants.cfg.getFilterWords().isEmpty()) {
            for (String word : BotConstants.cfg.getFilterWords()) {
                if (context.contains(word)){
                    return true;
                }
            }
        }
        return false;
    }
}
