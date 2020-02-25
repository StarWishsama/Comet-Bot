package io.github.starwishsama.namelessbot.utils;

import cc.moecraft.icq.user.GroupUser;
import cc.moecraft.icq.user.User;
import cn.hutool.extra.emoji.EmojiUtil;
import com.deadmandungeons.serverstatus.MinecraftServerStatus;
import com.deadmandungeons.serverstatus.ping.PingResponse;

import io.github.starwishsama.namelessbot.BotMain;
import io.github.starwishsama.namelessbot.BotConstants;
import io.github.starwishsama.namelessbot.enums.UserLevel;
import io.github.starwishsama.namelessbot.objects.BotLocalization;
import io.github.starwishsama.namelessbot.objects.BotUser;

import taskeren.extrabot.components.ExComponent;
import taskeren.extrabot.components.ExComponentAt;

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

    public static boolean isNoCoolDown(long qq){
        long currentTime = System.currentTimeMillis();
        if (coolDown.containsKey(qq) && !BotUtils.isBotOwner(qq)){
            if (currentTime - coolDown.get(qq) < BotConstants.cfg.getCoolDownTime() * 1000){
                return false;
            } else {
                coolDown.remove(qq);
            }
        } else {
            BotUtils.coolDown.put(qq, currentTime);
        }
        return true;
    }

    public static boolean isNoCoolDown(User user){
        long qq = user.getId();
        long currentTime = System.currentTimeMillis();
        if (coolDown.containsKey(qq) && !BotUtils.isBotOwner(qq)){
            if (currentTime - coolDown.get(qq) < BotConstants.cfg.getCoolDownTime() * 1000){
                return false;
            } else {
                coolDown.remove(qq);
            }
        } else {
            BotUtils.coolDown.put(qq, currentTime);
        }
        return true;
    }

    /**
     * 判断ID是否符合Minecraft用户名格式
     *
     * @author NamelessSAMA
     * @param id
     * @return true / false
     */
    public static boolean isLegitId(String id){
        return id.matches("[a-zA-Z0-9_.-]*");
    }

    public static UUID generateUuid(){
        return UUID.randomUUID();
    }

    public static BotUser getUser(Long qq){
        if (BotConstants.users != null){
            for (BotUser user : BotConstants.users){
                if (user.getUserQQ() == qq) {
                    return user;
                }
            }
        } else {
            BotMain.getLogger().warning("在获取 QQ 号为 " + qq + " 的用户数据时出现了问题: 用户列表为空");
        }
        return null;
    }

    public static BotUser getUser(User sender){
        if (BotConstants.users != null){
            for (BotUser user : BotConstants.users){
                if (user.getUserQQ() == sender.getId()) {
                    return user;
                }
            }
        } else {
            BotMain.getLogger().warning("在获取 QQ 号为 " + sender.getId() + " 的用户数据时出现了问题: 用户列表为空");
        }
        return null;
    }

    public static String getLocalMessage(String node){
        if (BotConstants.msg != null){
            for (BotLocalization local : BotConstants.msg){
                if (local.getNode().equals(node)){
                    return local.getText();
                }
            }
        } else {
            BotMain.getLogger().warning("无法获取本地化文本, 文本节点为: " + node);
        }
        return null;
    }

    public static String sendLocalMessage(String node, String otherText){
        if (getLocalMessage(node) != null){
            return getLocalMessage(node) + otherText;
        }
        return null;
    }

    public static String sendLocalMessage(String node, String... otherText){
        if (getLocalMessage(node) != null){
            StringBuilder sb = new StringBuilder();
            sb.append(getLocalMessage(node));
            for (String s : otherText){
                sb.append(s).append("\n");
            }
            return sb.toString().trim();
        }
        return null;
    }

    public static boolean isUserExist(long qq) {
        return getUser(qq) != null;
    }

    public static UserLevel getLevel(long qq){
        BotUser user = getUser(qq);
        if (user != null){
            return user.getLevel();
        }
        return UserLevel.USER;
    }

    public static boolean isBotAdmin(long id){
        return getLevel(id).ordinal() > 1;
    }

    public static boolean isBotOwner(long id){
        return getLevel(id) == UserLevel.OWNER;
    }

    public static boolean isBotOwner(User sender){
        return isBotOwner(sender.getId());
    }

    public static boolean isBotAdmin(User sender){
        return isBotAdmin(sender.getId());
    }

    public static long parseAt(String msg){
        if (msg != null){
            ExComponent component = ExComponent.parseComponent(msg);
            if (component instanceof ExComponentAt){
                return ((ExComponentAt) component).getAt();
            }
        }
        return -1000L;
    }

    public static ArrayList<Long> parseAts(String msg){
        ArrayList<Long> ats = new ArrayList<>();
        ats.add(-1000L);
        if (msg != null){
            ArrayList<ExComponent> components = ExComponent.parseComponents(msg);
            for (ExComponent c : components){
                if (c instanceof ExComponentAt) {
                    ats.add(((ExComponentAt) c).getAt());
                }
            }
            ats.remove(-1000L);
        }
        return ats;
    }
}
