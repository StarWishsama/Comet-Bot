package top.starwish.namelessbot.entity;

public class SignUp {
    private long signUpQQ = 0;
    private boolean isSignUp = false;
    private long lastSignUpTime = 0;

    public SignUp(){
    }

    public SignUp(long fromQQ, boolean isSignUp, int lastSignUpTime){
        this.signUpQQ = fromQQ;
        this.isSignUp = isSignUp;
        this.lastSignUpTime = lastSignUpTime;
    }

    public boolean isSignUp(){
        return isSignUp;
    }

    public void setIsSignup(boolean isSignUp){
        this.isSignUp = isSignUp;
    }

    public long getSignUpQQ(){
        return signUpQQ;
    }

    public void setSignUpQQ(long signUpQQ){
        this.signUpQQ = signUpQQ;
    }

    public long getLastSignUpTime(){
        return lastSignUpTime;
    }

    public void setLastSignUpTime(long lastSignUpTime){
        this.lastSignUpTime = lastSignUpTime;
    }
}
