package io.github.starwishsama.namelessbot.session;

import lombok.Data;

@Data
public class Session {
    private final long id;
    private long timeStamp = System.currentTimeMillis();

    public void updateTimeStamp(){
        timeStamp = System.currentTimeMillis();
    }
}
