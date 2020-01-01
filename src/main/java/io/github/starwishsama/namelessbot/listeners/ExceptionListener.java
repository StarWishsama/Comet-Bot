package io.github.starwishsama.namelessbot.listeners;

import cc.moecraft.icq.event.EventHandler;
import cc.moecraft.icq.event.IcqListener;
import cc.moecraft.icq.event.events.local.EventLocalException;
import cc.moecraft.icq.event.events.local.EventLocalSendPrivateMessage;
import cc.moecraft.icq.event.events.message.EventMessage;

import io.github.starwishsama.namelessbot.BotMain;

/**
 * 此类由 Hykilpikonna 在 2018/08/24 创建!
 * Created by Hykilpikonna on 2018/08/24!
 * Github: https://github.com/hykilpikonna
 * QQ: admin@moecraft.cc -OR- 871674895
 *
 * @author Hykilpikonna
 */

public class ExceptionListener extends IcqListener {
    @EventHandler
    public void onException(EventLocalException e)
    {
        if (e.getParentEvent() instanceof EventMessage)
        {
            ((EventMessage) e.getParentEvent()).respond("命令执行失败");
            BotMain.getLogger().warning("消息事件异常: " + e.getParentEvent().toString());
            e.getException().printStackTrace();
        }
    }
}
