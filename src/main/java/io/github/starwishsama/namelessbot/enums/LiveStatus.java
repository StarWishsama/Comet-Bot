package io.github.starwishsama.namelessbot.enums;

import lombok.Getter;

public enum LiveStatus {
    ONLINE("✓"),
    OFFLINE("✘"),
    NOT_FOUND("?");

    @Getter
    private final String status;

    LiveStatus(String status){
        this.status = status;
    }
}
