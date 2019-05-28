package top.starwish.namelessbot.entity;

import java.util.Date;

public class BotUser {
    private long userQQ = 0;
    private Date lastCheckInTime = new Date();
    private double checkInPoint = 0;
    private int checkInTime = 0;
    private boolean isBotAdmin = true;

    public BotUser() {
    }
    public long getUserQQ(){
        return userQQ;
    }
    public void setUserQQ(long userQQ){
        this.userQQ = userQQ;
    }
    public Date getLastCheckInTime(){
        return lastCheckInTime;
    }
    public void setLastCheckInTime(Date date){
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
    public boolean isBotAdmin(){
        return isBotAdmin;
    }
    public void setBotAdmin(boolean bool) {
        isBotAdmin = bool;
    }
}
