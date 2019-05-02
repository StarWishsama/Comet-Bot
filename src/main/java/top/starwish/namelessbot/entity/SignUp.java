package top.starwish.namelessbot.entity;

public class SignUp {
    private long signUpQQ = 0;
    private boolean isSignUp = false;
    private long lastSignUpTime = 0;

    public static void main (String[] args){
        // Debug here
    }

    public SignUp(){
    }

    public SignUp(long fromQQ, boolean isSignUp, int lastSignUpTime){
        this.signUpQQ = fromQQ;
        this.isSignUp = isSignUp;
        this.lastSignUpTime = lastSignUpTime;
    }
}
