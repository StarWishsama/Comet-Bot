package top.starwish.namelessbot.utils;

import me.dilley.MineStat;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BotUtils {
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
    /**
     * 获取 Minecraft 服务器信息
     * @author NamelessSAMA
     * @param addr
     * @param port
     * @return serverStatus
     */
    public static String getServerInfo(String addr, int port){
        MineStat server = new MineStat(addr, port);
        if (server.isServerUp()) {
            return ("在线玩家: " + server.getCurrentPlayers() + "/" + server.getMaximumPlayers()
                    + "\n延迟:" + server.getLatency() + "ms"
                    + "\nMOTD: " + server.getMotd()
                    + "\n版本: " + server.getVersion());
        } else
            return ("[Bot] 无法连接至 " + addr);
    }

    public static String getCustomServerInfo(String addr, int port, String msg){
        MineStat server = new MineStat(addr, port);
        if (server.isServerUp()) {
            return msg.replaceAll("%延迟%", server.getLatency() + "")
                    .replaceAll("%在线玩家%", server.getCurrentPlayers())
                    .replaceAll("%换行%", "\n")
                    .replaceAll("%最大玩家%", server.getMaximumPlayers())
                    .replaceAll("%MOTD%", server.getMotd())
                    .replaceAll("%版本%", server.getVersion());
        } else
            return "[Bot] 无法连接至服务器.";
    }

    /**
     * @author NamelessSAMA
     * @param currentTime 当前时间
     * @param compareTime 需要比较的时间
     * @return true / false
     */
    public static boolean isCheckInReset(Date currentTime, Date compareTime){
        SimpleDateFormat sdt = new SimpleDateFormat("yyyy-MM-dd");
        return !sdt.format(currentTime).equals(sdt.format(compareTime));
    }

    public static double checkInPointBonus(int time){
        if (time > 0 && time <= 7){
            return 1.5;
        } else if (time > 7 && time <= 15){
            return 1.8;
        } else if (time > 15 && time <= 30){
            return 2;
        } else if (time > 30){
            return 2.2;
        }
        return 1;
    }
}
