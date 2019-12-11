package io.github.starwishsama.namelessbot.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class Message {
    public String botPrefix;
    public String noPermission;
    public String bindSuccess;
    public String noCheckInData;

    public Message(){}
}
