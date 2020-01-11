package io.github.starwishsama.namelessbot.sessions;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;


public class SessionManager {
    @Getter
    private static List<CommandSession> sessions = new ArrayList<>();

    public static void add(CommandSession session){
        sessions.add(session);
    }

    public static void expire(CommandSession session){
        sessions.remove(session);
    }
}
