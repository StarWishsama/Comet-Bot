package io.github.starwishsama.namelessbot.managers;

import io.github.starwishsama.namelessbot.objects.user.Pet;

import java.util.LinkedList;
import java.util.List;

public class ArkManager {
    private static final List<Pet> operators = new LinkedList<>();

    public boolean register(Pet operator){
        return operators.add(operator);
    }
}
