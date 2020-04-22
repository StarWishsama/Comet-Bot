package io.github.starwishsama.namelessbot.utils;

import cn.hutool.core.util.RandomUtil;
import io.github.starwishsama.namelessbot.BotConstants;
import io.github.starwishsama.namelessbot.objects.draws.PCRCharacter;

import java.util.LinkedList;
import java.util.List;

public class PCRUtils {
    public static final int R3 = 25;
    public static final int R2 = 200;
    public static final int R1 = 775;

    public static PCRCharacter draw(){
        int chance = RandomUtil.randomInt(0, R1 + R2 + R3);
        if (chance <= R3){
            return getCharacter(3);
        } else if (chance <= R2 + R3){
            return getCharacter(2);
        } else {
            return getCharacter(1);
        }
    }

    public static List<PCRCharacter> tenTimesDraw(){
        List<PCRCharacter> result = new LinkedList<>();
        for (int i = 0; i < 10; i++){
            result.add(draw());
        }

        for (int i = 0; i < result.size(); i++){
            if (result.get(i).getStar() > 2){
                break;
            } else if (i == (result.size() -1) && result.get(i).getStar() < 2){
                result.set(i, getCharacter(2));
            }
        }

        return result;
    }

    public static PCRCharacter getCharacter(int rare){
        List<PCRCharacter> temp = new LinkedList<>();
        for (PCRCharacter c: BotConstants.pcr){
            if (c.getStar() == rare){
                temp.add(c);
            }
        }

        return temp.get(RandomUtil.randomInt(0, Math.max(1, temp.size())));
    }
}
