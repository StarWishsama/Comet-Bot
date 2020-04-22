package io.github.starwishsama.namelessbot.utils;

import cn.hutool.core.util.RandomUtil;
import io.github.starwishsama.namelessbot.BotConstants;
import io.github.starwishsama.namelessbot.objects.draws.ArkNightOperator;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ArkNightUtils {
    public static List<ArkNightOperator> tenTimeDraw(){
        List<ArkNightOperator> ops = new ArrayList<>();
        for (int i = 0; i < 10; i++){
            ops.add(draw());
        }
        return ops;
    }

    public static ArkNightOperator draw(){
        double probability = RandomUtil.randomDouble(0, 1, 2, RoundingMode.HALF_DOWN);
        int rare;

        if (BotUtils.inRange(probability, 0.48, 0.50)){
            rare = 6;
        } else if (BotUtils.inRange(probability, 0.0, 0.08)){
            rare = 5;
        } else if (BotUtils.inRange(probability, 0.40, 0.50)){
            rare = 4;
        } else {
            rare = 3;
        }

        return getOperator(rare);
    }

    public static ArkNightOperator getOperator(int rare){
        List<ArkNightOperator> ops = BotConstants.operators;
        List<ArkNightOperator> tempOps = new LinkedList<>();

        for (ArkNightOperator op: ops){
            if (op.getRare() == rare){
                tempOps.add(op);
            }
        }

        return tempOps.get(RandomUtil.randomInt(1, tempOps.size()));
    }
}
