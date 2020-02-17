package io.github.starwishsama.namelessbot.listeners;

import cc.moecraft.icq.event.EventHandler;
import cc.moecraft.icq.event.IcqListener;
import cc.moecraft.icq.event.events.request.EventGroupAddRequest;
import cc.moecraft.icq.user.Group;
import io.github.starwishsama.namelessbot.objects.groupconfig.GroupConfigManager;

public class GroupRequestHandler extends IcqListener {
    @EventHandler
    public void onNewGroupAdd(EventGroupAddRequest e){
        if (e.getUserId().equals(e.getSelfId())) {
            e.getBot().getAccountManager().refreshCache();
        } else {
            if (e.getBot().getGroupManager().groupCache.containsKey(e.getGroupId())){
                Group group = e.getBot().getGroupManager().groupCache.get(e.getGroupId());
                if (e.getBot().getGroupUserManager().getUserFromID(e.selfId, group).isAdmin()){
                    if (GroupConfigManager.isValidGroupConfig(e.getGroupId())){
                        if (GroupConfigManager.getConfig(e.getGroupId()).isAutoAccept()){
                            e.accept();
                        }
                    }
                }
            }
        }
    }
}
