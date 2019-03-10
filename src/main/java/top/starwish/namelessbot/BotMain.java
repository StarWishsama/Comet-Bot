package top.starwish.namelessbot;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.sobte.cqp.jcq.entity.*;
import com.sobte.cqp.jcq.event.JcqAppAbstract;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;

public class BotMain extends JcqAppAbstract implements ICQVer, IMsg, IRequest {
    public boolean botStatus = true;
    public boolean subSolidot = true;
    public boolean subJikeWakeup = true;

    File status = new File(CQ.getAppDirectory(), "status.yml");

    String jikeWakeup = "https://rsshub.app/jike/topic/text/553870e8e4b0cafb0a1bef68";

    public boolean statusSetup(){
        if (!status.exists()){
            try {
                Files.copy(getClass().getResourceAsStream("status.yml"), status.toPath());
            } catch (IOException e){
                e.printStackTrace();
                return false;
            }
        }
        Yaml yaml = new Yaml();
        return true;
    }

    public String appInfo() {
        String AppID = "top.starwish.namelessbot";
        return CQAPIVER + "," + AppID;
    }

    /**
     * 酷Q启动 (Type=1001)<br>
     * 本方法会在酷Q【主线程】中被调用。<br>
     * 请在这里执行插件初始化代码。<br>
     * 请务必尽快返回本子程序，否则会卡住其他插件以及主程序的加载。
     *
     * @return 请固定返回0
     */
    public int startup() {
        // 获取应用数据目录(无需储存数据时，请将此行注释)
        statusSetup();
        String appDirectory = CQ.getAppDirectory();
        CQ.logInfo("NamelessBot", "初始化完成, 欢迎使用!");
        // 返回如：D:\CoolQ\app\com.sobte.cqp.jcq\app\com.example.demo\
        // 应用的所有数据、配置【必须】存放于此目录，避免给用户带来困扰。
        return 0;
    }

    public int exit() {
        return 0;
    }

    public int enable() {
        enable = true;
        return 0;
    }

    public int disable() {
        enable = false;
        return 0;
    }

    public int privateMsg(int subType, int msgId, long fromQQ, String msg, int font) {
        // 这里处理消息
        if (fromQQ == 1552409060L || fromQQ == 1442988390L) {
            if (msg.startsWith("!bc")) {
                String text = msg.replaceAll("!bc", "");
                CQ.sendGroupMsg(111852382L, text);
            }
        }
        return MSG_IGNORE;
    }

    public int groupMsg(int subType, int msgId, long fromGroup, long fromQQ, String fromAnonymous, String msg,
            int font) {

        // Solidot 推送转发
        if (fromGroup == 779672339L && botStatus) {
            if (subSolidot) {
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
            String cmd[] = new String[4];

            /**
             * @brief Processing msg into cmd & params
             * @param cmd[0] << cmd after !, e.g. "help" cmd[1] << first param etc.
             * @author Stiven.ding
             */

            cmd[1] = "";
            cmd[2] = ""; // at most 3 paras are supported
            cmd[3] = ""; // init objects, or null pointers appear

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
                switch (cmd[0]) { // 无视这里的报错，用 JDK >= 1.7 编译即可
                    // 帮助命令
                    case "help":
                        CQ.sendGroupMsg(fromGroup,
                                CC.at(fromQQ) + "\n= Nameless Bot 帮助 =" + "\n /version 查看版本号"
                                        + "\n /repeat [内容] (次数) 复读你要说的话" + "\n /(un)sub [频道] 订阅/退订指定媒体"
                                        + "\n /switch [on/off] 开/关机器人");
                        break;
                    // 版本命令
                    case "version":
                        CQ.sendGroupMsg(fromGroup, "版本号: 1.0.1-dev");
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
                            if (cmd[1].equals("on")) {
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
                            switch (cmd[1].toLowerCase()) { // 无视此处报错，仅需在 JDK >= 1.7 编译即可
                                case "solidot": // 代码中务必只用小写，确保大小写不敏感
                                    subSolidot = true;
                                    CQ.sendGroupMsg(fromGroup, "[Bot] 已订阅 Solidot.");
                                    break;
                                case "jikewakeup":
                                    subJikeWakeup = true;
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
                                    subSolidot = false;
                                    CQ.sendGroupMsg(fromGroup, "[Bot] 已退订 Solidot.");
                                    break;
                                case "jikewakeup":
                                    subJikeWakeup = false;
                                    CQ.sendGroupMsg(fromGroup, "[Bot] 已退订 即刻 - 一觉醒来世界发生了什么.");
                                    break;
                                default:
                                    CQ.sendGroupMsg(fromGroup, "[Bot] 未知频道.");
                                    break;
                            }
                        } else
                            CQ.sendGroupMsg(fromGroup, "[Bot] 你没有权限!");
                        break;
                    case "debug":
                        if (isAdmin) {
                            switch (cmd[1].toLowerCase()) {
                                case "jikepush":
                                    try {
                                        URL url = new URL(jikeWakeup);
                                        // 读取RSS源
                                        XmlReader reader = new XmlReader(url);
                                        SyndFeedInput input = new SyndFeedInput();
                                        // 得到SyndFeed对象，即得到RSS源里的所有信息
                                        SyndFeed feed = input.build(reader);
                                        // 得到Rss新闻中子项列表
                                        List entries = feed.getEntries();
                                        SyndEntry entry = (SyndEntry) entries.get(0);
                                        String value = entry.getDescription().getValue().replaceAll("<br />", "\n");
                                        CQ.sendGroupMsg(fromGroup, entry.getTitle() + "\n" + value);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        CQ.sendGroupMsg(fromGroup, "[Bot] 发生了意料之外的错误, 请查看后台.");
                                    }
                                    break;
                                default:
                                    CQ.sendGroupMsg(fromGroup, "[Bot] 命令不存在哟~");
                                    break;
                            }
                        } else
                            CQ.sendGroupMsg(fromGroup, "[Bot] 你没有权限!");
                        break;
                    // 禁言
                    case "mute":
                        if (isAdmin) {
                            if (cmd[1].equals("")) {
                                CQ.sendGroupMsg(fromGroup, "[Bot] 用法: /ban [at需要禁言的人/QQ号] [时间(秒)]");
                            } else if (StringUtils.isNumeric(cmd[1]) && StringUtils.isNumeric(cmd[2])) {
                                long banQQ = Integer.parseInt(cmd[1]);
                                long bantime = Integer.parseInt(cmd[2]);
                                if (bantime >= 60 || bantime <= 2592000) {
                                    CQ.setGroupBan(fromGroup, banQQ, bantime);
                                } else
                                    CQ.sendGroupMsg(fromGroup, "[Bot] 时间有误!");
                            } else if (StringUtils.isNumeric(cmd[2])) {
                                try {
                                    long times = Integer.parseInt(cmd[2]);
                                    long banQQ = CC.getAt(cmd[1]);
                                    if (times > 0 && times <= 2592000) {
                                        CQ.setGroupBan(fromGroup, banQQ, times);
                                    } else CQ.sendGroupMsg(fromGroup, "[Bot] 你输入的时间有误!");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    CQ.sendGroupMsg(fromGroup, "[Bot] 请检查你输入的命令!");
                                }
                            }
                            else if (cmd[2].contains("d")||cmd[2].contains("h")||cmd[2].contains("m")){
                                // TODO: 实现 D/H/M 转换
                            } else
                                CQ.sendGroupMsg(fromGroup, "[Bot] 请检查你输入的命令!");
                        } else
                            CQ.sendGroupMsg(fromGroup, "[Bot] 你没有权限!");
                        break;
                    // 解除禁言
                    case "unmute":
                        if (isAdmin) {
                            if (cmd[1].equals("")) {
                                CQ.sendGroupMsg(fromGroup, "[Bot] 用法: /unban [at需要解禁的人]");
                            } else if (!cmd[1].equals("")) {
                                long bannedQQ = CC.getAt(cmd[1]);
                                CQ.setGroupBan(fromGroup, bannedQQ, 0);
                            } else if (StringUtils.isNumeric(cmd[1])) {
                                long bannedQQ = Integer.parseInt(cmd[1]);
                                CQ.setGroupBan(fromGroup, bannedQQ, 0);
                            } else
                                CQ.sendGroupMsg(fromGroup, "[Bot] 请检查你输入的命令!");
                        } else
                            CQ.sendGroupMsg(fromGroup, "[Bot] 你没有权限!");
                        break;
                // 未知命令
                default:
                    CQ.sendGroupMsg(fromGroup, "[Bot] 命令不存在哟~");
                    break;

                }
            } else if (cmd[0].equals("switch") && cmd[1].equals("off")) {
                // 机器人禁言关闭
                if (isAdmin) {
                    CQ.sendGroupMsg(fromGroup, "[Bot] 已关闭对机器人的禁言.");
                    botStatus = true;
                }
            }
        }
        else if (subJikeWakeup && botStatus){
            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, 7); // 控制时
            c.set(Calendar.MINUTE, 0); // 控制分
            c.set(Calendar.SECOND, 0); // 控制秒

            Date time = c.getTime();
            Timer timer = new Timer();

            timer.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    try {
                        URL url = new URL(jikeWakeup);
                        // 读取RSS源
                        XmlReader reader = new XmlReader(url);
                        SyndFeedInput input = new SyndFeedInput();
                        // 得到SyndFeed对象，即得到RSS源里的所有信息
                        SyndFeed feed = input.build(reader);
                        // 得到Rss新闻中子项列表
                        List entries = feed.getEntries();
                        SyndEntry entry = (SyndEntry) entries.get(0);
                        String value = entry.getDescription().getValue().replaceAll("<br />", "\n");
                        CQ.sendGroupMsg(fromGroup, entry.getTitle() + "\n" + value);
                    } catch (Exception e) {
                        e.printStackTrace();
                        CQ.sendGroupMsg(fromGroup, "[Bot] 发生了意料之外的错误, 请查看后台.");
                    }
                }
            }, time, 1000 * 60 * 60 * 24); // 这里设定将延时每天固定执行
        }
        return MSG_IGNORE;
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

    public int groupMemberDecrease(int subtype, int sendTime, long fromGroup, long fromQQ, long beingOperateQQ) {
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
}
