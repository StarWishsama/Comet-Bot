package io.github.starwishsama.namelessbot;

import io.github.starwishsama.namelessbot.objects.*;
import lombok.Data;
import lombok.Getter;

import java.util.*;

@Data
public class BotConstants {
    public static Collection<ShopItem> shopItems = new HashSet<>();
    public static Collection<BotUser> users = new HashSet<>();
    public static List<BotLocalization> msg = new ArrayList<>();
    public static Config cfg = new Config();
    public static List<String> livers = new ArrayList<>();
    public static List<RandomResult> underCovers = new LinkedList<>();
}
