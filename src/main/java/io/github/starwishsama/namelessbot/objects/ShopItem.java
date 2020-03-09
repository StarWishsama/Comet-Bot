package io.github.starwishsama.namelessbot.objects;

import lombok.Data;

@Data
public class ShopItem {
    private String itemName;
    private double point;
    private String itemCommand;
    private int buyTime;

    public ShopItem() {
    }

    public ShopItem(String itemName, double point, int times, String itemCommand) {
        this.itemName = itemName;
        this.point = point;
        buyTime = times;
        this.itemCommand = itemCommand;
    }

    public ShopItem(String itemName, double point, String itemCommand) {
        this.itemName = itemName;
        this.point = point;
        this.itemCommand = itemCommand;
    }
}