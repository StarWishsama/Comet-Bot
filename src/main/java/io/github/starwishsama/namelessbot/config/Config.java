package io.github.starwishsama.namelessbot.config;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class Config {
    @Getter
    @Setter
    private long ownerID;
    @Getter
    @Setter
    private int autoSaveTime = 15;
    @Getter
    @Setter
    private List<Long> botAdmins;
    @Getter
    @Setter
    private int postPort = 5700;
    @Getter
    @Setter
    private String postUrl = "127.0.0.1";
    @Getter
    @Setter
    private String botName = "Bot";
    @Getter
    @Setter
    private int botPort = 5703;
    @Getter
    @Setter
    private String rconUrl;
    @Getter
    @Setter
    private int rconPort;
    @Getter
    @Setter
    private byte[] rconPwd;
    @Getter
    @Setter
    private String netEaseApi;
    @Getter
    @Setter
    private String[] cmdPrefix = new String[]{"/", "#"};
    @Getter
    @Setter
    private boolean bindMCAccount = false;

    public Config(){
    }
}
