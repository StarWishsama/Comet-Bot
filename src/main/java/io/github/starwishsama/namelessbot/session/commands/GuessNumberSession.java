package io.github.starwishsama.namelessbot.session.commands;

import io.github.starwishsama.namelessbot.session.Session;

public class GuessNumberSession extends Session {
    private final long answer;
    private long guessTime = 0;

    public GuessNumberSession(long id, long answer) {
        super(id);
        this.answer = answer;
    }

    public void addCount(){
        super.updateTimeStamp();
        guessTime++;
    }

    public boolean isValid(){
        return System.currentTimeMillis() - guessTime < 5 * 6 * 1000;
    }

    public long getAnswer() {
        return answer;
    }

    public long getGuessTime(){
        return guessTime;
    }
}
