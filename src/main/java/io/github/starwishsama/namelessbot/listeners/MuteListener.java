package io.github.starwishsama.namelessbot.listeners;

import cc.moecraft.icq.event.EventHandler;
import cc.moecraft.icq.event.IcqListener;
import cc.moecraft.icq.event.events.notice.EventNoticeGroupBan;

public class MuteListener extends IcqListener {
    @EventHandler
    public void onBeenMuted(EventNoticeGroupBan e){
        if (e.getType() == EventNoticeGroupBan.BanType.BAN){

        }
    }
}
