package io.github.starwishsama.namelessbot.session.commands;

import io.github.starwishsama.namelessbot.session.Session;
import io.github.starwishsama.namelessbot.session.SessionUser;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class RequestSession extends Session {
    private boolean accept = false;

    public RequestSession(long id){
        SessionUser newUser = new SessionUser();
        newUser.setUserId(id);
        super.getUsers().add(newUser);
    }
}
