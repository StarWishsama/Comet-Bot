package io.github.starwishsama.namelessbot.objects;

import java.util.Calendar;
import java.util.UUID;

import io.github.starwishsama.namelessbot.utils.BotUtils;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BotUser {
    private long userQQ;
    private UUID userUUID;
    private Calendar lastCheckInTime = Calendar.getInstance();
    private double checkInPoint;
    private int checkInTime = 0;
    private String bindServerAccount;
    private int msgVL;

    public BotUser(long qq){
        userQQ = qq;
        userUUID = BotUtils.generateUUID();
    }
}
