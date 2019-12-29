package io.github.starwishsama.namelessbot;

import io.github.starwishsama.namelessbot.objects.BotLocalization;
import io.github.starwishsama.namelessbot.objects.BotUser;
import io.github.starwishsama.namelessbot.objects.Config;
import io.github.starwishsama.namelessbot.objects.ShopItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class BotConstants {
    public static Collection<ShopItem> shopItems = new HashSet<>();
    public static Collection<BotUser> users = new HashSet<>();
    public static List<BotLocalization> msg = new ArrayList<>();
    public static Config cfg = new Config();
}
