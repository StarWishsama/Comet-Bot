package io.github.starwishsama.namelessbot.config;

import io.github.starwishsama.namelessbot.objects.Shop;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.HashSet;

public class ShopItems {
    @Getter
    @Setter
    public static Collection<Shop> shopItems = new HashSet<>();

    public ShopItems(){}
}
