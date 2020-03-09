package io.github.starwishsama.namelessbot.managers;

import io.github.starwishsama.namelessbot.session.Session;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Nameless
 */
public class SessionManager {
    /**
     * 会话列表
     */
    private static List<Session> sessions = new LinkedList<>();

    public static void addSession(Session session){
        sessions.add(session);
    }

    public static void expireSession(Session session){
        sessions.remove(session);
    }

    public static boolean expireSession(long id){
        if (isValidSession(id)) {
            sessions.remove(getSession(id));
            return true;
        }
        return false;
    }

    public static boolean isValidSession(long id){
        return getSession(id) != null;
    }

    public static Session getSession(long id){
        if (!sessions.isEmpty()){
            for (Session session : sessions){
                if (session.getUserById(id) != null){
                    return session;
                }
            }
        }
        return null;
    }

    public static Session getSessionByGroup(long id){
        for (Session session : sessions){
            if (session.getGroupId() == id){
                return session;
            }
        }
        return null;
    }

    public static List<Session> getSessions(){
        return sessions;
    }
}
