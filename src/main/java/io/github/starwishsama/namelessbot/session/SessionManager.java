package io.github.starwishsama.namelessbot.session;

import java.util.LinkedHashMap;
import java.util.Map;

public class SessionManager {
    private static Map<Long, Session> sessions = new LinkedHashMap<>();

    public static void addSession(Session session){
        sessions.put(null, session);
    }

    public static void addSession(long id, Session session){
        sessions.put(id, session);
    }

    public static void expireSession(long id){
        sessions.remove(id);
    }

    public static boolean isValidSession(long id){
        return sessions.get(id) != null;
    }

    public static Session getSession(long id){
        return sessions.get(id);
    }
}
