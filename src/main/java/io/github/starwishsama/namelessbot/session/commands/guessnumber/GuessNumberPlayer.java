package io.github.starwishsama.namelessbot.session.commands.guessnumber;

import io.github.starwishsama.namelessbot.session.SessionUser;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Nameless
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GuessNumberPlayer extends SessionUser {
    private long guessTime = 1;

    public GuessNumberPlayer(long userId){
        super.setUserId(userId);
    }

    public void addGuessTime(){
        guessTime++;
    }
}
