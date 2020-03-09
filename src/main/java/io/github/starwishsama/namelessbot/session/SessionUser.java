package io.github.starwishsama.namelessbot.session;

import lombok.Data;

/**
 * @author Nameless
 */
@Data
public class SessionUser {
    private long userId;

    public SessionUser() {

    }

    public SessionUser(long userId) {
        this.userId = userId;
    }
}
