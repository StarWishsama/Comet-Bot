package io.github.starwishsama.namelessbot.session.commands.werewolfkill;

import lombok.Getter;

public enum PlayerType {
    wereWolve("狼人"),
    villager("村民"),
    seer("预言家"),
    doctor("女巫"),
    hunter("猎人");

    @Getter
    private String name;
    PlayerType(String name) {
        this.name = name;
    }
}
