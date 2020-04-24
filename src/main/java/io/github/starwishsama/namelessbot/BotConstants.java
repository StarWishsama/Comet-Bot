package io.github.starwishsama.namelessbot;

import io.github.starwishsama.namelessbot.objects.BotLocalization;
import io.github.starwishsama.namelessbot.objects.Config;
import io.github.starwishsama.namelessbot.objects.draws.ArkNightOperator;
import io.github.starwishsama.namelessbot.objects.draws.PCRCharacter;
import io.github.starwishsama.namelessbot.objects.group.GroupShop;
import io.github.starwishsama.namelessbot.objects.user.BotUser;
import io.github.starwishsama.namelessbot.objects.user.ClockInData;
import io.github.starwishsama.namelessbot.objects.user.RandomResult;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BotConstants {
    public static List<GroupShop> shop = new LinkedList<>();
    public static Collection<BotUser> users = new HashSet<>();
    public static List<BotLocalization> msg = new ArrayList<>();
    public static Config cfg = new Config();
    public static List<RandomResult> underCovers = new LinkedList<>();
    public static Map<Long, ClockInData> data = new HashMap<>();
    public static Map<String, Integer> repeatData = new ConcurrentHashMap<>();
    public static List<ArkNightOperator> operators = new LinkedList<>();
    public static List<PCRCharacter> pcr = new LinkedList<>();
}
