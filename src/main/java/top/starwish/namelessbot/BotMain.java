package top.starwish.namelessbot;

import com.sobte.cqp.jcq.entity.*;
import com.sobte.cqp.jcq.event.JcqAppAbstract;
import me.dilley.MineStat;
import org.apache.commons.lang3.StringUtils;
import com.alibaba.fastjson.*;

import java.io.File;
import java.util.*;

public class BotMain extends JcqAppAbstract implements ICQVer, IMsg, IRequest {
    String statusPath = CQ.getAppDirectory() + "status.json";
    String serverInfoPath = CQ.getAppDirectory() + "serverinfo.json";

    boolean botStatus = true;

    long groupId = 0;
    String serverIp = null;
    int serverPort = 0;
    String cantconnect = null;
    String infoMessage = null;

    RssItem solidot = new RssItem("https://www.solidot.org/index.rss");
    RssItem jikeWakeUp = new RssItem("https://rsshub.app/jike/topic/text/553870e8e4b0cafb0a1bef68");
    RssItem todayOnHistory = new RssItem("http://api.lssdjt.com/?ContentType=xml&appkey=rss.xml");

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
        if (msg.startsWith("/")) {
            // process only after there's a command, in order to get rid of memory trash
            String temp = msg.trim();
            String cmd[] = { "", "", "", "" };

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
            if (fromQQ == 1552409060L || fromQQ == 1448839220L) {
                switch (cmd[0]) {
                    case "say":
                        String message = msg.replaceAll(cmd[0] + " ", "").replaceAll(cmd[1] + " ", "");
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
                    case "help":
                        mySendPrivateMsg(fromQQ, "= 无名Bot " + VerClass.VERSION + " ="
                                + "\n /say [指定群] [内容]"
                                + "\n /switch [on/off]"
                        );
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
            boolean isAdmin = CQ.getGroupMemberInfoV2(fromGroup, fromQQ).getAuthority() > 1;
            // process only after there's a command, in order to get rid of memory trash
            String temp = msg.trim();
            String cmd[] = {"", "", "", ""};

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
                                        + "\n /unmute all 解除全群禁言" + "\n /kick [@/QQ] [是否永封(t/f)]" + "\n /debug");
                        break;
                    // 复读命令
                    case "repeat":
                        if (isAdmin) {
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
                                    mySendGroupMsg(fromGroup, cmd[1]);
                                }
                            }
                        } else
                            mySendGroupMsg(fromGroup, "[Bot] 你没有权限!");
                        break;
                    // 关闭命令
                    case "switch":
                        if (isAdmin) {
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
                        if (isAdmin) {
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
                        if (isAdmin) {
                            switch (cmd[1]) { // 无视此处报错，仅需在 JDK >= 1.7 编译即可
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
                        if (isAdmin) {
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
                                    if (banTime <= 30 * 24 * 60 * 60)
                                        CQ.setGroupBan(fromGroup, banQQ, banTime);
                                    else
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
                        if (isAdmin) {
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
                        if (isAdmin) {
                            switch (cmd[1].toLowerCase()) {
                                case "rss": // 代码中务必只用小写，确保大小写不敏感
                                    mySendGroupMsg(fromGroup, new RssItem(cmd[2]).getContext());
                                    break;
                                case "reload":
                                    readConf();
                                    break;
                                case "save":
                                    saveConf();
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
                                        mySendGroupMsg(fromGroup, new MCServerInfo(cmd[2], Integer.parseInt(cmd[3])).getServerInfo());
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
                        if (isAdmin) {
                            long kickQQ = StringUtils.isNumeric(cmd[1]) ? Integer.parseInt(cmd[1])
                                    : CC.getAt(cmd[1]);
                            if (cmd[2].equals("")) {
                                switch (cmd[1]) {
                                    case "":
                                        mySendGroupMsg(fromGroup, "[Bot] 用法: /kick [@/QQ号] [是否永封(t/f)]");
                                        break;
                                    default:
                                        CQ.setGroupKick(fromGroup, kickQQ, false);
                                        mySendGroupMsg(fromGroup, "[Bot] 已踢出 " + CQ.getStrangerInfo(kickQQ).getNick());
                                        break;
                                }
                            } else
                                switch (cmd[2]) {
                                    case "t":
                                        CQ.setGroupKick(fromGroup, kickQQ, true);
                                        mySendGroupMsg(fromGroup, "[Bot] 已踢出 " + CQ.getStrangerInfo(kickQQ).getNick());
                                        break;
                                    case "f":
                                        CQ.setGroupKick(fromGroup, kickQQ, false);
                                        mySendGroupMsg(fromGroup, "[Bot] 已踢出 " + CQ.getStrangerInfo(kickQQ).getNick());
                                        break;
                                    default:
                                        break;
                                }
                        } else
                            mySendGroupMsg(fromGroup, "[Bot] 你没有权限!");
                        break;
                }
            } else if (cmd[0].equals("switch") && cmd[1].equals("on")) {
                // 机器人禁言关闭
                if (isAdmin) {
                    mySendGroupMsg(fromGroup, "[Bot] 已启用机器人.");
                    botStatus = true;
                }
            }
        }
        else if (msg.equals("服务器信息") || msg.equals("服务器状态") || msg.equals("/info")){
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
                    MineStat acraft = new MineStat("103.91.211.243",13300);
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

        readConf();

        /**
         * @brief 启动时计划定时推送 & save confirguration
         * @author Starwish.sama
         */

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 7);
        c.set(Calendar.MINUTE, 30);

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                if (Calendar.getInstance().get(Calendar.MINUTE) == 30)
                    saveConf(); // 每小时保存一次

                // todayOnHistory @ 7:30 AM
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

                // jikeWakeUp @ 8:30 AM
                if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) == 8)
                    if (jikeWakeUp.getStatus() && botStatus) {
                        mySendGroupMsg(111852382L, jikeWakeUp.getContext().replaceAll("\uD83D\uDC49", CC.emoji(128073) ) + "\n即刻推送 - NamelessBot");
                    }
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
        } catch (Exception e) {
        }
    }

    public String saveConf() {
        JSONObject statusObject = new JSONObject();
        statusObject.put("botStatus", botStatus);
        statusObject.put("solidot", solidot.getStatus());
        statusObject.put("jikeWakeUp", jikeWakeUp.getStatus());
        statusObject.put("todayOnHistory", todayOnHistory.getStatus());
        FileProcess.createFile(statusPath, statusObject.toJSONString());
        JSONObject serverInfoObject = new JSONObject();
        serverInfoObject.put("groups", "");
        List groups = CQ.getGroupList();
        for (int i = 0; i < groups.size(); i++) {
            serverInfoObject.put(String.format("%d", groups.get(i)), "test");
        }
        FileProcess.createFile(serverInfoPath, serverInfoObject.toJSONString());
        CQ.logDebug("JSON", "配置已保存.");
        return statusObject.toJSONString();
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
                mySendGroupMsg(552185847L, CC.at(beingOperateQQ)
                        + "\n欢迎来到玩家的天堂 怪物的地狱\n在这里 你会体验到最极致的击杀快感~\n不要相信老玩家说的难度高，难度一点也不高~真的");
            }
            else
                return MSG_IGNORE;
        }
        return MSG_IGNORE;
    }

    public int friendAdd(int subtype, int sendTime, long fromQQ) {
        CQ.sendPrivateMsg(1552409060L, CQ.getStrangerInfo(fromQQ).getNick() + "(" + fromQQ + ") " + "向机器人发送了好友请求");
        return MSG_IGNORE;
    }

    public int requestAddFriend(int subtype, int sendTime, long fromQQ, String msg, String responseFlag) {
        return MSG_IGNORE;
    }

    public int requestAddGroup(int subtype, int sendTime, long fromGroup, long fromQQ, String msg,
            String responseFlag) {
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
