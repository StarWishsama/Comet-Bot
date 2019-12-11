package io.github.starwishsama.namelessbot.objects;

import lombok.Data;

@Data
public class Shop {
    private String itemName;
    private int needPoint;
    private String itemCommand;
    private int buyTime;
    private int money;

    public Shop(String name, int point, int times, String itemCommand){
        itemName = name;
        needPoint = point;
        buyTime = times;
        this.itemCommand = itemCommand;
    }

    public Shop(String name, int point, int times, int money){
        itemName = name;
        needPoint = point;
        buyTime = times;
        this.money = money;
    }
}