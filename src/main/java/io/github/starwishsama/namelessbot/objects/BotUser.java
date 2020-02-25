package io.github.starwishsama.namelessbot.objects;

import java.util.Calendar;
import java.util.UUID;

import io.github.starwishsama.namelessbot.enums.UserLevel;
import io.github.starwishsama.namelessbot.utils.BotUtils;
import lombok.Data;

@Data
public class BotUser {
    private long userQQ;
    private UUID userUUID;
    private Calendar lastCheckInTime = Calendar.getInstance();
    private double checkInPoint;
    private int checkInTime = 0;
    private String bindServerAccount;
    private int msgVL;
    private String r6sAccount;
    private UserLevel level = UserLevel.USER;
    private int randomTime = 10;

    public BotUser(long qq){
        userQQ = qq;
        userUUID = BotUtils.generateUuid();
    }

    public void decreaseTime(){
        this.randomTime--;
    }

    public void updateTime(){
        if (level == UserLevel.USER && randomTime < 10) {
            this.randomTime++;
        }
    }
}
