package io.github.starwishsama.namelessbot.config;

import lombok.Data;

import java.util.List;

@Data
public class Config {
    private long ownerID;
    private int autoSaveTime = 15;
    private List<Long> botAdmins;
    private int postPort = 5700;
    private String postUrl = "127.0.0.1";
    private String botName = "Bot";
    private int botPort = 5703;
    private String rconUrl;
    private int rconPort;
    private byte[] rconPwd;
    private String netEaseApi;
    private String[] cmdPrefix = new String[]{"/", "#"};
    private boolean bindMCAccount = false;
}
