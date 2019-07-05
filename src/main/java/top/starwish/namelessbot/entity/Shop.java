package top.starwish.namelessbot.entity;

public class Shop {
    private String itemName;
    private int needPoint;
    private String itemMineCraftName;

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

    public void setItemMineCraftName(String name){
        itemMineCraftName = name;
    }

    public String getItemMineCraftName(){
        return itemMineCraftName;
    }
}
