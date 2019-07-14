package top.starwish.namelessbot.entity;

public class Shop {
    private String itemName;
    private int needPoint;
    private String itemCommand;
    private int buyTime;
    private int money;

    public Shop(){
    }

    public void setItemName(String s){
        itemName = s;
    }

    public String getItemName(){
        return itemName;
    }

    public void setItemPoint(int point){
        needPoint = point;
    }

    public int getItemPoint(){
        return needPoint;
    }

    public void setItemCommand(String cmd){
        itemCommand = cmd;
    }

    public String getItemCommand(){
        return itemCommand;
    }

    public void setBuyTime(int time){
        buyTime = time;
    }

    public int getBuyTime(){
        return buyTime;
    }

    public int getMoney(){
        return money;
    }

    public void setMoney(int Money){
        money = Money;
    }
}
