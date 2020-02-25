package io.github.starwishsama.namelessbot.session;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Nameless
 */
@Data
public class Session {
    private List<SessionUser> users = new LinkedList<>();
    private long groupId;

    public SessionUser getUserById(long id){
        if (!users.isEmpty()){
            for (SessionUser user : users){
                if (user.getUserId() == id){
                    return user;
                }
            }
        }
        return null;
    }
}
