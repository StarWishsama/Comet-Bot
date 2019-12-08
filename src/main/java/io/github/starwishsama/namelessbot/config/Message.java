package io.github.starwishsama.namelessbot.config;

import lombok.Getter;
import lombok.Setter;

public class Message {
    @Getter
    @Setter
    public String botPrefix;
    @Getter
    @Setter
    public String noPermission;
    @Getter
    @Setter
    public String bindSuccess;
    @Getter
    @Setter
    public String noCheckInData;

    public Message(){}
}
