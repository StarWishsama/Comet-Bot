package top.starwish.namelessbot.entity;


import java.util.Date;

public class CheckIn {
    private long checkInQQ = 0;
    private Date lastCheckInTime = new Date();
    private int checkInPoint = 0;

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
    public void setLastCheckInTime(Date lastCheckInTime){
        this.lastCheckInTime = lastCheckInTime;
    }
    public int getCheckInPoint(){
        return checkInPoint;
    }
    public void setCheckInPoint(int point){
        checkInPoint = point;
    }
}
