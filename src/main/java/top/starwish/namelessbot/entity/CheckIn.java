package top.starwish.namelessbot.entity;


import java.util.Date;

public class CheckIn {
    private long checkInQQ = 0;
    private Date lastCheckInTime = new Date();
    private double checkInPoint = 0;
    private int checkInTime = 0;

    public CheckIn() {
    }
    public long getCheckInQQ(){
        return checkInQQ;
    }
    public void setCheckInQQ(long checkInQQ){
        this.checkInQQ = checkInQQ;
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
}
