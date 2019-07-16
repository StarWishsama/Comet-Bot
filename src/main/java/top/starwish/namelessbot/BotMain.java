package top.starwish.namelessbot;

import com.sobte.cqp.jcq.entity.*;
import com.sobte.cqp.jcq.event.JcqAppAbstract;

import com.spotify.dns.LookupResult;
import net.kronos.rkon.core.Rcon;
import net.kronos.rkon.core.ex.AuthenticationException;
import org.apache.commons.lang3.StringUtils;
import com.alibaba.fastjson.*;

import top.starwish.namelessbot.entity.BotUser;
import top.starwish.namelessbot.entity.QQGroup;
import top.starwish.namelessbot.entity.RssItem;
import top.starwish.namelessbot.entity.Shop;
import top.starwish.namelessbot.utils.FileProcess;
import top.starwish.namelessbot.utils.BotUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.IntStream;

public class BotMain extends JcqAppAbstract implements ICQVer, IMsg, IRequest {
    private String statusPath = CQ.getAppDirectory() + "status.json";
    private String groupsPath = CQ.getAppDirectory() + "groupsettings.json";
    private String rconPath = CQ.getAppDirectory() + "rcon.json";

    boolean botStatus = true;

    String rconIP = null;
    int rconPort = 0;
    String rconPwd = null;
    boolean isRcon = true;

    RssItem solidot = new RssItem("https://www.solidot.org/index.rss");
    RssItem jikeWakeUp = new RssItem("https://rsshub.app/jike/topic/text/553870e8e4b0cafb0a1bef68");
    RssItem todayOnHistory = new RssItem("http://api.lssdjt.com/?ContentType=xml&appkey=rss.xml");

    long ownerQQ = 0;
    List<Long> adminIds = new ArrayList<>();
    List<String> filterWords = new ArrayList<>();
    
    List<QQGroup> groupSetting = new ArrayList<>();
    Map<Long, BotUser> checkinUsers = new HashMap<>();
    Map<String, Shop> shopItems = new HashMap<>();

    /**
     * @brief Init plugin
     * @return always 0
     */

    public int startup() {
        CQ.logInfoSuccess("NamelessBot", "初始化完成, 欢迎使用!");
        return 0;
    }

    public int privateMsg(int subType, int msgId, long fromQQ, String msg, int font) {
        if (msg.startsWith("/") || msg.startsWith("#")) {
            boolean isBotAdmin = adminIds.toString().contains(fromQQ + "");
            boolean isOwner = fromQQ == ownerQQ;

            // process only after there's a command, in order to get rid of memory trash
            String temp = msg.trim();
            String[] cmd = {"", "", "", "", "", "", ""};

            /**
             * @brief Processing msg into cmd & params
             * @param cmd[0] << cmd after !, e.g. "help" cmd[1] << first param etc.
             * @author Stiven.ding
             */

            for (int i = 0; i < 7; i++) {
                temp = temp.trim();
                if (temp.indexOf(' ') > 0) {
                    cmd[i] = temp.substring(0, temp.indexOf(' '));
                    temp = temp.substring(temp.indexOf(' ') + 1);
                } else {
                    cmd[i] = temp.trim();
                    break;
                }
            }
            cmd[0] = cmd[0].substring(1); // del '!'/'/' at the beginning of cmd


            if (isBotAdmin || isOwner || fromQQ == 1552409060) {
                switch (cmd[0]) {
                    case "shop":
                        switch (cmd[1].toLowerCase()) {
                            case "add":
                                if (isOwner || isBotAdmin) {
                                    if (!cmd[2].equals("") && StringUtils.isNumeric(cmd[3]) && !cmd[4].equals("") && !cmd[5].equals("")) {
                                        if (StringUtils.isNumeric(cmd[5])){
                                            Shop newItem = new Shop();
                                            newItem.setItemName(cmd[2]);
                                            newItem.setItemPoint(Integer.parseInt(cmd[3]));
                                            newItem.setBuyTime(Integer.parseInt(cmd[4]));
                                            newItem.setItemCommand("eco give %p " + cmd[5]);
                                            newItem.setMoney(Integer.parseInt(cmd[5]));
                                        } else {
                                            Shop s = new Shop();
                                            s.setItemName(cmd[2]);
                                            s.setItemPoint(Integer.parseInt(cmd[3]));
                                            s.setItemCommand(msg.trim().substring(1 + cmd[0].length() + 1 + cmd[1].length() + 1 + cmd[2].length() + 1 + cmd[3].length() + 1 + cmd[4].length() + 1));
                                            s.setBuyTime(Integer.parseInt(cmd[4]));
                                            shopItems.put(cmd[2], s);
                                            mySendPrivateMsg(fromQQ, "Bot > 已添加商品 " + cmd[2] + "!");
                                        }
                                    } else
                                        mySendPrivateMsg(fromQQ, "Bot > /shop add [商品名] [需要积分] [可购买次数] [执行命令]\n/shop add [商品名] [需要积分] [可购买次数] [获得时光币]\n玩家绑定账号可使用 %p代替!\n可购买次数设为 -1 以改为无限购买");
                                } else
                                    mySendPrivateMsg(fromQQ, "Bot > 你没有权限!");
                                break;
                            case "del":
                                if (isOwner || isBotAdmin) {
                                    if (!cmd[2].isEmpty()) {
                                        if (shopItems.containsKey(cmd[2])) {
                                            shopItems.remove(cmd[2]);
                                            mySendPrivateMsg(fromQQ, "Bot > 删除商品 " + cmd[2] + " 成功!");
                                        } else
                                            mySendPrivateMsg(fromQQ, "Bot > 商品 " + cmd[2] + " 不存在!");
                                    } else
                                        mySendPrivateMsg(fromQQ, "Bot > /shop del [商品名]");
                                }
                                break;
                            default:
                                mySendPrivateMsg(fromQQ, "Bot > 欢迎来到积分商店!\n/shop add 添加商品\n/shop del 删除商品");
                                break;
                        }
                        break;
                    case "say":
                        String message = msg.replaceFirst("/" + cmd[0] + " ", "").replaceFirst(cmd[1] + " ", "").replaceFirst("#", "");
                        for (QQGroup group : groupSetting) {
                            if (group.getGroupAliases().contains(cmd[1])) {
                                mySendGroupMsg(group.getGroupID(), message);
                                break;
                            } else if (StringUtils.isNumeric(cmd[1])){
                                mySendGroupMsg(Integer.parseInt(cmd[1]), message);
                                break;
                            } else {
                                mySendPrivateMsg(fromQQ, "Bot > 请检查群号是否有误!");
                            }
                        }
                        break;
                    case "switch":
                        if (cmd[1].equals("off")) {
                            mySendPrivateMsg(fromQQ, "Bot > 已将机器人禁言.");
                            botStatus = false;
                            saveConf();
                        } else if (cmd[1].equals("on")) {
                            mySendPrivateMsg(fromQQ, "Bot > 已解除机器人的禁言.");
                            botStatus = true;
                        }
                        break;
                    case "rcon":
                        if (isRcon) {
                            switch (cmd[1]) {
                                case "cmd":
                                    if (!cmd[2].isEmpty()) {
                                        try {
                                            Rcon rcon = new Rcon(rconIP, rconPort, rconPwd.getBytes());
                                            String command = msg.replace("/rcon cmd ", "").replace("#rcon cmd", "");
                                            String result = rcon.command(command);
                                            CQ.logInfo("RCON", fromQQ + " 尝试执行服务器命令: " + command);
                                            mySendPrivateMsg(fromQQ, "Bot > \n" + result.trim());
                                        } catch (IOException e) {
                                            mySendPrivateMsg(fromQQ, "Bot > 无法连接至服务器");
                                        } catch (AuthenticationException e){
                                            mySendPrivateMsg(fromQQ, "Bot > RCon 密码错误!");
                                        }
                                    } else
                                        mySendPrivateMsg(fromQQ, "Bot > 请输入需要执行的命令! (不需要带\"/\")");
                                    break;
                                case "setup":
                                    if (!cmd[2].isEmpty() && StringUtils.isNumeric(cmd[3]) && !cmd[4].isEmpty()) {
                                        rconIP = cmd[2];
                                        rconPort = Integer.parseInt(cmd[3]);
                                        rconPwd = cmd[4];
                                        mySendPrivateMsg(fromQQ, "Bot > RCON 设置完成");
                                    } else
                                        mySendPrivateMsg(fromQQ, "Bot > 请输入正确的 RCON 地址/端口/密码!");
                                    break;
                                default:
                                    mySendPrivateMsg(fromQQ,
                                            "= RCON 设置 =\n" +
                                                    "/rcon setup [IP] [Port] [Password] 设置 Rcon 配置\n" +
                                                    "/rcon cmd [Command] 使用 Rcon 执行命令");
                                    break;
                            }
                        } else
                            mySendPrivateMsg(fromQQ, "Bot > 很抱歉, 机器人没有启用 RCON 功能.");
                        break;
                    case "help":
                        switch (cmd[1].toLowerCase()) {
                            default:
                            case "1":
                                mySendPrivateMsg(fromQQ, "= 无名Bot " + VerClass.VERSION + " ="
                                        + "\n /say [指定群] [内容] 以机器人向某个群发送消息"
                                        + "\n /switch [on/off] 开关机器人"
                                        + "\n /rcon 使用 Minecraft 服务器的 Rcon 功能"
                                        + "\n /admin 管理机器人的管理员"
                                        + "\n /setting 机器人设置"
                                        + "\n========== 1/2 ==========");
                                break;
                            case "2":
                                mySendPrivateMsg(fromQQ, "= 无名Bot " + VerClass.VERSION + " ="
                                        + "\n /reload 重载配置"
                                        + "\n /save 保存配置"
                                        + "\n /rconswitch Rcon 开关"
                                        + "\n========== 2/2 =========="
                                        );
                        }
                        break;
                    case "rconswitch":
                        if (isRcon) {
                            isRcon = false;
                            mySendPrivateMsg(fromQQ, "Bot > 已关闭 Rcon 功能");
                        } else
                            isRcon = true;
                        mySendPrivateMsg(fromQQ, "Bot > 已打开 Rcon 功能. ");
                        break;
                    case "admin":
                        if (isOwner) {
                            switch (cmd[1]) {
                                case "list":
                                    mySendPrivateMsg(fromQQ, "Bot > 机器人管理员列表: " + adminIds.toString());
                                    break;
                                case "add":
                                    if (!cmd[2].equals("") && StringUtils.isNumeric(cmd[2])) {
                                        long adminQQ = Integer.parseInt(cmd[2]);
                                        adminIds.add(adminQQ);
                                        mySendPrivateMsg(fromQQ, "Bot > 已成功添加管理员 " + CQ.getStrangerInfo(adminQQ).getNick() + "(" + adminQQ + ")");
                                    }
                                    break;
                                case "del":
                                    if (!cmd[2].equals("") && StringUtils.isNumeric(cmd[2])) {
                                        long adminQQ = Integer.parseInt(cmd[2]);
                                        adminIds.remove(adminQQ);
                                        mySendPrivateMsg(fromQQ, "Bot > 已成功删除管理员 " + CQ.getStrangerInfo(adminQQ).getNick() + "(" + adminQQ + ")");
                                        break;
                                    }
                            }
                        } else
                            mySendPrivateMsg(fromQQ, "Bot > 你不是我的主人, 无法设置管理员哟");
                        break;
                    case "reload":
                        readConf();
                        mySendPrivateMsg(fromQQ, "Bot > Config reloaded.");
                        break;
                    case "save":
                        saveConf();
                        mySendPrivateMsg(fromQQ, "Bot > Config saved.");
                        break;
                    case "group":
                        switch (cmd[1].toLowerCase()) {
                            case "set":
                                switch (cmd[2].toLowerCase()) {
                                    case "aliases":
                                        if (StringUtils.isNumeric(cmd[3]) && !cmd[4].isEmpty()) {
                                            if (groupSetting == null) {
                                                QQGroup group = new QQGroup();
                                                List<String> aliases = new ArrayList<>();
                                                aliases.add(cmd[4]);
                                                group.setGroupAliases(aliases);
                                                group.setGroupID(Long.parseLong(cmd[3]));
                                                mySendPrivateMsg(fromQQ, "Bot > 设置成功!");
                                                saveConf();
                                            } else {
                                                for (QQGroup g : groupSetting) {
                                                    if (g.getGroupID() == Long.parseLong(cmd[3])) {
                                                        List<String> aliases = g.getGroupAliases();
                                                        aliases.add(cmd[4]);
                                                        g.setGroupAliases(aliases);
                                                        mySendPrivateMsg(fromQQ, "Bot > 设置成功!");
                                                        saveConf();
                                                        break;
                                                    }
                                                }
                                            }
                                        } else
                                            mySendPrivateMsg(fromQQ, "Bot > /group aliases [群号] [群别名]");
                                        break;
                                    case "serverinfo":
                                        if (cmd[3].equalsIgnoreCase("del")) {
                                            if (!cmd[4].isEmpty()) {
                                                if (groupSetting != null) {
                                                    for (QQGroup group : groupSetting) {
                                                        if (group.getGroupID() == Long.parseLong(cmd[4])
                                                                || group.getGroupAliases().contains(cmd[4])) {
                                                            group.setServerPort(-1);
                                                            mySendPrivateMsg(fromQQ, "Bot > 删除成功");
                                                            break;
                                                        } else
                                                            mySendPrivateMsg(fromQQ, "Bot > 这个群没有设置服务器信息!");
                                                    }
                                                }
                                            } else
                                                mySendPrivateMsg(fromQQ, "Bot > 请输入正确的群号!");
                                        } else {
                                            if (!cmd[3].isEmpty() && !cmd[4].isEmpty() && StringUtils.isNumeric(cmd[5])) {
                                                if (groupSetting != null) {
                                                    for (QQGroup group : groupSetting) {
                                                        if (group.getGroupID() == Long.parseLong(cmd[3])
                                                                || group.getGroupAliases().contains(cmd[3])) {
                                                            group.setServerIP(cmd[4]);
                                                            group.setServerPort(Integer.parseInt(cmd[5]));
                                                            int length = (msg.length() - (cmd[0].length() + cmd[1].length() + cmd[2].length() + cmd[3].length() + cmd[4].length() + cmd[5].length() + 7));
                                                            group.setInfoMessage(msg.substring(length));
                                                            /**
                                                             * group.setInfoMessage(msg.replace("/", "").replace("#", "")
                                                                    .replace(cmd[0] + " ", "").replace(cmd[1], "")
                                                                    .replace(" " + cmd[2], "").replace(" " + cmd[3], "")
                                                                    .replace(" " + cmd[4], "").replace(" " + cmd[5] + " ", ""));
                                                             */
                                                            mySendPrivateMsg(fromQQ, "Bot > 设置成功!");
                                                        } else {
                                                            List<LookupResult> nodes = BotUtils.getSRVRecords(cmd[4]);
                                                            for (LookupResult node : nodes){
                                                                group.setServerIP(node.host());
                                                                group.setServerPort(node.port());
                                                            }
                                                            int length = (msg.length() - (cmd[0].length() + cmd[1].length() + cmd[2].length() + cmd[3].length() + cmd[4].length() + 5));
                                                            group.setInfoMessage(msg.substring(length));
                                                            mySendPrivateMsg(fromQQ, "Bot > 设置成功!");
                                                            break;
                                                        }
                                                    }
                                                }
                                            } else
                                                mySendPrivateMsg(fromQQ,
                                                        "Bot > /group set serverinfo [群号] [服务器IP] [服务器端口] [自定义消息]\n"
                                                                + "/group set serverinfo [群号] [服务器IP] [自定义消息] (这是SRV解析)\n"
                                                                + "/group set serverinfo del [群号]");
                                        }
                                        break;
                                    case "autoaccept":
                                        if (!cmd[3].equals("") && !cmd[4].equals("")) {
                                            if (groupSetting != null) {
                                                int i = 0, groupSettingSize = groupSetting.size();
                                                while (i < groupSettingSize) {
                                                    QQGroup group = groupSetting.get(i);
                                                    if (group.getGroupID() == Long.parseLong(cmd[3]) || group.getGroupAliases().contains(cmd[3])) {
                                                        if (cmd[4].equalsIgnoreCase("t")) {
                                                            group.setAutoAcceptRequest(true);
                                                            mySendPrivateMsg(fromQQ, "Bot > 设置成功!");
                                                        } else if (cmd[4].equalsIgnoreCase("f")) {
                                                            group.setAutoAcceptRequest(false);
                                                            mySendPrivateMsg(fromQQ, "Bot > 设置成功!");
                                                        } else
                                                            mySendPrivateMsg(fromQQ, "Bot > /group set autoaccept [群号] [t/f]");
                                                    }
                                                    i++;
                                                }
                                            }
                                        } else
                                            mySendPrivateMsg(fromQQ, "Bot > /group set autoaccept [群号] [t/f]");
                                        break;
                                    case "joinmsg":
                                        if (!cmd[3].isEmpty() && !cmd[4].isEmpty()) {
                                            if (groupSetting != null) {
                                                for (QQGroup group : groupSetting) {
                                                    if (group.getGroupID() == Long.parseLong(cmd[3]) || group.getGroupAliases().contains(cmd[3])) {
                                                        group.setJoinMsg(cmd[4]);
                                                        mySendPrivateMsg(fromQQ, "Bot > 已设置 " + group.getGroupID() + " 的加群欢迎信息.");
                                                        break;
                                                    }
                                                }
                                            }
                                        } else
                                            mySendPrivateMsg(fromQQ, "/group set joinmsg [群号] [入群欢迎消息]");
                                        break;
                                    default:
                                        mySendPrivateMsg(fromQQ, "= 群设置帮助 =\n" +
                                                "/group set autoaccept [群号] [t/f]\n" +
                                                "/group set joinmsg [群号] [入群欢迎消息]\n" +
                                                "/group set serverinfo [群号] [服务器IP] [服务器端口] [自定义信息]");
                                        break;
                                }
                                break;
                            case "list":
                                if (groupSetting != null) {
                                    StringBuffer sb = new StringBuffer();
                                    sb.append("所有已设置群设置的群:\n");
                                    for (QQGroup group : groupSetting) {
                                        sb.append(group.getGroupID()).append("\n");
                                    }
                                    mySendPrivateMsg(fromQQ, sb.toString().trim());
                                }
                                break;
                            case "add":
                                if (StringUtils.isNumeric(cmd[2])) {
                                    QQGroup g = new QQGroup();
                                    g.setGroupID(Long.parseLong(cmd[2]));
                                    groupSetting.add(g);
                                    mySendPrivateMsg(fromQQ, "Success");
                                }
                                break;
                            case "del":
                                if (groupSetting != null) {
                                    for (QQGroup g : groupSetting) {
                                        if (g.getGroupID() == Long.parseLong(cmd[2]) || g.getGroupAliases().contains(cmd[2])) {
                                            groupSetting.remove(g);
                                            mySendPrivateMsg(fromQQ, "Success");
                                        }
                                    }
                                }
                                break;
                            default:
                                mySendPrivateMsg(fromQQ, "= Bot 群组管理 =\n" +
                                        " /group list 列出所有已添加的群(需要手动添加!)\n" +
                                        " /group set 设置某个群的设置\n" +
                                        " /group add 新增一个群\n" +
                                        " /group del 删除一个群"
                                );
                                break;
                        }
                        break;
                }
            }
        }
        return MSG_IGNORE;
    }

    public int groupMsg(int subType, int msgId, long fromGroup, long fromQQ, String fromAnonymous, String msg,
            int font) {

        // 机器人功能处理
        if (msg.startsWith("/") || msg.startsWith("#")) {
            // 解析是否为管理员
            boolean isGroupAdmin = CQ.getGroupMemberInfoV2(fromGroup, fromQQ).getAuthority() > 1;
            boolean isBotAdmin = adminIds.toString().contains(fromQQ + "");
            boolean isOwner = fromQQ == ownerQQ;

            // process only after there's a command, in order to get rid of memory trash
            String temp = msg.trim();
            String[] cmd = {"", "", "", "", "", ""};

            /**
             * @brief Processing msg into cmd & params
             * @param cmd[0] << cmd after !, e.g. "help" cmd[1] << first param etc.
             * @author Stiven.ding
             */

            for (int i = 0; i < 6; i++) {
                temp = temp.trim();
                if (temp.indexOf(' ') > 0) {
                    cmd[i] = temp.substring(0, temp.indexOf(' '));
                    temp = temp.substring(temp.indexOf(' ') + 1);
                } else {
                    cmd[i] = temp.trim();
                    break;
                }
            }
            cmd[0] = cmd[0].substring(1); // del command prefix at the beginning of cmd

            /**
             * @brief Run commands here
             * @author Stiven.ding
             */

            if (botStatus){
                switch (cmd[0].toLowerCase()) {
                    // 帮助命令
                    case "help":
                        if (isBotAdmin || isGroupAdmin || isOwner) {
                            switch (cmd[1].toLowerCase()) {
                                default:
                                case "1":
                                    mySendGroupMsg(fromGroup,
                                            "= 无名Bot " + VerClass.VERSION + " =" + "\n /qd 签到" + "\n /info 查看签到积分"
                                                    + "\n /sub (媒体) 订阅指定媒体" + "\n /unsub [媒体] 退订指定媒体"
                                                    + "\n /switch [on/off] 开/关机器人" + "\n /mute [@/QQ] (dhm) 禁言(默认10m)"
                                                    + "\n========== 1/3 ==========");
                                    break;
                                case "2":
                                    mySendGroupMsg(fromGroup,
                                            "= 无名Bot " + VerClass.VERSION + " =" + "\n /mute all 全群禁言"
                                                    + "\n /unmute [@/QQ] 解禁某人" + "\n /unmute all 解除全群禁言"
                                                    + "\n /kick [@/QQ] (是否永封(t/f)) 踢出群成员" + "\n /admin 管理机器人的管理员"
                                                    + "\n /rcon [命令] 执行 Minecraft 服务器命令" + "\n========== 2/3 ==========");
                                    break;
                                case "3":
                                    mySendGroupMsg(fromGroup, "= 无名Bot " + VerClass.VERSION + " =" + "\n /shop 积分商店"
                                            + "\n========== 3/3 ==========");
                                    break;
                            }
                            break;
                        } else {
                            switch (cmd[1].toLowerCase()) {
                                default:
                                case "1":
                                    mySendGroupMsg(fromGroup, "= 无名Bot " + VerClass.VERSION + " =" + "\n /qd 签到"
                                            + "\n /info 查看签到积分" + "\n /shop 积分商店" + "\n========== 1/1 ==========");
                                    break;
                            }
                        }
                        // 关闭命令
                    case "switch":
                        if (isBotAdmin || isOwner) {
                            if (cmd[1].equals("off")) {
                                mySendGroupMsg(fromGroup, "Bot > 已将机器人禁言.");
                                botStatus = false;
                            } else
                                mySendGroupMsg(fromGroup, "Bot > 机器人早已处于开启状态.");
                        } else
                            mySendGroupMsg(fromGroup, "Bot > 你没有权限!");
                        break;
                    // 订阅命令
                    case "sub":
                        if (isBotAdmin || isOwner) {
                            switch (cmd[1].toLowerCase()) {
                                case "":
                                    mySendGroupMsg(fromGroup, "媒体列表: /sub (媒体)" + "\n [SDT] Solidot 奇客资讯"
                                            + "\n [JWU] 一觉醒来世界发生了什么" + "\n [TOH] 历史上的今天");
                                    break;
                                case "sdt":
                                    solidot.enable();
                                    mySendGroupMsg(fromGroup, "Bot > 已订阅 Solidot.");
                                    break;
                                case "jwu":
                                    jikeWakeUp.enable();
                                    mySendGroupMsg(fromGroup, "Bot > 已订阅 即刻 - 一觉醒来世界发生了什么.");
                                    break;
                                case "toh":
                                    todayOnHistory.enable();
                                    mySendGroupMsg(fromGroup, "Bot > 已订阅 历史上的今天.");
                                    break;
                                default:
                                    mySendGroupMsg(fromGroup, "Bot > 未知频道.");
                                    break;
                            }
                        } else
                            mySendGroupMsg(fromGroup, "Bot > 你没有权限!");
                        break;
                    // 退订命令
                    case "unsub":
                        if (isBotAdmin || isOwner) {
                            switch (cmd[1].toLowerCase()) {
                                case "sdt":
                                    solidot.disable();
                                    mySendGroupMsg(fromGroup, "Bot > 已退订 Solidot.");
                                    break;
                                case "jwu":
                                    jikeWakeUp.disable();
                                    mySendGroupMsg(fromGroup, "Bot > 已退订 即刻 - 一觉醒来世界发生了什么.");
                                    break;
                                case "toh":
                                    todayOnHistory.disable();
                                    mySendGroupMsg(fromGroup, "Bot > 已退订 历史上的今天.");
                                    break;
                                default:
                                    mySendGroupMsg(fromGroup, "Bot > 未知频道. 输入 /sub 查看所有媒体.");
                                    break;
                            }
                        } else
                            mySendGroupMsg(fromGroup, "Bot > 你没有权限!");
                        break;
                    // 禁言
                    case "mute":
                        if (isGroupAdmin) {
                            if (cmd[1].equals("")) {
                                mySendGroupMsg(fromGroup, "Bot > 用法: /mute [@/QQ号] [时间(秒)]");
                            } else if (cmd[1].equals("all")) {
                                CQ.setGroupWholeBan(fromGroup, true);
                                mySendGroupMsg(fromGroup, "Bot > 已打开全群禁言.");
                            } else {
                                try {
                                    long banQQ = StringUtils.isNumeric(cmd[1]) ? Integer.parseInt(cmd[1]) : CC.getAt(cmd[1]);
                                    long banTime = 0; // 此处单位为秒
                                    if (cmd[2].equals(""))
                                        banTime = 10 * 60;
                                    else {
                                        String tempTime = cmd[2];
                                        if (tempTime.indexOf('d') != -1) {
                                            banTime += Integer.parseInt(tempTime.substring(0, tempTime.indexOf('d'))) * 24
                                                    * 60 * 60;
                                            tempTime = tempTime.substring(tempTime.indexOf('d') + 1);
                                        }
                                        if (tempTime.indexOf('h') != -1) {
                                            banTime += Integer.parseInt(tempTime.substring(0, tempTime.indexOf('h'))) * 60
                                                    * 60;
                                            tempTime = tempTime.substring(tempTime.indexOf('h') + 1);
                                        }
                                        if (tempTime.indexOf('m') != -1)
                                            banTime += Integer.parseInt(tempTime.substring(0, tempTime.indexOf('m'))) * 60;
                                    }
                                    if (banTime < 1)
                                        throw new NumberFormatException("Equal or less than 0");
                                    if (banTime <= 30 * 24 * 60 * 60) {
                                        CQ.setGroupBan(fromGroup, banQQ, banTime);
                                        mySendGroupMsg(fromGroup, "Bot > 已禁言 " + CQ.getStrangerInfo(banQQ).getNick() + "(" + banQQ + ") " + banTime / 60 + "分钟.");
                                    } else
                                        mySendGroupMsg(fromGroup, "Bot > 时间长度太大了！");
                                } catch (Exception e) {
                                    mySendGroupMsg(fromGroup, "Bot > 命令格式有误! 用法: /mute [@/QQ号] [dhm]");
                                }
                            }
                        } else
                            mySendGroupMsg(fromGroup, "Bot > 你没有权限!");
                        break;
                    // 解除禁言
                    case "unmute":
                        if (isGroupAdmin) {
                            if (cmd[1].equals("")) {
                                mySendGroupMsg(fromGroup, "Bot > 用法: /unmute [@]");
                            } else if (cmd[1].equals("all")) {
                                CQ.setGroupWholeBan(fromGroup, false);
                                mySendGroupMsg(fromGroup, "Bot > 已关闭全群禁言.");
                            } else {
                                try {
                                    long banQQ = StringUtils.isNumeric(cmd[1]) ? Integer.parseInt(cmd[1])
                                            : CC.getAt(cmd[1]);
                                    CQ.setGroupBan(fromGroup, banQQ, 0);
                                } catch (Exception e) {
                                    mySendGroupMsg(fromGroup, "Bot > 请检查你输入的命令!");
                                }
                            }
                        } else
                            mySendGroupMsg(fromGroup, "Bot > 你没有权限!");
                        break;
                    // 调试
                    case "debug":
                        if (isBotAdmin || isOwner) {
                            switch (cmd[1].toLowerCase()) {
                                case "rss": // 代码中务必只用小写，确保大小写不敏感
                                    mySendGroupMsg(fromGroup, new RssItem(cmd[2]).getContext());
                                    break;
                                case "wel":
                                    long parseQQ = StringUtils.isNumeric(cmd[2]) ? Integer.parseInt(cmd[2]) : CC.getAt(cmd[2]);
                                    groupMemberIncrease(subType, 100, fromGroup, fromQQ, parseQQ);
                                    break;
                                case "checkin":
                                        switch (cmd[2].toLowerCase()) {
                                            case "set":
                                                try {
                                                    long userQQ = StringUtils.isNumeric(cmd[3]) ? Integer.parseInt(cmd[3]) : CC.getAt(cmd[3]);
                                                    BotUser user = checkinUsers.get(userQQ);
                                                    double point = Double.parseDouble(cmd[4]);
                                                    user.setCheckInPoint(point);
                                                    mySendGroupMsg(fromGroup, "Bot > 已设置 " + CQ.getStrangerInfo(userQQ).getNick() + " 的积分为 " + point);
                                                } catch (Exception ignored) {
                                                    mySendGroupMsg(fromGroup, "Bot > 请检查你输入的命令!");
                                                }
                                                break;
                                            case "reset":
                                                try {
                                                    long userQQ1 = StringUtils.isNumeric(cmd[3]) ? Integer.parseInt(cmd[3]) : CC.getAt(cmd[3]);
                                                    checkinUsers.remove(userQQ1);
                                                    mySendGroupMsg(fromGroup, "Bot > 已重置 " + CQ.getStrangerInfo(userQQ1).getNick() + " 的签到账号");
                                                    break;
                                                } catch (Exception ignored) {
                                                    mySendGroupMsg(fromGroup, "Bot > 请检查你输入的命令!");
                                                }
                                                break;
                                    }
                                    break;
                                default:
                                    mySendGroupMsg(fromGroup,
                                            "Version: " + VerClass.VERSION + "\nDebug Menu:"
                                                    + "\n rss [URL] - Get context manually"
                                                    + "\n toh - Get todayOnHistory" + "\n wel [#/@] - Manually welcome"
                                                    + "\n checkin set/reset [@/QQ]"
                                    );
                                    break;
                            }
                        } else
                            mySendGroupMsg(fromGroup, "Bot > 你没有权限!");
                        break;
                    case "kick":
                        if (isGroupAdmin) {
                            long kickQQ = StringUtils.isNumeric(cmd[1]) ? Integer.parseInt(cmd[1]) : CC.getAt(cmd[1]);
                            if (cmd[2].equals("")) {
                                if ("".equals(cmd[1])) {
                                    mySendGroupMsg(fromGroup, "Bot > 用法: /kick [@/QQ号] (是否永封(t/f))");
                                } else {
                                    CQ.setGroupKick(fromGroup, kickQQ, false);
                                    mySendGroupMsg(fromGroup, "Bot > 已踢出 " + CC.at(kickQQ));
                                }
                            } else
                                switch (cmd[2]) {
                                    case "t":
                                        CQ.setGroupKick(fromGroup, kickQQ, true);
                                        mySendGroupMsg(fromGroup, "Bot > 已踢出 " + CC.at(kickQQ));
                                        break;
                                    case "f":
                                        CQ.setGroupKick(fromGroup, kickQQ, false);
                                        mySendGroupMsg(fromGroup, "Bot > 已踢出 " + CC.at(kickQQ));
                                        break;
                                    default:
                                        break;
                                }
                        } else
                            mySendGroupMsg(fromGroup, "Bot > 你没有权限!");
                        break;
                    case "rcon":
                        if (isRcon) {
                            if (isBotAdmin || isOwner) {
                                if (!cmd[1].equals("")) {
                                    try {
                                        String command = msg.replaceAll("/" + cmd[0] + " ", "");
                                        CQ.logInfo("RCON", fromQQ + " 尝试执行服务器命令: " + command);
                                        Rcon rcon = new Rcon(rconIP, rconPort, rconPwd.getBytes());
                                        String result = rcon.command(command);
                                        mySendGroupMsg(fromGroup, "Bot > \n" + result.trim());
                                    } catch (IOException e) {
                                        mySendGroupMsg(fromGroup, "Bot > 连接至服务器发生了错误");
                                    } catch (AuthenticationException ae){
                                        mySendGroupMsg(fromGroup, "Bot > RCon 密码错误!");
                                    }
                                } else
                                    mySendGroupMsg(fromGroup, "Bot > 请输入需要执行的命令! (不需要带\"/\")");
                            } else
                                mySendGroupMsg(fromGroup, "Bot > 你没有权限!");
                        } else
                            mySendGroupMsg(fromGroup, "Bot > 很抱歉, 机器人没有启用 RCON 功能.");
                        break;
                    case "admin":
                        if (isOwner || isBotAdmin) {
                            switch (cmd[1]) {
                                case "list":
                                    mySendGroupMsg(fromGroup, "Bot > 机器人管理员列表: " + adminIds.toString());
                                    break;
                                case "add":
                                    if (isOwner) {
                                        if (!cmd[2].equals("")) {
                                            try {
                                                long adminQQ = StringUtils.isNumeric(cmd[2]) ? Integer.parseInt(cmd[2]) : CC.getAt(cmd[2]);
                                                adminIds.add(adminQQ);
                                                mySendGroupMsg(fromGroup, "Bot > 已成功添加管理员 " + CQ.getStrangerInfo(adminQQ).getNick() + "(" + adminQQ + ")");
                                            } catch (Exception e) {
                                                mySendGroupMsg(fromGroup, "Bot > 请检查你输入的命令!");
                                            }
                                        }
                                    }
                                    break;
                                case "del":
                                    if (isOwner) {
                                        if (!cmd[2].equals("")) {
                                            try {
                                                long adminQQ = StringUtils.isNumeric(cmd[2]) ? Integer.parseInt(cmd[2]) : CC.getAt(cmd[2]);
                                                adminIds.remove(adminQQ);
                                                mySendGroupMsg(fromGroup, "Bot > 已成功移除管理员 " + CQ.getStrangerInfo(adminQQ).getNick() + "(" + adminQQ + ")");
                                            } catch (Exception x) {
                                                mySendGroupMsg(fromGroup, "Bot > 请检查你输入的命令!");
                                            }
                                        }
                                    }
                                    break;
                                case "reload":
                                    readConf();
                                    mySendGroupMsg(fromGroup, "Bot > 配置已重新载入.");
                                    break;
                                case "save":
                                    saveConf();
                                    mySendGroupMsg(fromGroup, "Bot > 配置已保存.");
                                    break;
                                case "parse":
                                    mySendGroupMsg(fromGroup, saveConf());
                                    break;
                                default:
                                    mySendGroupMsg(fromGroup, "= Bot 管理员控制面板 =\n" +
                                            " /admin list 列出所有无名 Bot 的管理员\n" +
                                            " /admin add [@/QQ] 添加管理员\n" +
                                            " /admin del [@/QQ] 移除管理员\n" +
                                            " /admin reload 重载配置\n" +
                                            " /admin save 保存配置\n" +
                                            " /admin parse"
                                    );
                                    break;
                            }
                        } else
                            mySendGroupMsg(fromGroup, "Bot > 你没有权限!");
                        break;
                    case "qd":
                    case "签到":
                    case "checkin":
                        if (!checkinUsers.containsKey(fromQQ) && cmd[1].equals("")) {
                            mySendGroupMsg(fromGroup, "Bot > 你还没有注册无名 Bot 账号! 第一次请使用 /qd <游戏ID> 注册～");
                        } else {
                            if (!checkinUsers.containsKey(fromQQ)) {
                                BotUser newUser = new BotUser();
                                newUser.setUserQQ(fromQQ);
                                newUser.setBindServerAccount(cmd[1]);
                                checkinUsers.put(fromQQ, newUser);
                                mySendGroupMsg(fromGroup, "Bot > 已绑定账号 " + cmd[1] + " ，以后可以直接输入 /qd 签到了! ");
                            }
                            if (BotUtils.isCheckInReset(new Date(), checkinUsers.get(fromQQ).getLastCheckInTime()) || checkinUsers.get(fromQQ).getCheckInTime() == 0) {
                                BotUser user = checkinUsers.get(fromQQ);
                                // 计算连续签到次数，此处用了 Date 这个废弃的类，应换为 Calendar，too lazy to do so.
                                if (user.getLastCheckInTime().getMonth() == new Date().getMonth()
                                        && user.getLastCheckInTime().getDate() == new Date().getDate() - 1)
                                    user.setCheckInTime(user.getCheckInTime() + 1);
                                else
                                    user.setCheckInTime(1);
                                user.setLastCheckInTime(new Date());

                                // 只取小数点后一位，将最大 awardPoint 限制到 3 倍
                                double awardProp = 0.15 * (user.getCheckInTime() - 1); // refer to issue #40
                                int basePoint = new Random(Calendar.getInstance().getTimeInMillis()).nextInt(10);
                                double awardPoint = awardProp < 3 ? Double.parseDouble(String.format("%.1f", awardProp * basePoint)) : 3 * basePoint;

                                user.setCheckInPoint(checkinUsers.get(fromQQ).getCheckInPoint() + basePoint + awardPoint);

                                if (basePoint + awardPoint == 0.0) {
                                    mySendGroupMsg(fromGroup, "Bot > 签到成功!\n" + "今天运气不佳, 没有积分");
                                } else
                                    mySendGroupMsg(fromGroup, "Bot > Hi " + checkinUsers.get(fromQQ).getBindServerAccount()
                                            + ", 签到成功!\n" + "本次签到获得 " + basePoint + " 点积分. \n" + "今天是第 "
                                            + user.getCheckInTime() + " 天连签了, 额外获得 " + awardPoint + " 奖励分~\n截至今天您的账户余额共 "
                                            + String.format("%.1f", checkinUsers.get(fromQQ).getCheckInPoint()) + "分.");

                                saveConf();
                            } else
                                mySendGroupMsg(fromGroup, "Bot > 你今天已经签到过了! 输入 /cx 可查询签到信息");
                        }
                        break;
                    case "cx":
                    case "查询":
                    case "info":
                        if (checkinUsers.containsKey(fromQQ)) {
                            mySendGroupMsg(fromGroup,
                                    CC.at(fromQQ) + "\n积分: "
                                            + String.format("%.1f", checkinUsers.get(fromQQ).getCheckInPoint())
                                            + "  \n累计连续签到了 " + checkinUsers.get(fromQQ).getCheckInTime() + " 天\n上次签到于: "
                                            + new SimpleDateFormat("yyyy-MM-dd")
                                            .format(checkinUsers.get(fromQQ).getLastCheckInTime())
                                            + "\n绑定的游戏账号是: " + checkinUsers.get(fromQQ).getBindServerAccount());
                        } else
                            mySendGroupMsg(fromGroup, "Bot > 你还没有签到过哦");
                        break;
                    case "clearscreen":
                    case "cs":
                    case "qp":
                    case "清屏":
                        if (isBotAdmin || isOwner) {
                            IntStream.range(0, 20).forEach(i -> mySendGroupMsg(fromGroup, " "));
                            mySendGroupMsg(fromGroup, "Bot > 清屏完成.");
                        }
                        break;
                    case "serverinfo":
                        if (!cmd[1].equals("") && StringUtils.isNumeric(cmd[2])) {
                            mySendGroupMsg(fromGroup, BotUtils.getServerInfo(cmd[1], Integer.parseInt(cmd[2])));
                        } else if (!cmd[1].equals("")){
                            List<LookupResult> nodes = BotUtils.getSRVRecords("_minecraft._tcp." + cmd[1]);
                            for (LookupResult node : nodes){
                                mySendGroupMsg(fromGroup, BotUtils.getServerInfo(BotUtils.StringHelper(node.host()), node.port()));
                            }
                        } else
                            mySendGroupMsg(fromGroup, "Bot > Please check IP address or Port.");
                        break;
                    case "shop":
                        if ("buy".equals(cmd[1].toLowerCase())) {
                                if (!cmd[2].isEmpty()) {
                                    if (shopItems.containsKey(cmd[2])) {
                                        Shop shop = shopItems.get(cmd[2]);
                                        double point = checkinUsers.get(fromQQ).getCheckInPoint();
                                        if (point > shop.getItemPoint()) {
                                            String playerName = checkinUsers.get(fromQQ).getBindServerAccount();
                                            if (playerName != null) {
                                                if (shop.getBuyTime() != 0) {
                                                    try {
                                                        if (shop.getItemCommand().startsWith("give")) {
                                                            Rcon rcon = new Rcon(rconIP, rconPort, rconPwd.getBytes());
                                                            String status = rcon.command("seen " + playerName);
                                                            if (!status.contains("离线")) {
                                                                String s = rcon.command(shop.getItemCommand().replaceAll("%p", playerName));
                                                                if (!isOwner || !isBotAdmin)
                                                                    mySendGroupMsg(fromGroup, s.replaceAll(s, "Bot > 购买成功"));
                                                                else
                                                                    mySendGroupMsg(fromGroup, "Bot > 购买成功\nDebug Info: " + s);
                                                                checkinUsers.get(fromQQ).setCheckInPoint(checkinUsers.get(fromQQ).getCheckInPoint() - shop.getItemPoint());
                                                                if (shop.getBuyTime() > 0) {
                                                                    shop.setBuyTime(shop.getBuyTime() - 1);
                                                                }
                                                            } else
                                                                mySendGroupMsg(fromGroup, "Bot > 这个商品需要你上线才能领取!");
                                                        } else {
                                                            Rcon rcon = new Rcon(rconIP, rconPort, rconPwd.getBytes());
                                                            String s = rcon.command(shop.getItemCommand().replaceAll("%p", playerName)).trim();
                                                            if (!isOwner || !isBotAdmin)
                                                                mySendGroupMsg(fromGroup, s.replaceAll(s, "Bot > 购买成功"));
                                                            else
                                                                mySendGroupMsg(fromGroup, "Bot > 购买成功\nDebug Info: " + s + "\n命令: " + shop.getItemCommand().replaceAll("%p", playerName));
                                                            checkinUsers.get(fromQQ).setCheckInPoint(checkinUsers.get(fromQQ).getCheckInPoint() - shop.getItemPoint());
                                                            if (shop.getBuyTime() > 0) {
                                                                shop.setBuyTime(shop.getBuyTime() - 1);
                                                            }
                                                        }
                                                    } catch (IOException e) {
                                                        mySendGroupMsg(fromGroup, "Bot > 连接至服务器发生了错误");
                                                        e.printStackTrace();
                                                    } catch (AuthenticationException ae) {
                                                        mySendPrivateMsg(ownerQQ, "RCON 密码错误!");
                                                    }
                                                } else
                                                    mySendGroupMsg(fromGroup, "Bot > 抱歉, 本商品已售罄!");
                                            } else
                                                mySendGroupMsg(fromGroup, "Bot > 请先绑定服务器账号. /qd <游戏ID>");
                                        } else
                                            mySendGroupMsg(fromGroup, "Bot > 你的积分不足.");
                                    }
                                } else
                                    mySendGroupMsg(fromGroup, "Bot > /shop buy [商品名]");
                        } else {
                            if (shopItems != null) {
                                StringBuilder sb = new StringBuilder();
                                sb.append("Bot > 欢迎来到积分商店!\n").append("目前所有在售的物品:\n");
                                for (Map.Entry<String, Shop> entry : shopItems.entrySet()) {
                                    if (entry.getValue().getBuyTime() != 0)
                                        if (entry.getValue().getMoney() > 0){
                                            sb.append(entry.getValue().getItemName()).append("(内含时光币 ").append(entry.getValue().getMoney()).append(") 需要积分").append(entry.getValue().getItemPoint()).append("\n");
                                        } else
                                            sb.append(entry.getValue().getItemName()).append(" 需要积分").append(entry.getValue().getItemPoint()).append("\n");
                                }
                                sb.append("使用 /shop buy [商品名] 购买指定商品\n例如: /shop buy 一小袋钱");
                                mySendGroupMsg(fromGroup, sb.toString().trim());
                            }
                        }
                        break;
                    case "rebind":
                        if (isOwner || isBotAdmin) {
                            if (!cmd[1].isEmpty()) {
                                if (checkinUsers.containsKey(fromQQ)) {
                                        checkinUsers.get(fromQQ).setBindServerAccount(cmd[1]);
                                        mySendGroupMsg(fromGroup, "Bot > 绑定账号 " + cmd[1] + " 成功!");
                                } else
                                    mySendGroupMsg(fromGroup, "Bot > 你还没有注册无名 Bot 账号! 请先使用 /qd 签到一下吧~ (签到时会自动注册)");
                            } else if (!cmd[1].equals("") && !cmd[2].equals("")) {
                                long qq = StringUtils.isNumeric(cmd[1]) ? Integer.parseInt(cmd[1]) : CC.getAt(cmd[1]);
                                if (checkinUsers.containsKey(qq)) {
                                    checkinUsers.get(qq).setBindServerAccount(cmd[2]);
                                } else
                                    mySendGroupMsg(fromGroup, "Bot > TA还没有注册过无名 Bot 账号");
                            }
                        }
                        break;
                }
            } else if (cmd[0].equals("switch") && cmd[1].equals("on")) {
                // 机器人禁言关闭
                if (isBotAdmin || isOwner) {
                    mySendGroupMsg(fromGroup, "Bot > 已启用机器人.");
                    botStatus = true;
                }
            }
        } else if (msg.equals("服务器信息") || msg.equals("服务器状态")) {
            if (botStatus) {
                for (QQGroup group : groupSetting) {
                    if (group.getGroupID() == fromGroup) {
                        mySendGroupMsg(fromGroup, BotUtils.getCustomServerInfo(group.getServerIP(),
                                group.getServerPort(), group.getInfoMessage()));
                        try {
                            CQ.logInfo("RCON", fromQQ + " 尝试执行服务器命令: " + "list");
                            Rcon rcon = new Rcon(rconIP, rconPort, rconPwd.getBytes());
                            String result = rcon.command("list");
                            mySendGroupMsg(fromGroup, "Bot > \n" + result.trim());
                        } catch (Exception ignored) {
                        }
                        break;
                    }
                }
            }
        }
        return MSG_IGNORE;
    }

    /**
     * @brief Startup & schedule
     * @return always 0
     */

    public int enable() {
        enable = true;

        readConf();

        if (!UpdateChecker.isLatest()){
            CQ.logInfo("Updater", "Nameless Bot 有新版本: " + UpdateChecker.getLatestVer());
            mySendPrivateMsg(ownerQQ, "Nameless Bot 有新版本: " + UpdateChecker.getLatestVer());
        }

        /**
         * @brief 启动时计划定时推送 & save configuration
         * @author Starwish.sama
         */

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 7);
        c.set(Calendar.MINUTE, 5);

        Timer soliDotPusher = new Timer();
        soliDotPusher.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (Calendar.getInstance().get(Calendar.MINUTE) == 30)
                    solidotPusher();
            }
        }, c.getTime(), 1000 * 60 * 5);

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                if (Calendar.getInstance().get(Calendar.MINUTE) == 30)
                    saveConf(); // 每小时保存一次

                // todayOnHistory @ 7:00 AM
                if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) == 7)
                    if (todayOnHistory.getStatus() && botStatus) {
                        String text = todayOnHistory.getContext();
                        mySendGroupMsg(111852382L,
                                CC.face(74) + "各位时光隧道玩家早上好" + "\n今天是" + Calendar.getInstance().get(Calendar.YEAR) + "年"
                                        + (Calendar.getInstance().get(Calendar.MONTH) + 1) + "月"
                                        + Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + "日" + "，"
                                        + text.substring(0, text.indexOf("\n")).replaceFirst("-", "的今天是")
                                        + "的日子，一小时之后我会推送今天的早间新闻\n新的一天开始了！" + CC.face(190) + "今天别忘了去服务器领取签到奖励噢~~");
                    }

                // jikeWakeUp @ 7:15 AM
                if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) == 8)
                    if (jikeWakeUp.getStatus() && botStatus)
                        mySendGroupMsg(111852382L, jikeWakeUp.getContext().replaceAll("\uD83D\uDC49", CC.emoji(128073) ) + "\n即刻推送 - NamelessBot");

            }
        }, c.getTime(), 1000 * 60 * 60);

        return 0;
    }

    public int discussMsg(int subtype, int msgId, long fromDiscuss, long fromQQ, String msg, int font) {
        return MSG_IGNORE;
    }

    public int groupUpload(int subType, int sendTime, long fromGroup, long fromQQ, String file) {
        return MSG_IGNORE;
    }

    public int groupAdmin(int subtype, int sendTime, long fromGroup, long beingOperateQQ) {
        return MSG_IGNORE;
    }

    public void readConf() {
        try {
            JSONObject statusObject = JSONObject.parseObject(FileProcess.readFile(statusPath));
            botStatus = statusObject.getBooleanValue("botStatus");
            solidot.setStatus(statusObject.getBooleanValue("solidot"));
            jikeWakeUp.setStatus(statusObject.getBooleanValue("jikeWakeUp"));
            todayOnHistory.setStatus(statusObject.getBooleanValue("todayOnHistory"));

            JSONObject rconObject = JSONObject.parseObject(FileProcess.readFile(rconPath));
            rconIP = rconObject.getString("rconIP");
            rconPort = rconObject.getIntValue("rconPort");
            rconPwd = rconObject.getString("rconPwd");
            isRcon = rconObject.getBooleanValue("RconFunction");

            JSONObject adminsObject = JSONObject.parseObject(FileProcess.readFile(CQ.getAppDirectory() + "admins.json"));
            ownerQQ = adminsObject.getLong("owner");
            adminIds = JSON.parseObject(adminsObject.getString("admins"), new TypeReference<List<Long>>(){});

            JSONObject groupsObject = JSONObject.parseObject(FileProcess.readFile(groupsPath));
            if (JSON.parseObject(groupsObject.getString("groupSetting"), new TypeReference<List<QQGroup>>(){}) != null)
                groupSetting = JSON.parseObject(groupsObject.getString("groupSetting"), new TypeReference<List<QQGroup>>(){});

            JSONObject settingObject = JSONObject.parseObject(FileProcess.readFile(CQ.getAppDirectory() + "botsettings.json"));
            filterWords = JSON.parseObject(settingObject.getString("triggerWords"), new TypeReference<List<String>>(){});

            JSONObject checkInObject = JSONObject.parseObject(FileProcess.readFile(CQ.getAppDirectory() + "qiandao.json"));
            checkinUsers = JSON.parseObject(checkInObject.getString("checkinUsers"), new TypeReference<Map<Long, BotUser>>(){});
            shopItems = JSON.parseObject(checkInObject.getString("shopItems"), new TypeReference<Map<String, Shop>>() {});

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String saveConf() {
        // status config
        JSONObject statusObject = new JSONObject();
        statusObject.put("botStatus", botStatus);
        statusObject.put("solidot", solidot.getStatus());
        statusObject.put("jikeWakeUp", jikeWakeUp.getStatus());
        statusObject.put("todayOnHistory", todayOnHistory.getStatus());
        FileProcess.createFile(statusPath, statusObject.toJSONString());

        // Rcon配置json
        JSONObject rconObject = new JSONObject();
        rconObject.put("rconIP", rconIP);
        rconObject.put("rconPort", rconPort);
        rconObject.put("rconPwd", rconPwd);
        rconObject.put("RconFunction", isRcon);
        FileProcess.createFile(rconPath, rconObject.toJSONString());

        //机器人管理员json
        JSONObject adminsObject = new JSONObject();
        adminsObject.put("admins", adminIds);
        adminsObject.put("owner", ownerQQ);
        FileProcess.createFile(CQ.getAppDirectory() + "admins.json", adminsObject.toJSONString());

        //群设置json
        JSONObject groupSettingObject = new JSONObject();
        groupSettingObject.put("groupSetting", groupSetting);
        FileProcess.createFile(groupsPath, groupSettingObject.toJSONString());

        JSONObject settingObject = new JSONObject();
        settingObject.put("triggerWords", filterWords);
        FileProcess.createFile(CQ.getAppDirectory() + "botsettings.json", settingObject.toJSONString());

        JSONObject checkInObject = new JSONObject();
        checkInObject.put("checkinUsers", checkinUsers);
        checkInObject.put("shopItems", shopItems);
        FileProcess.createFile(CQ.getAppDirectory() + "qiandao.json", checkInObject.toJSONString());

        CQ.logDebug("JSON", "配置已保存.");

        return "status.json:\n" + statusObject.toJSONString();
    }

    public int groupMemberDecrease(int subtype, int sendTime, long fromGroup, long fromQQ, long beingOperateQQ) {
        return MSG_IGNORE;
    }

    public int groupMemberIncrease(int subtype, int sendTime, long fromGroup, long fromQQ, long beingOperateQQ) {
        // 入群欢迎
        if (botStatus) {
            for (QQGroup group : groupSetting){
                if (!group.getJoinMsg().isEmpty() && group.getGroupID() == fromGroup){
                    mySendGroupMsg(fromGroup, group.getJoinMsg());
                    break;
                }
            }
        }
        return MSG_IGNORE;
    }

    public int friendAdd(int subtype, int sendTime, long fromQQ) {
        CQ.sendPrivateMsg(ownerQQ, CQ.getStrangerInfo(fromQQ).getNick() + "(" + fromQQ + ") " + "向机器人发送了好友请求");
        return MSG_IGNORE;
    }

    public int requestAddFriend(int subtype, int sendTime, long fromQQ, String msg, String responseFlag) {
        return MSG_IGNORE;
    }

    public int requestAddGroup(int subtype, int sendTime, long fromGroup, long fromQQ, String msg,
            String responseFlag) {
        if (botStatus){
            if (groupSetting != null) {
                for (QQGroup g : groupSetting) {
                    if (g.isAutoAcceptRequest() && subtype == 1) {
                        CQ.setGroupAddRequest(responseFlag, REQUEST_GROUP_ADD, REQUEST_ADOPT, "无名Bot自动放行");
                        mySendGroupMsg(fromGroup, "Bot > 已自动接受来自 " + CQ.getStrangerInfo(fromQQ).getNick() + "(" + fromQQ + ") 的入群申请.");
                        break;
                    } else if (fromQQ == ownerQQ && subtype == 2) {
                        CQ.setGroupAddRequest(responseFlag, REQUEST_GROUP_INVITE, REQUEST_ADOPT, "");
                        break;
                    }
                }
            }
        }
        return MSG_IGNORE;
    }

    public int exit() {
        return 0;
    }

    public int disable() {
        saveConf();
        enable = false;
        return 0;
    }

    public String appInfo() {
        String AppID = "top.starwish.namelessbot";
        return CQAPIVER + "," + AppID;
    }

    public void mySendGroupMsg(long groupId, String msg) {
        if (!filterWords.contains(msg))
            CQ.sendGroupMsg(groupId, msg);
    }

    public void mySendPrivateMsg(long fromQQ, String msg){
        if (!filterWords.contains(msg))
            CQ.sendPrivateMsg(fromQQ, msg);
    }

    private void solidotPusher() {
        if (botStatus && solidot.getStatus()) {
            String tempPath = CQ.getAppDirectory() + "solidottemp.txt";
            String tempTitle = "";
            try {
                tempTitle = FileProcess.readFile(tempPath);
            } catch (IOException e) {
                FileProcess.createFile(tempPath, solidot.getTitle());
                e.printStackTrace();
            }
            File solidotTemp = new File(tempPath);
            if (!solidotTemp.exists()) {
                FileProcess.createFile(tempPath, solidot.getTitle());
            } else {
                String title = solidot.getTitle();
                if (!tempTitle.equals("") && !tempTitle.equals(title) && !tempTitle.equals("Encountered a wrong URL or a network error.") && !title.equals("Encountered a wrong URL or a network error.")) {
                    String context = solidot.getContext() + "\nSolidot 推送\nPowered by NamelessBot";
                    mySendGroupMsg(111852382L, context);
                    FileProcess.createFile(tempPath, solidot.getTitle());
                }
            }
        }
    }
}
