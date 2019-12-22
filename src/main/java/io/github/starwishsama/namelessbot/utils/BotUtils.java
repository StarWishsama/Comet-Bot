package io.github.starwishsama.namelessbot.utils;

import com.deadmandungeons.serverstatus.MinecraftServerStatus;
import com.deadmandungeons.serverstatus.ping.PingResponse;
import io.github.starwishsama.namelessbot.BotMain;
import io.github.starwishsama.namelessbot.config.BotCfg;
import io.github.starwishsama.namelessbot.objects.BotUser;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.*;

public class BotUtils {
    public static Map<Long, Long> coolDown = new HashMap<>();

    /**
     * @param string 需要去除彩色符号的字符串
     * @return 去除彩色符号的字符串
     */
    public static String removeColor(String string){
        return string.replaceAll("§\\S", "");
    }

    /**
     * 获取 Minecraft 服务器信息 (SRV解析)
     * @author NamelessSAMA
     * @param addr
     * @return serverStatus
     */

    public static String getServerInfo(String addr){
        try {
            PingResponse response = MinecraftServerStatus.pingServerStatus(addr);
            return ("在线玩家: " + response.getPlayers()
                    + "\n延迟:" + response.getLatency() + "ms"
                    + "\nMOTD: " + removeColor(response.getDescription().toString())
                    + "\n版本: " + response.getVersion());
        } catch (IOException | URISyntaxException e) {
            BotMain.getLogger().warning("在获取服务器信息时出现了问题, " + e);
            return ("Bot > 无法连接至 " + addr);
        }
    }

    /**
     * 获取 Minecraft 服务器信息 (非SRV解析)
     * @author NamelessSAMA
     * @param addr
     * @param port
     * @return serverStatus
     */

    public static String getServerInfo(String addr, int port){
        try {
            PingResponse response = MinecraftServerStatus.pingServerStatus(addr, port);
            return ("在线玩家: " + response.getPlayers()
                    + "\n延迟:" + response.getLatency() + "ms"
                    + "\nMOTD: " + removeColor(response.getDescription().toString())
                    + "\n版本: " + response.getVersion());
        } catch (IOException | URISyntaxException e) {
            BotMain.getLogger().warning("在获取服务器信息时出现了问题, " + e);
            return ("Bot > 无法连接至 " + addr);
        }
    }

    /**
     * 获取 Minecraft 服务器信息 (非SRV解析 + 自定义消息样式)
     * @param addr 服务器IP
     * @param port 端口
     * @param msg 自定义消息
     * @return 服务器状态
     */

    public static String getCustomServerInfo(String addr, int port, String msg){
        try {
            PingResponse response = MinecraftServerStatus.pingServerStatus(addr, port);
            return msg.replaceAll("%延迟%", response.getLatency() + "ms")
                    .replaceAll("%在线玩家%", response.getPlayers().toString())
                    .replaceAll("%换行%", "\n")
                    .replaceAll("%MOTD%", removeColor(response.getDescription().toString()))
                    .replaceAll("%版本%", response.getVersion().toString());
        } catch (IOException | URISyntaxException e) {
            BotMain.getLogger().warning("在获取服务器信息时出现了问题, " + e);
            return ("Bot > 无法连接至 " + addr);
        }
    }

    /**
     * 获取 Minecraft 服务器信息 (SRV解析 + 自定义消息样式)
     * @param addr 服务器IP
     * @param msg 自定义消息
     * @return 服务器状态
     */

    public static String getCustomServerInfo(String addr, String msg){
        try {
            PingResponse response = MinecraftServerStatus.pingServerStatus(addr);
            return msg.replaceAll("%延迟%", response.getLatency() + "ms")
                    .replaceAll("%在线玩家%", response.getPlayers().toString())
                    .replaceAll("%换行%", "\n")
                    .replaceAll("%MOTD%", removeColor(response.getDescription().toString()))
                    .replaceAll("%版本%", response.getVersion().toString());
        } catch (IOException | URISyntaxException e) {
            BotMain.getLogger().warning("在获取服务器信息时出现了问题, " + e);
            return ("Bot > 无法连接至 " + addr);
        }
    }

    /**
     * 判断是否签到过了
     *
     * @author NamelessSAMA
     * @param currentTime 当前时间
     * @param compareTime 需要比较的时间
     * @return true/false
     */
    public static boolean isCheckInReset(Date currentTime, Date compareTime){
        SimpleDateFormat sdt = new SimpleDateFormat("yyyy-MM-dd");
        return !sdt.format(currentTime).equals(sdt.format(compareTime));
    }

    /**
     * 判断指定QQ号是否仍在冷却中
     *
     * @author NamelessSAMA
     * @param qq 指定的QQ号
     * @return true/false
     */

    public static boolean hasCoolDown(long qq){
        if (coolDown != null){
            if (coolDown.containsKey(qq)){
                if (new Date().getTime() - coolDown.get(qq) <= 10 * 1000){
                    return true;
                } else
                    coolDown.remove(qq);
            } else
                BotUtils.coolDown.put(qq, new Date().getTime());
        } else
            coolDown = new HashMap<>();
        return false;
    }

    /**
     * 判断ID是否符合Minecraft用户名格式
     *
     * @author NamelessSAMA
     * @param id
     * @return true / false
     */
    public static boolean isLegitID(String id){
        return id.matches("[a-zA-Z0-9_]*");
    }

    public static UUID generateUUID(){
        return UUID.randomUUID();
    }

    public static boolean isUserExist(Long qq){
        if (BotCfg.users.getUsers() != null){
            for (BotUser user : BotCfg.users.getUsers()){
                if (user.getUserQQ() == qq)
                    return true;
            }
        }
        return false;
    }

    public static BotUser getUser(Long qq){
        if (BotCfg.users.getUsers() != null){
            for (BotUser user : BotCfg.users.getUsers()){
                if (user.getUserQQ() == qq)
                    return user;
            }
        }
        return null;
    }
}
