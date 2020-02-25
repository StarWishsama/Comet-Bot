package io.github.starwishsama.namelessbot.session.commands.werewolfkill;

import lombok.Data;

@Data
public class WereWolfKillPlayer {
    private PlayerType type;
    private long voteCount;
    private boolean status;
}
