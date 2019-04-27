package top.starwish.namelessbot.utils;

public class Utils {
    public static String deColor(String string){
        return string.replaceAll("§a", "")
                .replaceAll("§b", "")
                .replaceAll("§c", "")
                .replaceAll("§d", "")
                .replaceAll("§e", "")
                .replaceAll("§f", "")
                .replaceAll("§n", "")
                .replaceAll("§m", "")
                .replaceAll("§r", "")
                .replaceAll("§k", "")
                .replaceAll("§o", "")
                .replaceAll("§l", "")
                .replaceAll("§1", "")
                .replaceAll("§2", "")
                .replaceAll("§3", "")
                .replaceAll("§4", "")
                .replaceAll("§5", "")
                .replaceAll("§6", "")
                .replaceAll("§7", "")
                .replaceAll("§8", "")
                .replaceAll("§9", "")
                .replaceAll("§0", "");
    }
}
