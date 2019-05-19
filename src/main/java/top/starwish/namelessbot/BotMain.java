package top.starwish.namelessbot;

import com.alibaba.fastjson.serializer.SerializerFeature;

import com.sobte.cqp.jcq.entity.*;
import com.sobte.cqp.jcq.event.JcqAppAbstract;

import net.kronos.rkon.core.Rcon;
import net.kronos.rkon.core.ex.AuthenticationException;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import com.alibaba.fastjson.*;

import top.starwish.namelessbot.entity.CheckIn;
import top.starwish.namelessbot.entity.MCServer;
import top.starwish.namelessbot.entity.RssItem;
import top.starwish.namelessbot.utils.RSAUtils;
import top.starwish.namelessbot.utils.BotUtils;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.SimpleDateFormat;
import java.util.*;

public class BotMain extends JcqAppAbstract implements ICQVer, IMsg, IRequest {
    String statusPath = CQ.getAppDirectory() + "status.json";
    String groupsPath = CQ.getAppDirectory() + "groupsettings.json";
    String rconPath = CQ.getAppDirectory() + "rcon.json";

    boolean botStatus = true;
    boolean debugMode = false;

    String rconIP = null;
    int rconPort = 0;
    String rconPwd = null;
    boolean isRcon = true;

    RssItem solidot = new RssItem("https://www.solidot.org/index.rss");
    RssItem jikeWakeUp = new RssItem("https://rsshub.app/jike/topic/text/553870e8e4b0cafb0a1bef68");
    RssItem todayOnHistory = new RssItem("http://api.lssdjt.com/?ContentType=xml&appkey=rss.xml");

    // RSA Encrypt&Decrypt
    private Map<String, String> keyMap = RSAUtils.createKeys(1024);
    private String publicKey = keyMap.get("publicKey");
    private String privateKey = keyMap.get("privateKey");
    private String encodedPwd = "";

    long ownerQQ = 0;
    List<Long> adminIds = new ArrayList<>();
    List<Long> autoAcceptList = new ArrayList<>();
    Map<Long, String> joinMsg = new HashMap<>();

    List<String> filterWords = new ArrayList<>();
    Map<String, Long> groupAliases = new HashMap<>();

    Map<Long, MCServer> serverInfo = new HashMap<>();
    Map<Long, CheckIn> checkinUsers = new HashMap<>();

    // main 函数仅供调试使用
    public static void main (String[] args) {
    }
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
            String cmd[] = {"", "", "", "", "", "", ""};

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


            if (isBotAdmin || isOwner) {
                switch (cmd[0]) {
                    case "debug":
                        switch (cmd[1]){
                            case "true":
                                debugMode = true;
                                break;
                            case "false":
                                debugMode = false;
                                break;
                            default:
                                mySendPrivateMsg(fromQQ, "[Bot] /debug true/false");
                                break;
                        }
                        break;
                    case "say":
                        String message = msg.replaceFirst("/" + cmd[0] + " ", "").replaceFirst(cmd[1] + " ", "").replaceFirst("#", "");
                        if (cmd[1].equals("")) {
                            mySendPrivateMsg(fromQQ, "[Bot] 请输入需要转发的群号!");
                        } else if (groupAliases.containsKey(cmd[1])) {
                            mySendGroupMsg(groupAliases.get(cmd[1]), message);
                        } else {
                            if (StringUtils.isNumeric(cmd[1]))
                                mySendGroupMsg(Integer.parseInt(cmd[1]), message);
                            else
                                mySendPrivateMsg(fromQQ, "[Bot] 请检查群号是否有误!");
                        }
                        break;
                    case "switch":
                        if (cmd[1].equals("off")) {
                            mySendPrivateMsg(fromQQ, "[Bot] 已将机器人禁言.");
                            botStatus = false;
                            saveConf();
                        } else if (cmd[1].equals("on")) {
                            mySendPrivateMsg(fromQQ, "[Bot] 已解除机器人的禁言.");
                            botStatus = true;
                        }
                        break;
                    case "rcon":
                        if (isRcon) {
                            switch (cmd[1]) {
                                case "cmd":
                                    if (!cmd[2].isEmpty()) {
                                        try {
                                            String decodedPwd = RSAUtils.privateDecrypt(encodedPwd, RSAUtils.getPrivateKey(privateKey));
                                            Rcon rcon = new Rcon(rconIP, rconPort, decodedPwd.getBytes());
                                            String result = rcon.command(msg.replace("/rcon cmd ", "").replace("#rcon cmd", ""));
                                            mySendPrivateMsg(fromQQ, "[Bot] " + result);
                                        } catch (Exception e) {
                                            if (debugMode) {
                                                e.printStackTrace();
                                                mySendPrivateMsg(fromQQ, "[Bot] 无法连接至服务器");
                                            } else
                                                mySendPrivateMsg(fromQQ, "[Bot] 无法连接至服务器");
                                        }
                                    } else
                                        mySendPrivateMsg(fromQQ, "[Bot] 请输入需要执行的命令! (不需要带\"/\")");
                                    break;
                                case "setup":
                                    if (!cmd[2].isEmpty() && StringUtils.isNumeric(cmd[3]) && !cmd[4].isEmpty()) {
                                        try {
                                            rconIP = cmd[2];
                                            rconPort = Integer.parseInt(cmd[3]);
                                            rconPwd = RSAUtils.publicEncrypt(cmd[4], RSAUtils.getPublicKey(publicKey));
                                            encodedPwd = rconPwd;
                                            mySendPrivateMsg(fromQQ, "[Bot] RCON 设置完成");
                                        } catch (Exception e) {
                                            if (debugMode)
                                                e.printStackTrace();
                                        }
                                    } else
                                        mySendPrivateMsg(fromQQ, "[Bot] 请输入正确的 RCON 地址/端口/密码!");
                                    break;
                                default:
                                    mySendPrivateMsg(fromQQ,
                                            "= RCON 设置 =\n" +
                                                    "/rcon setup [IP] [Port] [Password] 设置 Rcon 配置\n" +
                                                    "/rcon switch Rcon 开关\n" +
                                                    "/rcon cmd [Command] 使用 Rcon 执行命令");
                                    break;
                            }
                        } else
                            mySendPrivateMsg(fromQQ, "[Bot] 很抱歉, 机器人没有启用 RCON 功能.");
                        break;
                    case "help":
                        mySendPrivateMsg(fromQQ, "= 无名Bot " + VerClass.VERSION + " ="
                                + "\n /say [指定群] [内容] 以机器人向某个群发送消息"
                                + "\n /switch [on/off] 开关机器人"
                                + "\n /rcon 使用 Minecraft 服务器的 Rcon 功能"
                                + "\n /admin 管理机器人的管理员"
                        );
                        break;
                    case "rcon switch":
                        if (isRcon) {
                            isRcon = false;
                            mySendPrivateMsg(fromQQ, "[Bot] 已关闭 Rcon 功能");
                        } else
                            isRcon = true;
                        mySendPrivateMsg(fromQQ, "[Bot] 已打开 Rcon 功能. ");
                        break;
                    case "admin":
                        if (isOwner) {
                            switch (cmd[1]) {
                                case "list":
                                    mySendPrivateMsg(fromQQ, "[Bot] 机器人管理员列表: " + adminIds.toString());
                                    break;
                                case "add":
                                    if (!cmd[2].equals("") && StringUtils.isNumeric(cmd[2])) {
                                        long adminQQ = Integer.parseInt(cmd[2]);
                                        adminIds.add(adminQQ);
                                        mySendPrivateMsg(fromQQ, "[Bot] 已成功添加管理员 " + CQ.getStrangerInfo(adminQQ).getNick() + "(" + adminQQ + ")");
                                    }
                                    break;
                                case "del":
                                    if (!cmd[2].equals("") && StringUtils.isNumeric(cmd[2])) {
                                        long adminQQ = Integer.parseInt(cmd[2]);
                                        adminIds.remove(adminQQ);
                                        mySendPrivateMsg(fromQQ, "[Bot] 已成功删除管理员 " + CQ.getStrangerInfo(adminQQ).getNick() + "(" + adminQQ + ")");
                                        break;
                                    }
                            }
                        } else
                            mySendPrivateMsg(fromQQ, "[Bot] 你不是我的主人, 无法设置管理员哟");
                        break;
                    case "reload":
                        readConf();
                        mySendPrivateMsg(fromQQ, "[Bot] Config reloaded.");
                        break;
                    case "save":
                        saveConf();
                        mySendPrivateMsg(fromQQ, "[Bot] Config saved.");
                        break;
                    case "group":
                        if ("set".equals(cmd[1])) {
                            switch (cmd[2].toLowerCase()) {
                                case "serverinfo":
                                    if (StringUtils.isNumeric(cmd[3]) && !cmd[4].isEmpty() && StringUtils.isNumeric(cmd[5]) && !cmd[6].isEmpty()) {
                                        MCServer group = new MCServer();
                                        group.setEnabled(true);
                                        group.setServerIP(cmd[4]);
                                        group.setServerPort(Integer.parseInt(cmd[5]));
                                        group.setInfoMessage(msg.replace("/", "")
                                                .replace("#", "")
                                                .replace(cmd[0] + " ", "")
                                                .replace(cmd[1], "")
                                                .replace(" " + cmd[2], "")
                                                .replace(" " + cmd[3], "")
                                                .replace(" " + cmd[4], "")
                                                .replace(" " + cmd[5] + " ", "")
                                        );
                                        serverInfo.put((long) Integer.parseInt(cmd[3]), group);
                                        mySendPrivateMsg(fromQQ, "[Bot] 添加成功, 你将可以在群 " + cmd[3] + " 中获取指定服务器的状态!");
                                    } else if (cmd[3].equalsIgnoreCase("del") && StringUtils.isNumeric(cmd[4])) {
                                        if (serverInfo.containsKey((long) Integer.parseInt(cmd[4]))) {
                                            serverInfo.remove((long) Integer.parseInt(cmd[4]));
                                            mySendPrivateMsg(fromQQ, "[Bot] 已删除群 " + cmd[4] + " 的服务器信息");
                                        } else {
                                            mySendPrivateMsg(fromQQ, "[Bot] " + cmd[4] + " 没有设置服务器信息");
                                        }
                                    }
                                    break;
                                case "autoacceptList":
                                    if (!cmd[3].equals("") && !cmd[4].equals("")) {
                                        if (StringUtils.isNumeric(cmd[3])) {
                                            if (cmd[4].equals("t")) {
                                                long groupId = Integer.parseInt(cmd[3]);
                                                if (!autoAcceptList.contains(groupId)) {
                                                    autoAcceptList.add(groupId);
                                                    mySendPrivateMsg(fromQQ, "[Bot] 已打开 " + groupId + "的自动接受入群请求.");
                                                } else
                                                    mySendPrivateMsg(fromQQ, "[Bot] " + groupId + " 已打开自动接受入群请求了.");
                                            } else if (cmd[4].equals("f")) {
                                                long groupId = Integer.parseInt(cmd[3]);
                                                if (!autoAcceptList.contains(groupId)) {
                                                    mySendPrivateMsg(fromQQ, "[Bot] " + groupId + " 已关闭自动接受入群请求了.");
                                                } else {
                                                    autoAcceptList.remove(groupId);
                                                    mySendPrivateMsg(fromQQ, "[Bot] 已关闭 " + groupId + "的自动接受入群请求.");
                                                }
                                            }
                                        } else
                                            mySendPrivateMsg(fromQQ, "[Bot] 群号格式有误");
                                    } else
                                        mySendPrivateMsg(fromQQ, "[Bot] /group set autoacceptList [群号] [t/f]");
                                    break;
                                case "joinmsg":
                                    if (StringUtils.isNumeric(cmd[3]) && !cmd[4].equals("")) {
                                        long groupId = Integer.parseInt(cmd[3]);
                                        joinMsg.put(groupId, cmd[4]);
                                        mySendPrivateMsg(fromQQ, "[Bot] 已打开群 " + groupId + " 的加群欢迎功能.");
                                    } else if (cmd[3].equals("del") && StringUtils.isNumeric(cmd[4])) {
                                        long groupId = Integer.parseInt(cmd[4]);
                                        joinMsg.remove(groupId);
                                        mySendPrivateMsg(fromQQ, "[Bot] 已关闭群 " + groupId + " 的加群欢迎功能.");
                                    } else
                                        mySendPrivateMsg(fromQQ, "[Bot] 群号格式有误");
                                    break;
                                default:
                                    mySendPrivateMsg(fromQQ, "= 群设置帮助 =\n" +
                                            "/group set autoAcceptList [群号] [t/f]\n" +
                                            "/group set joinMsg [群号] [入群欢迎消息]\n" +
                                            "/group set serverinfo [群号] [服务器IP] [服务器端口] [自定义信息]");
                                    break;
                            }
                        } else {
                            mySendPrivateMsg(fromQQ, "= Bot 群组管理 =\n" +
                                    " /group list 列出所有已添加的群(需要手动添加!)\n" +
                                    " /group set 设置某个群的设置"
                            );
                        }
                        break;
                    case "setting":
                        switch (cmd[1].toLowerCase()) {
                            case "word":
                                if ("add".equals(cmd[2].toLowerCase())) {
                                    if (!cmd[3].isEmpty()) {
                                        filterWords.add(cmd[3]);
                                        mySendPrivateMsg(fromQQ, "[Bot] 已添加关键字.");
                                    } else
                                        mySendPrivateMsg(fromQQ, "[Bot] 请输入要添加的关键字!");
                                } else if ("del".equals(cmd[2].toLowerCase())) {
                                    if (!cmd[3].isEmpty()) {
                                        if (filterWords.contains(cmd[3])) {
                                            filterWords.remove(cmd[3]);
                                            mySendPrivateMsg(fromQQ, "[Bot] 已删除关键字.");
                                        } else
                                            mySendPrivateMsg(fromQQ, "[Bot] 该关键字不存在.");
                                    } else
                                        mySendPrivateMsg(fromQQ, "[Bot] 请输入要删除的关键字!");
                                } else {
                                    mySendPrivateMsg(fromQQ, "[Bot] 用法: /word add/del [触发关键字]\n暂不支持正则!");
                                }
                                break;
                            case "aliases":
                                if ("add".equals(cmd[2].toLowerCase())) {
                                    if (StringUtils.isNumeric(cmd[4]) && !cmd[3].isEmpty()) {
                                        long groupId = Integer.parseInt(cmd[4]);
                                        groupAliases.put(cmd[3], groupId);
                                        mySendPrivateMsg(fromQQ, "[Bot] 成功添加群别名 " + cmd[3] + "(" + groupId + ")");
                                    }
                                } else if ("del".equals(cmd[2].toLowerCase())) {
                                    if (!cmd[3].isEmpty()) {
                                        groupAliases.remove(cmd[3]);
                                        mySendPrivateMsg(fromQQ, "[Bot] 成功删除群别名 " + cmd[3]);
                                    }
                                }
                                break;
                            default:
                                mySendPrivateMsg(fromQQ, "= Bot 设置 =\n" +
                                        " /setting word 设置RSS屏蔽词\n" +
                                        " /setiing aliases 设置 /say 中的群号别称"
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
            String cmd[] = {"", "", "", "", ""};

            /**
             * @brief Processing msg into cmd & params
             * @param cmd[0] << cmd after !, e.g. "help" cmd[1] << first param etc.
             * @author Stiven.ding
             */

            for (int i = 0; i < 5; i++) {
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

            /**
             * @brief Run commands here
             * @author Stiven.ding
             */

            if (botStatus){
                switch (cmd[0]) {
                    // 帮助命令
                    case "help":
                        mySendGroupMsg(fromGroup,
                                "= 无名Bot " + VerClass.VERSION + " ="
                                        + "\n /sub (媒体) 订阅指定媒体" + "\n /unsub [媒体] 退订指定媒体" + "\n /switch [on/off] 开/关机器人"
                                        + "\n /mute [@/QQ] (dhm) 禁言(默认10m)" + "\n /mute all 全群禁言" + "\n /unmute [@/QQ] 解禁某人"
                                        + "\n /unmute all 解除全群禁言" + "\n /kick [@/QQ] (是否永封(t/f)) 踢出群成员"
                                        + "\n /admin 管理机器人的管理员" + "\n /rcon [命令] 执行 Minecraft 服务器命令"
                        );
                        break;
                    // 关闭命令
                    case "switch":
                        if (isBotAdmin || isOwner) {
                            if (cmd[1].equals("off")) {
                                mySendGroupMsg(fromGroup, "[Bot] 已将机器人禁言.");
                                botStatus = false;
                            } else
                                mySendGroupMsg(fromGroup, "[Bot] 机器人早已处于开启状态.");
                        } else
                            mySendGroupMsg(fromGroup, "[Bot] 你没有权限!");
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
                                    mySendGroupMsg(fromGroup, "[Bot] 已订阅 Solidot.");
                                    break;
                                case "jwu":
                                    jikeWakeUp.enable();
                                    mySendGroupMsg(fromGroup, "[Bot] 已订阅 即刻 - 一觉醒来世界发生了什么.");
                                    break;
                                case "toh":
                                    todayOnHistory.enable();
                                    mySendGroupMsg(fromGroup, "[Bot] 已订阅 历史上的今天.");
                                    break;
                                default:
                                    mySendGroupMsg(fromGroup, "[Bot] 未知频道.");
                                    break;
                            }
                        } else
                            mySendGroupMsg(fromGroup, "[Bot] 你没有权限!");
                        break;
                    // 退订命令
                    case "unsub":
                        if (isBotAdmin || isOwner) {
                            switch (cmd[1].toLowerCase()) {
                                case "sdt":
                                    solidot.disable();
                                    mySendGroupMsg(fromGroup, "[Bot] 已退订 Solidot.");
                                    break;
                                case "jwu":
                                    jikeWakeUp.disable();
                                    mySendGroupMsg(fromGroup, "[Bot] 已退订 即刻 - 一觉醒来世界发生了什么.");
                                    break;
                                case "toh":
                                    todayOnHistory.disable();
                                    mySendGroupMsg(fromGroup, "[Bot] 已退订 历史上的今天.");
                                    break;
                                default:
                                    mySendGroupMsg(fromGroup, "[Bot] 未知频道. 输入 /sub 查看所有媒体.");
                                    break;
                            }
                        } else
                            mySendGroupMsg(fromGroup, "[Bot] 你没有权限!");
                        break;
                    // 禁言
                    case "mute":
                        if (isGroupAdmin) {
                            if (cmd[1].equals("")) {
                                mySendGroupMsg(fromGroup, "[Bot] 用法: /mute [@/QQ号] [时间(秒)]");
                            } else if (cmd[1].equals("all")) {
                                CQ.setGroupWholeBan(fromGroup, true);
                                mySendGroupMsg(fromGroup, "[Bot] 已打开全群禁言.");
                            } else {
                                try {
                                    long banQQ = StringUtils.isNumeric(cmd[1]) ? Integer.parseInt(cmd[1])
                                            : CC.getAt(cmd[1]);
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
                                        mySendGroupMsg(fromGroup, "[Bot] 已禁言 " + CQ.getStrangerInfo(banQQ).getNick() + "(" + banQQ + ") " + banTime / 60 + "分钟.");
                                    } else
                                        mySendGroupMsg(fromGroup, "[Bot] 时间长度太大了！");
                                } catch (Exception e) {
                                    mySendGroupMsg(fromGroup, "[Bot] 命令格式有误! 用法: /mute [@/QQ号] [dhm]");
                                }
                            }
                        } else
                            mySendGroupMsg(fromGroup, "[Bot] 你没有权限!");
                        break;
                    // 解除禁言
                    case "unmute":
                        if (isGroupAdmin) {
                            if (cmd[1].equals("")) {
                                mySendGroupMsg(fromGroup, "[Bot] 用法: /unmute [@]");
                            } else if (cmd[1].equals("all")) {
                                CQ.setGroupWholeBan(fromGroup, false);
                                mySendGroupMsg(fromGroup, "[Bot] 已关闭全群禁言.");
                            } else {
                                try {
                                    long banQQ = StringUtils.isNumeric(cmd[1]) ? Integer.parseInt(cmd[1])
                                            : CC.getAt(cmd[1]);
                                    CQ.setGroupBan(fromGroup, banQQ, 0);
                                } catch (Exception e) {
                                    mySendGroupMsg(fromGroup, "[Bot] 请检查你输入的命令!");
                                }
                            }
                        } else
                            mySendGroupMsg(fromGroup, "[Bot] 你没有权限!");
                        break;
                    // 调试
                    case "debug":
                        if (isBotAdmin || isOwner) {
                            switch (cmd[1].toLowerCase()) {
                                case "rss": // 代码中务必只用小写，确保大小写不敏感
                                    mySendGroupMsg(fromGroup, new RssItem(cmd[2]).getContext());
                                    break;
                                case "toh":
                                    String text = todayOnHistory.getContext();
                                    mySendGroupMsg(111852382L,
                                            CC.face(74) + "今天是"
                                                    + Calendar.getInstance().get(Calendar.YEAR) + "年"
                                                    + (Calendar.getInstance().get(Calendar.MONTH) + 1) + "月"
                                                    + Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + "日" + "，"
                                                    + text.substring(0, text.indexOf("\n")).replaceFirst("-", "的今天是")
                                                    + "的日子");
                                    break;
                                case "wel":
                                    long parseQQ = StringUtils.isNumeric(cmd[2]) ? Integer.parseInt(cmd[2]) : CC.getAt(cmd[2]);
                                    groupMemberIncrease(subType, 100, fromGroup, fromQQ, parseQQ);
                                    break;
                                case "serverinfo":
                                    if (!cmd[2].equals("") && StringUtils.isNumeric(cmd[3])) {
                                        mySendGroupMsg(fromGroup, BotUtils.getServerInfo(cmd[2], Integer.parseInt(cmd[3])));
                                    } else
                                        mySendGroupMsg(fromGroup, "[Bot] Please check IP address or Port.");
                                    break;
                                case "checkin":
                                    if (isOwner) {
                                        switch (cmd[2].toLowerCase()) {
                                            case "set":
                                                try {
                                                    long userQQ = StringUtils.isNumeric(cmd[3]) ? Integer.parseInt(cmd[3]) : CC.getAt(cmd[3]);
                                                    CheckIn user = checkinUsers.get(userQQ);
                                                    double point = Integer.parseInt(cmd[4]);
                                                    user.setCheckInPoint(point);
                                                    mySendGroupMsg(fromGroup, "[Bot] 已设置 " + CQ.getStrangerInfo(userQQ).getNick() + " 的积分为 " + point);
                                                } catch (Exception ignored){
                                                    mySendGroupMsg(fromGroup, "[Bot] 请检查你输入的命令!");
                                                }
                                                break;
                                            case "reset":
                                                try {
                                                    long userQQ1 = StringUtils.isNumeric(cmd[3]) ? Integer.parseInt(cmd[3]) : CC.getAt(cmd[3]);
                                                    CheckIn user1 = checkinUsers.get(userQQ1);
                                                    user1.setCheckInPoint(0d);
                                                    mySendGroupMsg(fromGroup, "[Bot] 已重置 " + CQ.getStrangerInfo(userQQ1).getNick() + " 的签到积分");
                                                    break;
                                                } catch (Exception ignored){
                                                    mySendGroupMsg(fromGroup, "[Bot] 请检查你输入的命令!");
                                                }
                                        }
                                    }
                                    break;
                                default:
                                    mySendGroupMsg(fromGroup,
                                            "Version: " + VerClass.VERSION + "\nDebug Menu:"
                                                    + "\n rss [URL] - Get context manually"
                                                    + "\n toh - Get todayOnHistory" + "\n wel [#/@] - Manually welcome"
                                                    + "\n serverinfo [IP/addr] [Port] - Get minecraft server info"
                                                    + "\n checkin set/reset [@/QQ]"
                                    );
                                    break;
                            }
                        } else
                            mySendGroupMsg(fromGroup, "[Bot] 你没有权限!");
                        break;
                    case "kick":
                        if (isGroupAdmin) {
                            long kickQQ = StringUtils.isNumeric(cmd[1]) ? Integer.parseInt(cmd[1])
                                    : CC.getAt(cmd[1]);
                            if (cmd[2].equals("")) {
                                if ("".equals(cmd[1])) {
                                    mySendGroupMsg(fromGroup, "[Bot] 用法: /kick [@/QQ号] (是否永封(t/f))");
                                } else {
                                    CQ.setGroupKick(fromGroup, kickQQ, false);
                                    mySendGroupMsg(fromGroup, "[Bot] 已踢出 " + CC.at(fromQQ));
                                }
                            } else
                                switch (cmd[2]) {
                                    case "t":
                                        CQ.setGroupKick(fromGroup, kickQQ, true);
                                        mySendGroupMsg(fromGroup, "[Bot] 已踢出 " + CC.at(fromQQ));
                                        break;
                                    case "f":
                                        CQ.setGroupKick(fromGroup, kickQQ, false);
                                        mySendGroupMsg(fromGroup, "[Bot] 已踢出 " + CC.at(fromQQ));
                                        break;
                                    default:
                                        break;
                                }
                        } else
                            mySendGroupMsg(fromGroup, "[Bot] 你没有权限!");
                        break;
                    case "rcon":
                        if (isRcon) {
                            if (isBotAdmin || isOwner) {
                                if (!cmd[1].equals("")) {
                                    try {
                                        String decodedPwd = RSAUtils.privateDecrypt(encodedPwd, RSAUtils.getPrivateKey(privateKey));
                                        Rcon rcon = new Rcon(rconIP, rconPort, decodedPwd.getBytes());
                                        String result = rcon.command(msg.replaceAll("/" + cmd[0] + " ", ""));
                                        mySendGroupMsg(fromGroup, "[Bot] " + result);
                                    } catch (IOException e) {
                                        if (debugMode)
                                            e.printStackTrace();
                                        mySendGroupMsg(fromGroup, "[Bot] 连接至服务器发生了错误");
                                    } catch (AuthenticationException e) {
                                        mySendGroupMsg(fromGroup, "[Bot] Rcon 密码错误!");
                                    } catch (NoSuchAlgorithmException | InvalidKeySpecException ignored) {
                                    }
                                } else
                                    mySendGroupMsg(fromGroup, "[Bot] 请输入需要执行的命令! (不需要带\"/\")");
                            } else
                                mySendGroupMsg(fromGroup, "[Bot] 你没有权限!");
                        } else
                            mySendGroupMsg(fromGroup, "[Bot] 很抱歉, 机器人没有启用 RCON 功能.");
                        break;
                    case "admin":
                        if (isOwner) {
                            switch (cmd[1]) {
                                case "list":
                                    mySendGroupMsg(fromGroup, "[Bot] 机器人管理员列表: " + adminIds.toString());
                                    break;
                                case "add":
                                    if (!cmd[2].equals("")) {
                                        try {
                                            long adminQQ = StringUtils.isNumeric(cmd[2]) ? Integer.parseInt(cmd[2]) : CC.getAt(cmd[2]);
                                            adminIds.add(adminQQ);
                                            mySendGroupMsg(fromGroup, "[Bot] 已成功添加管理员 " + CQ.getStrangerInfo(adminQQ).getNick() + "(" + adminQQ + ")");
                                        } catch (Exception e) {
                                            mySendGroupMsg(fromGroup, "[Bot] 请检查你输入的命令!");
                                        }
                                    }
                                    break;
                                case "del":
                                    if (!cmd[2].equals("")) {
                                        try {
                                            long adminQQ = StringUtils.isNumeric(cmd[2]) ? Integer.parseInt(cmd[2]) : CC.getAt(cmd[2]);
                                            adminIds.remove(adminQQ);
                                            mySendGroupMsg(fromGroup, "[Bot] 已成功移除管理员 " + CQ.getStrangerInfo(adminQQ).getNick() + "(" + adminQQ + ")");
                                        } catch (Exception x) {
                                            mySendGroupMsg(fromGroup, "[Bot] 请检查你输入的命令!");
                                        }
                                        break;
                                    }
                                case "reload":
                                    readConf();
                                    mySendGroupMsg(fromGroup, "[Bot] 配置已重新载入.");
                                    break;
                                case "save":
                                    saveConf();
                                    mySendGroupMsg(fromGroup, "[Bot] 配置已保存.");
                                    break;
                                case "parse":
                                    mySendGroupMsg(fromGroup, saveConf());
                                    break;
                                default:
                                    mySendGroupMsg(fromGroup, "= Bot 管理员控制面板 =\n" +
                                            " /admin list 列出所有无名 Bot 的管理员\n" +
                                            " /admin add [@/QQ] 添加管理员\n" +
                                            " /admin del [@/QQ] 移除管理员\n" +
                                            " /admin reload 重载配置" +
                                            " /admin save 保存配置" +
                                            " /admin parse"
                                    );
                                    break;
                            }
                        } else
                            mySendGroupMsg(fromGroup, "[Bot] 你没有权限!");
                        break;
                    case "qd":
                    case "checkin":
                        if (!checkinUsers.containsKey(fromQQ)) {
                            CheckIn checkin = new CheckIn();
                            Date currentDate = new Date();
                            double point = new Random().nextInt(10);
                            checkin.setCheckInQQ(fromQQ);
                            checkin.setLastCheckInTime(currentDate);
                            checkin.setCheckInPoint(point);
                            checkin.setCheckInTime(1);
                            checkinUsers.put(fromQQ, checkin);
                            mySendGroupMsg(fromGroup, "[Bot] 签到成功!\n" +
                                    "获得 " + point + "点积分!"
                            );
                        } else if (checkinUsers.containsKey(fromQQ) && BotUtils.isCheckInReset(new Date(), checkinUsers.get(fromQQ).getLastCheckInTime())) {
                            double point = new Random().nextInt(10);
                            CheckIn user = checkinUsers.get(fromQQ);
                            if (user.getCheckInTime() > 1) {
                                user.setCheckInPoint(checkinUsers.get(fromQQ).getCheckInPoint() + point * BotUtils.checkInPointBonus(user.getCheckInTime()));
                            } else
                                user.setCheckInPoint(checkinUsers.get(fromQQ).getCheckInPoint() + point);
                            user.setLastCheckInTime(new Date());
                            user.setCheckInTime(user.getCheckInTime() + 1);
                            mySendGroupMsg(fromGroup, "[Bot] 签到成功!\n" +
                                    "获得 " + point + "点积分!"
                            );
                        } else {
                            mySendGroupMsg(fromGroup, "[Bot] 你今天已经签到过了!");
                        }
                        break;
                    case "info":
                        if (checkinUsers.containsKey(fromQQ)) {
                            double point = checkinUsers.get(fromQQ).getCheckInPoint();
                            int day = checkinUsers.get(fromQQ).getCheckInTime();
                            String lastCheckInTime = new SimpleDateFormat("yyyy-MM-dd").format(checkinUsers.get(fromQQ).getLastCheckInTime());
                            mySendGroupMsg(fromGroup, CC.at(fromQQ) + " " + CQ.getStrangerInfo(fromQQ).getNick() + "\n积分: " + point + "  连续签到了" + day + "天\n上次签到于 " + lastCheckInTime);
                        } else
                            mySendGroupMsg(fromGroup, "[Bot] 你还没有签到过哦");
                        break;
                }
            } else if (cmd[0].equals("switch") && cmd[1].equals("on")) {
                // 机器人禁言关闭
                if (isBotAdmin || isOwner) {
                    mySendGroupMsg(fromGroup, "[Bot] 已启用机器人.");
                    botStatus = true;
                }
            }
        } else if (msg.equals("服务器信息") || msg.equals("服务器状态")){
            // Reworked
            if (botStatus) {
                if (serverInfo.containsKey(fromGroup)){
                    MCServer group = serverInfo.get(fromGroup);
                    if (group.isEnabled()) {
                        String ip = group.getServerIP();
                        int port = group.getServerPort();
                        String customMsg = group.getInfoMessage();
                        mySendGroupMsg(fromGroup, BotUtils.getCustomServerInfo(ip, port, customMsg));
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

        if (!BotUpdater.isLatest()){
            CQ.logInfo("Updater", "Nameless Bot 有新版本: " + BotUpdater.getLatestVer());
            mySendPrivateMsg(ownerQQ, "Nameless Bot 有新版本: " + BotUpdater.getLatestVer());
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
            isRcon = rconObject.getBooleanValue("rconFunction");

            JSONObject adminsObject = JSONObject.parseObject(FileProcess.readFile(CQ.getAppDirectory() + "admins.json"));
            ownerQQ = adminsObject.getLong("owner");
            adminIds = JSON.parseObject(adminsObject.getString("admins"), new TypeReference<List<Long>>(){});

            JSONObject groupsObject = JSONObject.parseObject(FileProcess.readFile(groupsPath));
            autoAcceptList = JSON.parseObject(groupsObject.getString("autoAccept"), new TypeReference<List<Long>>(){});
            joinMsg = JSON.parseObject(groupsObject.getString("joinMsg"), new TypeReference<Map<Long, String>>(){});

            JSONObject settingObject = JSONObject.parseObject(FileProcess.readFile(CQ.getAppDirectory() + "config.json"));
            filterWords = JSON.parseObject(settingObject.getString("triggerWords"), new TypeReference<List<String>>(){});
            groupAliases = JSON.parseObject(settingObject.getString("groupAliases"), new TypeReference<Map<String, Long>>(){});

            JSONObject serverInfoObject = JSONObject.parseObject(FileProcess.readFile(CQ.getAppDirectory() + "serverinfo.json"));
            serverInfo = JSON.parseObject(serverInfoObject.getString("groups"), new TypeReference<Map<Long, MCServer>>(){});

            JSONObject checkInObject = JSONObject.parseObject(FileProcess.readFile(CQ.getAppDirectory() + "qiandao.json"));
            checkinUsers = JSON.parseObject(checkInObject.getString("checkinUsers"), new TypeReference<Map<Long, CheckIn>>(){});

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String saveConf() {
        // 状态json
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
        String adminsPath = CQ.getAppDirectory() + "admins.json";
        adminsObject.put("admins", adminIds);
        adminsObject.put("owner", ownerQQ);
        FileProcess.createFile(adminsPath, JSONObject.toJSONString(adminsObject, SerializerFeature.WriteNullNumberAsZero, SerializerFeature.WriteNullListAsEmpty));

        //群设置json (WIP)
        JSONObject groupSettingObject = new JSONObject();
        groupSettingObject.put("autoAccept", autoAcceptList);
        groupSettingObject.put("joinMsg", joinMsg);
        FileProcess.createFile(groupsPath, groupSettingObject.toJSONString());

        //服务器信息
        JSONObject serverInfoObject = new JSONObject();
        serverInfoObject.put("groups", serverInfo);
        FileProcess.createFile(CQ.getAppDirectory() + "serverinfo.json", serverInfoObject.toJSONString());

        //机器人设置
        JSONObject settingObject = new JSONObject();
        if (!groupAliases.isEmpty()) {
            settingObject.put("triggerWords", filterWords);
            settingObject.put("groupAliases", groupAliases);
            FileProcess.createFile(CQ.getAppDirectory() + "config.json", settingObject.toJSONString());
        }

        //签到
        JSONObject checkInObject = new JSONObject();
        checkInObject.put("checkinUsers", checkinUsers);
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
            if (joinMsg.containsKey(fromGroup)){
                mySendGroupMsg(fromGroup, joinMsg.get(fromGroup));
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
        if (autoAcceptList.contains(fromGroup) && subtype == 1){
            CQ.setGroupAddRequest(responseFlag, REQUEST_GROUP_ADD, REQUEST_ADOPT, "");
            mySendGroupMsg(fromGroup, "[Bot] 已自动接受来自 " + CQ.getStrangerInfo(fromQQ).getNick() + "(" + fromQQ + ") 的入群申请.");
        }
        else if (fromQQ == ownerQQ && subtype == 2){
            CQ.setGroupAddRequest(responseFlag, REQUEST_GROUP_INVITE, REQUEST_ADOPT, "");
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

    private void mySendGroupMsg(long groupId, String msg) {
        if (!filterWords.contains(msg))
            CQ.sendGroupMsg(groupId, msg);
    }

    private void mySendPrivateMsg(long fromQQ, String msg){
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
