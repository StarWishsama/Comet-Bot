package io.github.starwishsama.namelessbot.listeners;

import cc.moecraft.icq.event.EventHandler;
import cc.moecraft.icq.event.IcqListener;
import cc.moecraft.icq.event.events.local.EventLocalSendMessage;
import io.github.starwishsama.namelessbot.BotConstants;


public class KeyWordListener extends IcqListener {
    @EventHandler
    public void onSendMessage(EventLocalSendMessage els){
        boolean filter = isFilterWord(els.getMessage());
        if (filter){
            // 拦截含有关键词的信息
            els.setMessage(null);
        }
    }
    /**
    //@EventHandler
    public void onGroupMessage(EventGroupMessage em){
        for (String word : BotConstants.cfg.getFilterWords()){
            boolean filter = isFilterWord(em.getMessage());
            if (filter){
                // 警告信息含有关键词
            }
        }
    } */

    private boolean isFilterWord(String context){
        if (BotConstants.cfg.getFilterWords() != null && !BotConstants.cfg.getFilterWords().isEmpty()) {
            for (String word : BotConstants.cfg.getFilterWords()) {
                if (word.equals(context)){
                    return true;
                }
            }
        }
        return false;
    }
}
