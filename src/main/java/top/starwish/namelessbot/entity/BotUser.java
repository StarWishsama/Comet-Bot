package top.starwish.namelessbot.entity;

import java.util.Date;

public class BotUser {
    private long userQQ;
    private Date lastCheckInTime = new Date();
    private double checkInPoint;
    private int checkInTime;
    private String bindServerAccount;

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
    public void setBindServerAccount(String name){
        bindServerAccount = name;
    }
    public String getBindServerAccount(){
        return bindServerAccount;
    }
}
