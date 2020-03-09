package io.github.starwishsama.namelessbot.objects;

import io.github.starwishsama.namelessbot.enums.UserLevel;
import io.github.starwishsama.namelessbot.utils.BotUtils;
import lombok.Data;

import java.util.Calendar;
import java.util.UUID;

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
    private int randomTime = 20;

    public BotUser(long qq){
        userQQ = qq;
        userUUID = BotUtils.generateUuid();
    }

    public void decreaseTime(){
        this.randomTime--;
    }

    public boolean updateTime() {
        if (level == UserLevel.USER && randomTime < 20) {
            this.randomTime++;
            return true;
        }
        return false;
    }

    public void addPoint(double point) {
        checkInPoint = checkInPoint + point;
    }

    public void addTime(int time) {
        if (level == UserLevel.USER && randomTime < 20) {
            randomTime = randomTime + time;
        }
    }
}
