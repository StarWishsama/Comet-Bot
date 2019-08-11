package top.starwish.namelessbot.utils;

import com.deadmandungeons.serverstatus.MinecraftServerStatus;
import com.deadmandungeons.serverstatus.ping.PingResponse;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BotUtils {
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
            e.printStackTrace();
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
            e.printStackTrace();
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
        if (addr.isEmpty() || port < 0 || port > 25565 || msg.isEmpty()){
            return "Bot > 服务器信息未设置正确";
        } else {
            try {
                PingResponse response = MinecraftServerStatus.pingServerStatus(addr, port);
                return msg.replaceAll("%延迟%", response.getLatency() + "ms")
                        .replaceAll("%在线玩家%", response.getPlayers().toString())
                        .replaceAll("%换行%", "\n")
                        .replaceAll("%MOTD%", removeColor(response.getDescription().toString()))
                        .replaceAll("%版本%", response.getVersion().toString());
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
                return ("Bot > 无法连接至 " + addr);
            }
        }
    }

    /**
     * 获取 Minecraft 服务器信息 (SRV解析 + 自定义消息样式)
     * @param addr 服务器IP
     * @param msg 自定义消息
     * @return 服务器状态
     */

    public static String getCustomServerInfo(String addr, String msg){
        if (addr.isEmpty() || msg.isEmpty()) {
            return "Bot > 服务器信息未设置正确";
        } else {
            try {
                PingResponse response = MinecraftServerStatus.pingServerStatus(addr);
                return msg.replaceAll("%延迟%", response.getLatency() + "ms")
                        .replaceAll("%在线玩家%", response.getPlayers().toString())
                        .replaceAll("%换行%", "\n")
                        .replaceAll("%MOTD%", removeColor(response.getDescription().toString()))
                        .replaceAll("%版本%", response.getVersion().toString());
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
                return ("Bot > 无法连接至 " + addr);
            }
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
}

