package io.github.starwishsama.namelessbot.objects;

import java.util.Date;
import java.util.UUID;

import io.github.starwishsama.namelessbot.utils.BotUtils;

public class BotUser {
    private long userQQ;
    private UUID userUUID;
    private Calendar lastCheckInTime = Calendar.getInstance();
    private double checkInPoint;
    private int checkInTime;
    private String bindServerAccount;

    public BotUser() {
    }

    public BotUser(long qq){
        userQQ = qq;
        userUUID = BotUtils.generateUUID();
    }

    public long getUserQQ(){
        return userQQ;
    }

    public void setUserQQ(long userQQ){
        this.userQQ = userQQ;
    }

    public Calendar getLastCheckInTime(){
        return lastCheckInTime;
    }

    public void setLastCheckInTime(Calendar date){
        lastCheckInTime = date;
    }

    public double getCheckInPoint(){
        return checkInPoint;
    }

    public void setCheckInPoint(double point){
        checkInPoint = point;
    }

    public int getCheckInTime(){
        return checkInTime;
    }

    public void setCheckInTime(int time){
        checkInTime = time;
    }

    public void setBindServerAccount(String name){
        bindServerAccount = name;
    }

    public String getBindServerAccount(){
        return bindServerAccount;
    }

    public UUID getUserUUID(){
        return userUUID;
    }

    public void setUserUUID(UUID uuid){
        userUUID = uuid;
    }
}
