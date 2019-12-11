package io.github.starwishsama.namelessbot.config;

import io.github.starwishsama.namelessbot.objects.BotUser;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.HashSet;

@Data
public class Users {
    private Collection<BotUser> users = new HashSet<>();

    public Users(){
    }
}
