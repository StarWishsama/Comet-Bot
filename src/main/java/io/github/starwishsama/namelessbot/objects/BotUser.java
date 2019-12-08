package io.github.starwishsama.namelessbot.objects;

import java.util.Calendar;
import java.util.UUID;

import io.github.starwishsama.namelessbot.utils.BotUtils;
import lombok.Getter;
import lombok.Setter;

public class BotUser {
    @Setter
    @Getter
    private long userQQ;
    @Setter
    @Getter
    private UUID userUUID;
    @Setter
    @Getter
    private Calendar lastCheckInTime = Calendar.getInstance();
    @Setter
    @Getter
    private double checkInPoint;
    @Setter
    @Getter
    private int checkInTime = 0;
    @Setter
    @Getter
    private String bindServerAccount;

    public BotUser() {
    }

    public BotUser(long qq){
        userQQ = qq;
        userUUID = BotUtils.generateUUID();
    }
}
