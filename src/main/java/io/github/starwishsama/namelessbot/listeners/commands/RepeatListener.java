package io.github.starwishsama.namelessbot.listeners.commands;

import cc.moecraft.icq.event.EventHandler;
import cc.moecraft.icq.event.IcqListener;
import cc.moecraft.icq.event.events.message.EventGroupMessage;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

public class RepeatListener extends IcqListener {
    @Getter
    private static Map<Long, String> repeatMessage = new LinkedHashMap<>();

    @EventHandler
    public void onGroupMessage(EventGroupMessage e) {
        final long id = e.getSenderId();
        final String msg = e.getMessage();
        if (repeatMessage.containsKey(id) && repeatMessage.get(id).equals(msg)) {
            e.respond(msg);
        }
    }
}
