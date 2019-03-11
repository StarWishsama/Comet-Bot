package top.starwish.namelessbot;

import com.sobte.cqp.jcq.entity.*;
import com.sobte.cqp.jcq.event.JcqAppAbstract;
import org.apache.commons.lang3.StringUtils;
import com.alibaba.fastjson.*;
import java.util.*;

public class BotMain extends JcqAppAbstract implements ICQVer, IMsg, IRequest {
    boolean botStatus;

    RssItem solidot = new RssItem(); // 仅供统一代码格式，实际上 solidot 并非 RSS 源
    RssItem jikeWakeUp = new RssItem("https://rsshub.app/jike/topic/text/553870e8e4b0cafb0a1bef68");

    String configPath = CQ.getAppDirectory() + "status.json";

    /**
     * @brief Init plugin
     * @return always 0
     */

    public int startup() {
        CQ.logInfoSuccess("NamelessBot", "初始化完成, 欢迎使用!");
        return 0;
    }

    public int privateMsg(int subType, int msgId, long fromQQ, String msg, int font) {
        // 这里处理消息
        if (fromQQ == 1552409060L || fromQQ == 1448839220L) {
            if (msg.startsWith("!bc") || msg.startsWith("/bc"))
                CQ.sendGroupMsg(111852382L, msg.replaceAll("!bc", "").replaceAll("/bc", ""));
        }
        return MSG_IGNORE;
    }

    public int groupMsg(int subType, int msgId, long fromGroup, long fromQQ, String fromAnonymous, String msg,
            int font) {

        // Solidot 推送转发
        if (fromGroup == 779672339L && botStatus) {
            if (solidot.getStatus()) {
                if (!(msg.contains("中国") || msg.contains("警察") || msg.contains("侵入") || msg.contains("华为"))) {
                    CQ.sendGroupMsg(111852382L, msg);
                }
            }
        }

        // 机器人功能处理
        else if ((msg.startsWith("!") || msg.startsWith("/"))) {
            // 解析是否为管理员
            boolean isAdmin = CQ.getGroupMemberInfoV2(fromGroup, fromQQ).getAuthority() > 1;
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

            /**
             * @brief Run commands here
             * @author Stiven.ding
             */

            if (botStatus) {
                switch (cmd[0]) {
                // 帮助命令
                case "help":
                    CQ.sendGroupMsg(fromGroup,
                            "= 无名Bot " + VerClass.VERSION + " =" + "\n /repeat [内容] (次数) 复读你要说的话"
                                    + "\n /sub [solidot/jikewakeup] 订阅指定媒体" + "\n /unsub [solidot/jikewakeup] 退订指定媒体"
                                    + "\n /switch [on/off] 开/关机器人" + "\n /mute [@或QQ号] (dhm) 禁言某人，默认 10m"
                                    + "\n /unmute [@或QQ号] 解禁某人" + "\n /debug");
                    break;
                // 复读命令
                case "repeat":
                    if (isAdmin) {
                        if (cmd[1].equals("")) {
                            CQ.sendGroupMsg(fromGroup, "[Bot] 请输入需要复读的话!");
                        } else {
                            try {
                                int times = Integer.parseInt(cmd[2]);
                                if (times < 1 || times > 20)
                                    CQ.sendGroupMsg(fromGroup, "[Bot] 次数太多了! 想刷爆嘛");
                                else
                                    for (int i = 0; i < times; i++)
                                        CQ.sendGroupMsg(fromGroup, cmd[1]);
                            } catch (Exception e) { // 没有识别到次数就只复读一次
                                CQ.sendGroupMsg(fromGroup, cmd[1]);
                            }
                        }
                    } else
                        CQ.sendGroupMsg(fromGroup, "[Bot] 你没有权限!");
                    break;
                // 关闭命令
                case "switch":
                    if (isAdmin) {
                        if (cmd[1].equals("off")) {
                            CQ.sendGroupMsg(fromGroup, "[Bot] 已将机器人禁言.");
                            botStatus = false;
                        } else
                            CQ.sendGroupMsg(fromGroup, "[Bot] 机器人早已处于开启状态.");
                    } else
                        CQ.sendGroupMsg(fromGroup, "[Bot] 你没有权限!");
                    break;
                // 订阅命令
                case "sub":
                    if (isAdmin) {
                        switch (cmd[1].toLowerCase()) {
                        case "solidot": // 代码中务必只用小写，确保大小写不敏感
                            solidot.enable();
                            CQ.sendGroupMsg(fromGroup, "[Bot] 已订阅 Solidot.");
                            break;
                        case "jikewakeup":
                            jikeWakeUp.enable();
                            CQ.sendGroupMsg(fromGroup, "[Bot] 已订阅 即刻 - 一觉醒来世界发生了什么.");
                            break;
                        default:
                            CQ.sendGroupMsg(fromGroup, "[Bot] 未知频道.");
                            break;
                        }
                    } else
                        CQ.sendGroupMsg(fromGroup, "[Bot] 你没有权限!");
                    break;
                // 退订命令
                case "unsub":
                    if (isAdmin) {
                        switch (cmd[1].toLowerCase()) { // 无视此处报错，仅需在 JDK >= 1.7 编译即可
                        case "solidot": // 代码中务必只用小写，确保大小写不敏感
                            solidot.disable();
                            CQ.sendGroupMsg(fromGroup, "[Bot] 已退订 Solidot.");
                            break;
                        case "jikewakeup":
                            jikeWakeUp.disable();
                            CQ.sendGroupMsg(fromGroup, "[Bot] 已退订 即刻 - 一觉醒来世界发生了什么.");
                            break;
                        default:
                            CQ.sendGroupMsg(fromGroup, "[Bot] 未知频道.");
                            break;
                        }
                    } else
                        CQ.sendGroupMsg(fromGroup, "[Bot] 你没有权限!");
                    break;
                // 禁言
                case "mute":
                    if (isAdmin) {
                        if (cmd[1].equals("")) {
                            CQ.sendGroupMsg(fromGroup, "[Bot] 用法: /mute [@/QQ号] [时间(秒)]");
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
                                    CQ.sendGroupMsg(fromGroup, "[Bot] 时间长度太大了！");
                            } catch (Exception e) {
                                CQ.sendGroupMsg(fromGroup, "[Bot] 命令格式有误! 用法: /mute [@/QQ号] [dhm]");
                            }
                        }
                    } else
                        CQ.sendGroupMsg(fromGroup, "[Bot] 你没有权限!");
                    break;
                // 解除禁言
                case "unmute":
                    if (isAdmin) {
                        if (cmd[1].equals("")) {
                            CQ.sendGroupMsg(fromGroup, "[Bot] 用法: /unmute [at需要解禁的人]");
                        } else {
                            try {
                                long banQQ = StringUtils.isNumeric(cmd[1]) ? Integer.parseInt(cmd[1])
                                        : CC.getAt(cmd[1]);
                                CQ.setGroupBan(fromGroup, banQQ, 0);
                            } catch (Exception e) {
                                CQ.sendGroupMsg(fromGroup, "[Bot] 请检查你输入的命令!");
                            }
                        }
                    } else
                        CQ.sendGroupMsg(fromGroup, "[Bot] 你没有权限!");
                    break;
                // 调试发送 RSS 订阅
                case "debug":
                    if (isAdmin) {
                        switch (cmd[1].toLowerCase()) {
                        case "rss": // 代码中务必只用小写，确保大小写不敏感
                            CQ.sendGroupMsg(fromGroup, RssItem.getFromURL(cmd[1]));
                            break;
                        case "reload":
                            readConf();
                            break;
                        case "save":
                            saveConf();
                            break;
                        default:
                            CQ.sendGroupMsg(fromGroup,
                                    "Version: " + VerClass.VERSION + "\nDebug Menu:"
                                            + "\n /debug RSS [URL] Get context manually" + "\n /debug reload"
                                            + "\n /debug save");
                            break;
                        }
                    } else
                        CQ.sendGroupMsg(fromGroup, "[Bot] 你没有权限!");
                    break;
                // 未知命令
                default:
                    CQ.sendGroupMsg(fromGroup, "[Bot] 命令不存在哟~");
                    break;

                }
            } else if (cmd[0].equals("switch") && cmd[1].equals("on")) {
                // 机器人禁言关闭
                if (isAdmin) {
                    CQ.sendGroupMsg(fromGroup, "[Bot] 已启用机器人.");
                    botStatus = true;
                }
            }
        }
        return MSG_IGNORE;

    }

    /**
     * @brief Startup
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
                saveConf(); // 每小时保存一次
                if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) == 7)
                    if (jikeWakeUp.getStatus() && botStatus)
                        CQ.sendGroupMsg(111852382L, jikeWakeUp.getContext());
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
            JSONObject jsonObject = JSONObject.parseObject(FileProcess.readFile(configPath));
            botStatus = jsonObject.getBooleanValue("botStatus");
            solidot.setStatus(jsonObject.getBooleanValue("solidot"));
            jikeWakeUp.setStatus(jsonObject.getBooleanValue("jikeWakeUp"));

        } catch (Exception e) {
            botStatus = true;
            solidot.enable();
            jikeWakeUp.enable();
            saveConf();
        }
    }

    public void saveConf() {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("botStatus", botStatus);
        jsonObject.put("solidot", solidot.getStatus());
        jsonObject.put("jikeWakeUp", jikeWakeUp.getStatus());
        FileProcess.createFile(configPath, jsonObject.toJSONString());
        CQ.logDebug("JSON", "配置已保存.");
    }

    public int groupMemberDecrease(int subtype, int sendTime, long fromGroup, long fromQQ, long beingOperateQQ) {
        if (botStatus) {
            if (fromGroup == 111852382L) {
                CQ.sendGroupMsg(fromGroup, "玩家 " + CC.at(beingOperateQQ) + "退出了本群！");
            }
        }
        return MSG_IGNORE;
    }

    public int groupMemberIncrease(int subtype, int sendTime, long fromGroup, long fromQQ, long beingOperateQQ) {
        // 入群欢迎
        if (botStatus) {
            if (fromGroup == 111852382L) {
                CQ.sendGroupMsg(fromGroup, "欢迎 " + CC.at(beingOperateQQ)
                        + "加入时光隧道!\n【进群请修改群名片为游戏ID】\n【建议使用群文件中的官方客户端!】\n\n服务器IP地址:bgp.sgsd.pw:25846\n赞助网址:http://www.mcrmb.com/cz/13153");
            }
        }
        return MSG_IGNORE;
    }

    public int friendAdd(int subtype, int sendTime, long fromQQ) {
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
}
