package top.starwish.namelessbot.entity;


import java.util.Date;

public class Checkin {
    private long checkInQQ = 0;
    private boolean isCheckIn = false;
    private Date lastCheckInTime = new Date();

    public Checkin(){
    }

    public Checkin(long fromQQ, boolean isCheckIn, Date lastCheckInTime){
        this.checkInQQ = fromQQ;
        this.isCheckIn = isCheckIn;
        this.lastCheckInTime = lastCheckInTime;
    }

    public boolean isCheckIn(){
        return isCheckIn;
    }

    public void setIsSignup(boolean isSignUp){
        this.isCheckIn = isSignUp;
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
}
