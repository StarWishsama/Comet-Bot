package io.github.starwishsama.namelessbot.session.commands.guessnumber;

import io.github.starwishsama.namelessbot.session.Session;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Nameless
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GuessNumberSession extends Session {
    private final long answer;
    private long lastGuessTime = System.currentTimeMillis();

    public GuessNumberSession(long id, long groupId, long answer) {
        super.setGroupId(groupId);
        super.getUsers().add(new GuessNumberPlayer(id));
        this.answer = answer;
    }

    public boolean isExpire(){
        return System.currentTimeMillis() - lastGuessTime > 5 * 6 * 1000;
    }

    public void updateTime(){
        lastGuessTime = System.currentTimeMillis();
    }

    public void join(long id){
        super.getUsers().add(new GuessNumberPlayer(id));
    }
}
