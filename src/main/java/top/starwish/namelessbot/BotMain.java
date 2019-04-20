package top.starwish.namelessbot;

import com.sobte.cqp.jcq.entity.*;
import com.sobte.cqp.jcq.event.JcqAppAbstract;
import me.dilley.MineStat;
import net.kronos.rkon.core.Rcon;
import org.apache.commons.lang3.StringUtils;
import com.alibaba.fastjson.*;
import top.starwish.namelessbot.entity.MCServer;
import top.starwish.namelessbot.entity.RssItem;
import top.starwish.namelessbot.utils.RSAUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class BotMain extends JcqAppAbstract implements ICQVer, IMsg, IRequest {
    String statusPath = CQ.getAppDirectory() + "status.json";
    String groupsPath = CQ.getAppDirectory() + "groupsettings.json";
    String rconPath = CQ.getAppDirectory() + "rcon.json";

    boolean botStatus = true;

    int groupId = 0;
    String serverIp = null;
    int serverPort = 0;
    String cantconnect = null;
    String infoMessage = null;

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
    List<Long> adminIds = new ArrayList();

    // Group settings
    List<Long> autoaccept = new ArrayList();
    HashMap<Long, String> joinmsg = new HashMap<>();

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
            String cmd[] = { "", "", "", "", ""};

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


            if (isBotAdmin || isOwner) {
                switch (cmd[0]) {
                    case "say":
                        String message = msg.replaceAll("/" + cmd[0] + " ", "").replaceAll(cmd[1] + " ", "").replaceAll("#", "");
                        switch (cmd[1]) {
                            case "":
                                mySendPrivateMsg(fromQQ, "[Bot] 请输入需要转发的群号!");
                                break;
                            case "acraft":
                                mySendGroupMsg(552185847L, message);
                                break;
                            case "times":
                                mySendGroupMsg(111852382L, message);
                                break;
                            default:
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
                            switch (cmd[1]){
                                case "cmd":
                                    if (!cmd[2].isEmpty()) {
                                        try {
                                            String decodedPwd = RSAUtils.privateDecrypt(encodedPwd, RSAUtils.getPrivateKey(privateKey));
                                            Rcon rcon = new Rcon(rconIP, rconPort, decodedPwd.getBytes());
                                            String result = rcon.command(msg.replace("/rcon cmd ", "").replace("#rcon cmd", ""));
                                            mySendPrivateMsg(fromQQ, "[Bot] " + result);
                                        } catch (Exception e) {
                                            mySendPrivateMsg(fromQQ, "[Bot] 无法连接至服务器");
                                        }
                                    } else
                                        mySendPrivateMsg(fromQQ, "[Bot] 请输入需要执行的命令! (不需要带\"/\")");
                                    break;
                                case "setup":
                                    if (!cmd[2].isEmpty() && StringUtils.isNumeric(cmd[3]) && !cmd[4].isEmpty()){
                                        rconIP = cmd[2];
                                        rconPort = Integer.parseInt(cmd[3]);
                                        try {
                                            rconPwd = RSAUtils.publicEncrypt(cmd[4], RSAUtils.getPublicKey(publicKey));
                                            encodedPwd = rconPwd;
                                            saveConf();
                                        } catch (Exception e){
                                            mySendPrivateMsg(fromQQ, "[Bot] 发生错误, 请查看后台");
                                            e.printStackTrace();
                                        }
                                        saveConf();
                                        mySendPrivateMsg(fromQQ, "[Bot] RCON 设置完成");
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
                        if (isRcon){
                            isRcon = false;
                            mySendPrivateMsg(fromQQ, "[Bot] 已关闭 Rcon 功能");
                        } else
                            isRcon = true;
                            mySendPrivateMsg(fromQQ, "[Bot] 已打开 Rcon 功能. ");
                        break;
                    case "admin":
                        if (isOwner){
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
                    case "group":
                        switch (cmd[1]){
                            case "list":
                                mySendPrivateMsg(fromQQ, "自动同意入群的群: " + autoaccept.toString());
                                break;
                            case "set":
                                switch (cmd[2]){
                                    case "autoaccept":
                                        if (!cmd[3].equals("") && !cmd[4].equals("")){
                                            if (StringUtils.isNumeric(cmd[3])){
                                                if (cmd[4].equals("t")){
                                                    long groupId = Integer.parseInt(cmd[3]);
                                                    if (!autoaccept.contains(groupId)) {
                                                        autoaccept.add(groupId);
                                                        mySendPrivateMsg(fromQQ, "[Bot] 已打开 " + groupId + "的自动接受入群请求.");
                                                    } else
                                                        mySendPrivateMsg(fromQQ, "[Bot] " + groupId + " 已打开自动接受入群请求了.");
                                                } else if (cmd[4].equals("f")){
                                                    long groupId = Integer.parseInt(cmd[3]);
                                                    if (!autoaccept.contains(groupId)){
                                                        mySendPrivateMsg(fromQQ, "[Bot] " + groupId + " 已关闭自动接受入群请求了.");
                                                    } else {
                                                        autoaccept.remove(groupId);
                                                        mySendPrivateMsg(fromQQ, "[Bot] 已关闭 " + groupId + "的自动接受入群请求.");
                                                    }
                                                }
                                            } else
                                                mySendPrivateMsg(fromQQ, "[Bot] 群号格式有误");
                                        } else
                                            mySendPrivateMsg(fromQQ, "[Bot] /group set autoaccept [群号] [t/f]");
                                        break;
                                    case "serverinfo":
                                        break;
                                    case "joinmsg":
                                        if (!cmd[3].equals("") && !cmd[4].equals("")){
                                            if (StringUtils.isNumeric(cmd[3])){
                                                long groupId = Integer.parseInt(cmd[3]);
                                                if (!joinmsg.containsKey(groupId)){
                                                    joinmsg.put(groupId, cmd[4]);
                                                    mySendPrivateMsg(fromQQ, "[Bot] 已打开群 " + groupId + " 的加群欢迎功能.");
                                                } else {
                                                    mySendPrivateMsg(fromQQ, "[Bot] 该群已经打开加群欢迎功能了!");
                                                }
                                            }
                                            else if (cmd[3].equals("del") && StringUtils.isNumeric(cmd[4])){
                                                long groupId = Integer.parseInt(cmd[4]);
                                                if (!joinmsg.containsKey(groupId)){
                                                    mySendPrivateMsg(fromQQ, "[Bot] 该群没有打开加群欢迎功能!");
                                                } else {
                                                    mySendPrivateMsg(fromQQ, "[Bot] 已关闭群 " + groupId + " 的加群欢迎功能.");
                                                }
                                            } else
                                                mySendPrivateMsg(fromQQ, "[Bot] 群号格式有误");
                                        }
                                        break;
                                    default:
                                        mySendPrivateMsg(fromQQ, "= 群设置帮助 =\n" +
                                                "/group set autoaccept [群号] [t/f]\n" +
                                                "[WIP] /group set serverinfo [群号] [服务器IP] [端口] [信息样式]\n" +
                                                "/group set joinmsg [群号] [入群欢迎消息]");
                                        break;
                                }
                                break;
                            default:
                                mySendPrivateMsg(fromQQ, "= Bot 群组管理 =\n" +
                                                " /group list 列出所有已添加的群(需要手动添加!)\n" +
                                                " /group set 设置某个群的设置"
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
        if (msg.startsWith("/")) {
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

            for (int i = 0; i < 4; i++) {
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
                                "= 无名Bot " + VerClass.VERSION + " =" + "\n /repeat [内容] (次数) 复读你要说的话"
                                        + "\n /sub (媒体) 订阅指定媒体" + "\n /unsub [媒体] 退订指定媒体" + "\n /switch [on/off] 开/关机器人"
                                        + "\n /mute [@/QQ] (dhm) 禁言(默认10m)" + "\n /mute all 全群禁言" + "\n /unmute [@/QQ] 解禁某人"
                                        + "\n /unmute all 解除全群禁言" + "\n /kick [@/QQ] (是否永封(t/f))"
                                        + "\n /admin 管理机器人的管理员" + "\n/rcon [命令]"
                        );
                        break;
                    // 复读命令
                    case "repeat":
                        if (isGroupAdmin || isBotAdmin || isOwner) {
                            if (cmd[1].equals("")) {
                                mySendGroupMsg(fromGroup, "[Bot] 请输入需要复读的话!");
                            } else {
                                try {
                                    int times = Integer.parseInt(cmd[2]);
                                    if (times < 1 || times > 20)
                                        mySendGroupMsg(fromGroup, "[Bot] 次数太多了! 想刷爆嘛");
                                    else
                                        for (int i = 0; i < times; i++)
                                            mySendGroupMsg(fromGroup, cmd[1]);
                                } catch (Exception e) { // 没有识别到次数就只复读一次
                                    mySendGroupMsg(fromGroup, msg.replaceAll("/repeat ", ""));
                                }
                            }
                        } else
                            mySendGroupMsg(fromGroup, "[Bot] 你没有权限!");
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
                        if (isGroupAdmin || isBotAdmin || isOwner) {
                            switch (cmd[1]) {
                                case "":
                                    mySendGroupMsg(fromGroup, "媒体列表: /sub (媒体)" + "\n [SDT] Solidot 奇客资讯"
                                            + "\n [JWU] 一觉醒来世界发生了什么" + "\n [TOH] 历史上的今天");
                                    break;
                                case "SDT":
                                    solidot.enable();
                                    mySendGroupMsg(fromGroup, "[Bot] 已订阅 Solidot.");
                                    break;
                                case "JWU":
                                    jikeWakeUp.enable();
                                    mySendGroupMsg(fromGroup, "[Bot] 已订阅 即刻 - 一觉醒来世界发生了什么.");
                                    break;
                                case "TOH":
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
                        if (isGroupAdmin || isBotAdmin || isOwner) {
                            switch (cmd[1]) {
                                case "SDT":
                                    solidot.disable();
                                    mySendGroupMsg(fromGroup, "[Bot] 已退订 Solidot.");
                                    break;
                                case "JWU":
                                    jikeWakeUp.disable();
                                    mySendGroupMsg(fromGroup, "[Bot] 已退订 即刻 - 一觉醒来世界发生了什么.");
                                    break;
                                case "TOH":
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
                        if (isGroupAdmin || isBotAdmin || isOwner) {
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
                        if (isGroupAdmin || isBotAdmin || isOwner) {
                            if (cmd[1].equals("")) {
                                mySendGroupMsg(fromGroup, "[Bot] 用法: /unmute [at需要解禁的人]");
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
                                case "reload":
                                    readConf();
                                    mySendGroupMsg(fromGroup, "[Bot] Config reloaded.");
                                    break;
                                case "save":
                                    saveConf();
                                    mySendGroupMsg(fromGroup, "[Bot] Config saved.");
                                    break;
                                case "parse":
                                    mySendGroupMsg(fromGroup, saveConf());
                                    break;
                                case "toh":
                                    String text = todayOnHistory.getContext();
                                    mySendGroupMsg(111852382L,
                                            CC.face(74) + "各位时光隧道玩家--好" + "\n------------------------\n今天是"
                                                    + Calendar.getInstance().get(Calendar.YEAR) + "年"
                                                    + (Calendar.getInstance().get(Calendar.MONTH) + 1) + "月"
                                                    + Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + "日" + "，"
                                                    + text.substring(0, text.indexOf("\n")).replaceFirst("-", "的今天是")
                                                    + "的日子\n一小时之后我会推送今天的早间新闻\n新的一天开始了！" + CC.face(190) + "今天别忘了去服务器领取签到奖励噢~~");
                                    break;
                                case "wel":
                                    long parseQQ = StringUtils.isNumeric(cmd[2]) ? Integer.parseInt(cmd[2]) : CC.getAt(cmd[2]);
                                    groupMemberIncrease(subType, 100, fromGroup, fromQQ, parseQQ);
                                    break;
                                case "serverinfo":
                                    if (!cmd[2].equals("") && StringUtils.isNumeric(cmd[3])) {
                                        mySendGroupMsg(fromGroup, new MCServer(cmd[2], Integer.parseInt(cmd[3])).getServerInfo());
                                    } else
                                        mySendGroupMsg(fromGroup, "[Bot] Please check IP address or Port.");
                                    break;
                                default:
                                    mySendGroupMsg(fromGroup,
                                            "Version: " + VerClass.VERSION + "\nDebug Menu:"
                                                    + "\n RSS [URL] - Get context manually" + "\n reload - Reload config"
                                                    + "\n save - Save config" + "\n parse - Parse JSON"
                                                    + "\n toh - Get todayOnHistory" + "\n wel [#/@] - Manually welcome"
                                                    + "\n serverinfo [IP/addr] [Port] - Get Minecraft server info"
                                    );
                                    break;
                            }
                        } else
                            mySendGroupMsg(fromGroup, "[Bot] 你没有权限!");
                        break;
                    case "kick":
                        if (isGroupAdmin || isBotAdmin || isOwner) {
                            long kickQQ = StringUtils.isNumeric(cmd[1]) ? Integer.parseInt(cmd[1])
                                    : CC.getAt(cmd[1]);
                            if (cmd[2].equals("")) {
                                if ("".equals(cmd[1])) {
                                    mySendGroupMsg(fromGroup, "[Bot] 用法: /kick [@/QQ号] [是否永封(t/f)]");
                                } else {
                                    CQ.setGroupKick(fromGroup, kickQQ, false);
                                    mySendGroupMsg(fromGroup, "[Bot] 已踢出 " + CQ.getGroupMemberInfo(kickQQ, fromGroup).getNick());
                                }
                            } else
                                switch (cmd[2]) {
                                    case "t":
                                        CQ.setGroupKick(fromGroup, kickQQ, true);
                                        mySendGroupMsg(fromGroup, "[Bot] 已踢出 " + CQ.getGroupMemberInfo(kickQQ, fromGroup).getNick());
                                        break;
                                    case "f":
                                        CQ.setGroupKick(fromGroup, kickQQ, false);
                                        mySendGroupMsg(fromGroup, "[Bot] 已踢出 " + CQ.getGroupMemberInfo(kickQQ, fromGroup).getNick());
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
                                    } catch (Exception e) {
                                        mySendGroupMsg(fromGroup, "[Bot] 连接至服务器发生了错误");
                                        e.printStackTrace();
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
                                            mySendGroupMsg(fromGroup, "[Bot] 已成功删除管理员 " + CQ.getStrangerInfo(adminQQ).getNick() + "(" + adminQQ + ")");
                                        } catch (Exception e) {
                                            mySendGroupMsg(fromGroup, "[Bot] 请检查你输入的命令!");
                                        }
                                        break;
                                    }
                            }
                        }
                        break;
                }
            } else if (cmd[0].equals("switch") && cmd[1].equals("on")) {
                // 机器人禁言关闭
                if (isBotAdmin || isOwner) {
                    mySendGroupMsg(fromGroup, "[Bot] 已启用机器人.");
                    botStatus = true;
                }
            }
        }
        else if (msg.equals("服务器信息") || msg.equals("服务器状态")){
            if (botStatus) {
                if (fromGroup == 111852382L) {
                    // 防止获取的数据永远一样
                    MineStat times = new MineStat("bgp.sgsd.pw", 25846);
                    if (times.isServerUp()) {
                        mySendGroupMsg(fromGroup, "= 时光隧道 - 五周目 =\n在线玩家: "
                                + times.getCurrentPlayers()
                                + "/" + times.getMaximumPlayers()
                                + "\n" + "延迟: " + times.getLatency() + "ms"
                        );
                    } else
                        mySendGroupMsg(fromGroup, "[Bot] 无法连接至服务器, 可能正在维护?");
                } else if (fromGroup == 552185847L) {
                    // 防止获取的数据永远一样
                    MineStat acraft = new MineStat("103.91.211.243",17000);
                    if (acraft.isServerUp()) {
                        mySendGroupMsg(fromGroup, CC.at(fromQQ) + "\n在线玩家: "
                                + acraft.getCurrentPlayers() + "/" + acraft.getMaximumPlayers()
                                + "\n延迟: " + acraft.getLatency()
                                + "ms\n\nACraft - 2019"
                        );
                    } else
                        mySendGroupMsg(fromGroup, "[Bot] ACraft 目前可能正在维护, 稍等一会哟");
                } else if (fromGroup == groupId){
                    MineStat server = new MineStat(serverIp, serverPort);
                    if (server.isServerUp()){
                        String message = infoMessage.replaceAll("%在线玩家%", server.getCurrentPlayers())
                                .replaceAll("%最大人数%", server.getMaximumPlayers())
                                .replaceAll("%延迟%", String.format("%d", server.getLatency()))
                                .replaceAll("%MOTD%", server.getMotd())
                                .replaceAll("%版本%", server.getVersion());
                        mySendGroupMsg(fromGroup, message);
                    } else
                        mySendGroupMsg(fromGroup, cantconnect.replaceAll("%addr%", serverIp));
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

        configStartup();
        if (!configStartup()){
            CQ.logError("NamelessBot", "Something is wrong when startup!");
            enable = false;
        }
        readConf();

        /**
         * @brief 启动时计划定时推送 & save confirguration
         * @author NamelessSAMA & Stiven.Ding
         */

        Calendar.getInstance().set(Calendar.HOUR_OF_DAY, 7);

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                if (Calendar.getInstance().get(Calendar.MINUTE) == 30)
                    saveConf(); // 每小时保存一次

                // Solidot auto push service
                if (Calendar.getInstance().get(Calendar.MINUTE) == 15) {
                    if (botStatus && solidot.getStatus()) {
                        String temppath = CQ.getAppDirectory() + "solidottemp.txt";
                        File solidottemp = new File(temppath);

                        System.out.println("[SolidotPush] TestMessage");

                        if (!solidottemp.exists()) {
                            FileProcess.createFile(temppath, solidot.getTitle());
                        } else {
                            try {
                                String temptitle = FileProcess.readFile(temppath);
                                String title = solidot.getTitle();
                                if (!temptitle.equals("") && !temptitle.equals(title)) {
                                    String context = solidot.getContext() + "\nSolidot 推送\nPowered by NamelessBot";
                                    mySendGroupMsg(111852382L, context);
                                    FileProcess.createFile(temppath, solidot.getTitle());
                                } else if (temptitle.isEmpty()){
                                    String context = solidot.getContext() + "\nSolidot 推送\nPowered by NamelessBot";
                                    mySendGroupMsg(111852382L, context);
                                    FileProcess.createFile(temppath, solidot.getTitle());
                                }
                            } catch (IOException e) {
                                FileProcess.createFile(temppath, solidot.getTitle());
                            }
                        }
                    }
                }

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

                // jikeWakeUp @ 8:00 AM
                if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) == 8)
                    if (jikeWakeUp.getStatus() && botStatus) {
                        mySendGroupMsg(111852382L, jikeWakeUp.getContext().replaceAll("\uD83D\uDC49", CC.emoji(128073) ) + "\n即刻推送 - NamelessBot");
                    }
            }
        }, Calendar.getInstance().getTime(), 1000 * 60 * 60);
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
            autoaccept = JSON.parseObject(groupsObject.getString("autoAccept"), new TypeReference<List<Long>>(){});
            joinmsg = JSON.parseObject(groupsObject.getString("joinmsg"), new TypeReference<HashMap<Long, String>>(){});

        } catch (Exception ignored) {
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

        /**
         * @TODO 服务器信息json

        JSONObject serverInfoObject = new JSONObject();
        serverInfoObject.put("ServerIP", "Your Server IP here");
        serverInfoObject.put("ServerPort", 25565);
        serverInfoObject.put("ServerInfoMsg", "服务器信息\n玩家: %在线玩家%/%最大人数%\n延迟: %延迟%");


          JSONObject groupObject = new JSONObject();

        if (CQ.getGroupList() != null) {
            groupObject.put("" + groupId, groupsPath);
        } else
            CQ.logError("NamelessBot", "机器人没有加入任何一个QQ群!");

        JSONObject settingObject = new JSONObject();
        settingObject.put("groups_settings", groupObject);

        FileProcess.createFile(groupsPath, JSONObject.toJSONString(settingObject, WriteMapNullValue));
         */

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
        adminsObject.put("admins", adminIds.toString());
        adminsObject.put("owner", ownerQQ);
        FileProcess.createFile(adminsPath, adminsObject.toJSONString());

        //群设置json (WIP)
        JSONObject groupSettingObject = new JSONObject();
        groupSettingObject.put("autoAccept", autoaccept.toString());
        groupSettingObject.put("joinmsg", joinmsg.toString());
        FileProcess.createFile(groupsPath, groupSettingObject.toJSONString());

        CQ.logDebug("JSON", "配置已保存.");

        return "status.json:\n" + statusObject.toJSONString();
    }

    public boolean configStartup(){
        File statusJSON = new File(CQ.getAppDirectory() + "status.json");
        File rconJSON = new File(CQ.getAppDirectory() + "rcon.json");

        if (!statusJSON.exists() || !rconJSON.exists()){
            try {
                CQ.logDebug("ConfigSetup", "Config file isn't exist, Creating...");
                Files.copy(getClass().getClassLoader().getResourceAsStream("status.json"), statusJSON.toPath());
                Files.copy(getClass().getClassLoader().getResourceAsStream("rcon.json"), rconJSON.toPath());
                CQ.logDebug("ConfigSetup", "Created config file successfully.");
            } catch (IOException e) {
                CQ.logFatal("ConfigSetup", "An unexpected error occurred.");
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public int groupMemberDecrease(int subtype, int sendTime, long fromGroup, long fromQQ, long beingOperateQQ) {
        return MSG_IGNORE;
    }

    public int groupMemberIncrease(int subtype, int sendTime, long fromGroup, long fromQQ, long beingOperateQQ) {
        // 入群欢迎
        if (botStatus) {
            if (fromGroup == 111852382L) {
                mySendGroupMsg(111852382L, "欢迎 " + CC.at(beingOperateQQ)
                        + "加入时光隧道!\n【进群请修改群名片为游戏ID】\n【建议使用群文件中的官方客户端!】\n\n服务器IP地址: bgp.sgsd.pw:25846\n赞助网址: http://www.mcrmb.com/cz/13153");
            }
            else if (fromGroup == 552185847L){
                mySendGroupMsg(552185847L, CC.at(beingOperateQQ) + " 欢迎加入ACraft!");
            }
            else if (joinmsg.containsKey(fromGroup)){
                mySendGroupMsg(fromGroup, joinmsg.get(fromGroup).replaceAll("@入群的人", CC.at(beingOperateQQ)));
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
        if (autoaccept.contains(fromGroup) && subtype == 1){
            CQ.setGroupAddRequest(responseFlag, REQUEST_GROUP_ADD, REQUEST_ADOPT, null);
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
        if (!(msg.contains("警察") || msg.contains("侵入") || msg.contains("华为") || msg.contains("共产党")))
            CQ.sendGroupMsg(groupId, msg);
    }

    public void mySendPrivateMsg(long fromQQ, String msg){
        if (!(msg.contains("警察") || msg.contains("侵入") || msg.contains("华为")|| msg.contains("共产党")))
            CQ.sendPrivateMsg(fromQQ, msg);
    }
}
