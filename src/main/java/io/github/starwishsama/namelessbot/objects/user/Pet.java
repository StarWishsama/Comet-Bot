package io.github.starwishsama.namelessbot.objects.user;

import lombok.Data;

@Data
public class Pet {
    private int level = 1;
    private String name;
    private long owner;
    private int exp = 0;
    private int practiceTime = 50;

    public Pet(){
    }

    public Pet(String name, Long owner){
        this.name = name;
        this.owner = owner;
    }

    public void addExp(int exp){
        this.exp = this.exp + exp;
    }
}
