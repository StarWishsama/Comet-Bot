package io.github.starwishsama.namelessbot.listeners;

import cc.moecraft.icq.event.EventHandler;
import cc.moecraft.icq.event.IcqListener;
import cc.moecraft.icq.event.events.message.EventGroupMessage;
import cn.hutool.core.util.RandomUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class RepeatListener extends IcqListener {
    @Getter
    private static final Map<Long, RepeatData> ready2repeat = new HashMap<>();

    @EventHandler
    public void onPrepareRepeat(EventGroupMessage e){
        double chance = RandomUtil.randomDouble(0, 1);
        long id = e.getSenderId();

        if (ready2repeat.containsKey(id) && ready2repeat.get(id).getTime() > 0) {
            if (ready2repeat.get(id).getChance() > chance || ready2repeat.get(id).getTime() == 1) {
                e.respond(e.getMessage().replaceAll("我", "你").replaceAll("你", "他"));
                ready2repeat.remove(id);
            }

            if (ready2repeat.get(id) != null) {
                ready2repeat.get(id).setTime(ready2repeat.get(id).getTime() - 1);
            }
        }
    }

    @Data
    @AllArgsConstructor
    public static class RepeatData {
        private double chance;
        private int time;
    }
}
