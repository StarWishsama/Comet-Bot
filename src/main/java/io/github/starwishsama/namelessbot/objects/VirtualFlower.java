package io.github.starwishsama.namelessbot.objects;

import io.github.starwishsama.namelessbot.enums.FlowerType;
import lombok.Data;

@Data
public class VirtualFlower {
    private int level;
    private String name;
    private FlowerType type;
    private long exp;

    public VirtualFlower(String name, FlowerType type) {
        this.name = name;
        this.type = type;
    }
}
