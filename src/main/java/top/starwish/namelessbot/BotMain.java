package top.starwish.namelessbot;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.sobte.cqp.jcq.entity.*;
import com.sobte.cqp.jcq.event.JcqAppAbstract;

import javax.swing.*;
import java.net.URL;
import java.util.List;

public class BotMain extends JcqAppAbstract implements ICQVer, IMsg, IRequest {
    public boolean botStatus = true;
    public boolean subSolidot = true;

    String jikeWakeup = "https://rsshub.app/jike/topic/text/553870e8e4b0cafb0a1bef68";

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
        // String appDirectory = CQ.getAppDirectory();
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

    /**
     * 私聊消息 (Type=21)<br>
     * 本方法会在酷Q【线程】中被调用。<br>
     *
     * @param subType 子类型，11/来自好友 1/来自在线状态 2/来自群 3/来自讨论组
     * @param msgId   消息ID
     * @param fromQQ  来源QQ
     * @param msg     消息内容
     * @param font    字体
     * @return 返回值*不能*直接返回文本 如果要回复消息，请调用api发送<br>
     *         这里 返回 {@link IMsg#MSG_INTERCEPT MSG_INTERCEPT} - 截断本条消息，不再继续处理<br>
     *         注意：应用优先级设置为"最高"(10000)时，不得使用本返回值<br>
     *         如果不回复消息，交由之后的应用/过滤器处理，这里 返回 {@link IMsg#MSG_IGNORE MSG_IGNORE} -
     *         忽略本条消息
     */
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

    /**
     * 群消息 (Type=2)<br>
     * 本方法会在酷Q【线程】中被调用。<br>
     *
     * @param subType       子类型，目前固定为1
     * @param msgId         消息ID
     * @param fromGroup     来源群号
     * @param fromQQ        来源QQ号
     * @param fromAnonymous 来源匿名者
     * @param msg           消息内容
     * @param font          字体
     * @return 关于返回值说明, 见 {@link #privateMsg 私聊消息} 的方法
     */
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
                                    + "\n /mute [on/off] 开/关机器人");
                    break;
                // 版本命令
                case "version":
                    CQ.sendGroupMsg(fromGroup, "版本号: 1.0.0-RELEASE");
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
                case "mute":
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
                        default:
                            CQ.sendGroupMsg(fromGroup, "[Bot] 未知频道.");
                            break;
                        }
                    } else
                        CQ.sendGroupMsg(fromGroup, "[Bot] 你没有权限!");
                    break;
                case "jikedaily":
                    if (isAdmin) {
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
                            System.out.println(entry.getTitle() + "\n" + value);
                        } catch (Exception e) {
                            e.printStackTrace();
                            CQ.sendGroupMsg(fromGroup, "[Bot] 发生了意料之外的错误, 请查看后台.");
                        }
                    } else
                        CQ.sendGroupMsg(fromGroup, "[Bot] 你没有权限!");
                    break;
                    // 禁言
                    case "ban":
                        if (isAdmin) {
                            if (cmd[1].equals("")) {
                                CQ.sendGroupMsg(fromGroup, "[Bot] 用法: /ban [at需要禁言的人] [时间(秒)]");
                            } else {
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
                        } else
                            CQ.sendGroupMsg(fromGroup, "[Bot] 你没有权限!");
                        break;
                    // 解除禁言
                    case "unban":
                        if (isAdmin) {
                            if (cmd[1].equals("")) {
                                CQ.sendGroupMsg(fromGroup, "[Bot] 用法: /unban [at需要解禁的人]");
                            } else {
                                long bannedQQ = CC.getAt(cmd[1]);
                                CQ.setGroupBan(fromGroup, bannedQQ, 0);
                            }
                        } else
                            CQ.sendGroupMsg(fromGroup, "[Bot] 你没有权限!");
                        break;
                // 未知命令
                default:
                    CQ.sendGroupMsg(fromGroup, "[Bot] 命令不存在哟~");
                    break;

                }
            } else if (cmd[0].equals("mute") && cmd[1].equals("off")) {
                // 机器人禁言关闭
                if (isAdmin) {
                    CQ.sendGroupMsg(fromGroup, "[Bot] 已关闭对机器人的禁言.");
                    botStatus = true;
                }
            }
        }

        // 这里处理消息
        return MSG_IGNORE;
    }

    /**
     * 讨论组消息 (Type=4)<br>
     * 本方法会在酷Q【线程】中被调用。<br>
     *
     * @param subtype     子类型，目前固定为1
     * @param msgId       消息ID
     * @param fromDiscuss 来源讨论组
     * @param fromQQ      来源QQ号
     * @param msg         消息内容
     * @param font        字体
     * @return 关于返回值说明, 见 {@link #privateMsg 私聊消息} 的方法
     */
    public int discussMsg(int subtype, int msgId, long fromDiscuss, long fromQQ, String msg, int font) {
        // 这里处理消息

        return MSG_IGNORE;
    }

    /**
     * 群文件上传事件 (Type=11)<br>
     * 本方法会在酷Q【线程】中被调用。<br>
     *
     * @param subType   子类型，目前固定为1
     * @param sendTime  发送时间(时间戳)// 10位时间戳
     * @param fromGroup 来源群号
     * @param fromQQ    来源QQ号
     * @param file      上传文件信息
     * @return 关于返回值说明, 见 {@link #privateMsg 私聊消息} 的方法
     */
    public int groupUpload(int subType, int sendTime, long fromGroup, long fromQQ, String file) {
        GroupFile groupFile = CQ.getGroupFile(file);
        if (groupFile == null) { // 解析群文件信息，如果失败直接忽略该消息
            return MSG_IGNORE;
        }
        // 这里处理消息
        return MSG_IGNORE;
    }

    /**
     * 群事件-管理员变动 (Type=101)<br>
     * 本方法会在酷Q【线程】中被调用。<br>
     *
     * @param subtype        子类型，1/被取消管理员 2/被设置管理员
     * @param sendTime       发送时间(时间戳)
     * @param fromGroup      来源群号
     * @param beingOperateQQ 被操作QQ
     * @return 关于返回值说明, 见 {@link #privateMsg 私聊消息} 的方法
     */
    public int groupAdmin(int subtype, int sendTime, long fromGroup, long beingOperateQQ) {
        // 这里处理消息

        return MSG_IGNORE;
    }

    /**
     * 群事件-群成员减少 (Type=102)<br>
     * 本方法会在酷Q【线程】中被调用。<br>
     *
     * @param subtype        子类型，1/群员离开 2/群员被踢
     * @param sendTime       发送时间(时间戳)
     * @param fromGroup      来源群号
     * @param fromQQ         操作者QQ(仅子类型为2时存在)
     * @param beingOperateQQ 被操作QQ
     * @return 关于返回值说明, 见 {@link #privateMsg 私聊消息} 的方法
     */
    public int groupMemberDecrease(int subtype, int sendTime, long fromGroup, long fromQQ, long beingOperateQQ) {
        // 这里处理消息

        return MSG_IGNORE;
    }

    /**
     * 群事件-群成员增加 (Type=103)<br>
     * 本方法会在酷Q【线程】中被调用。<br>
     *
     * @param subtype        子类型，1/管理员已同意 2/管理员邀请
     * @param sendTime       发送时间(时间戳)
     * @param fromGroup      来源群号
     * @param fromQQ         操作者QQ(即管理员QQ)
     * @param beingOperateQQ 被操作QQ(即加群的QQ)
     * @return 关于返回值说明, 见 {@link #privateMsg 私聊消息} 的方法
     */
    public int groupMemberIncrease(int subtype, int sendTime, long fromGroup, long fromQQ, long beingOperateQQ) {
        // 这里处理消息
        if (botStatus) {
            if (fromGroup == 111852382L) {
                CQ.sendGroupMsg(fromGroup, "欢迎 " + CC.at(beingOperateQQ)
                        + "加入时光隧道!\n【进群请修改群名片为游戏ID】\n【建议使用群文件中的官方客户端!】\n\n服务器IP地址:bgp.sgsd.pw:25846\n赞助网址:http://www.mcrmb.com/cz/13153");
            }
        }
        return MSG_IGNORE;
    }

    /**
     * 好友事件-好友已添加 (Type=201)<br>
     * 本方法会在酷Q【线程】中被调用。<br>
     *
     * @param subtype  子类型，目前固定为1
     * @param sendTime 发送时间(时间戳)
     * @param fromQQ   来源QQ
     * @return 关于返回值说明, 见 {@link #privateMsg 私聊消息} 的方法
     */
    public int friendAdd(int subtype, int sendTime, long fromQQ) {
        // 这里处理消息

        return MSG_IGNORE;
    }

    /**
     * 请求-好友添加 (Type=301)<br>
     * 本方法会在酷Q【线程】中被调用。<br>
     *
     * @param subtype      子类型，目前固定为1
     * @param sendTime     发送时间(时间戳)
     * @param fromQQ       来源QQ
     * @param msg          附言
     * @param responseFlag 反馈标识(处理请求用)
     * @return 关于返回值说明, 见 {@link #privateMsg 私聊消息} 的方法
     */
    public int requestAddFriend(int subtype, int sendTime, long fromQQ, String msg, String responseFlag) {
        // 这里处理消息

        /**
         * REQUEST_ADOPT 通过 REQUEST_REFUSE 拒绝
         */

        // CQ.setFriendAddRequest(responseFlag, REQUEST_ADOPT, null); // 同意好友添加请求
        return MSG_IGNORE;
    }

    /**
     * 请求-群添加 (Type=302)<br>
     * 本方法会在酷Q【线程】中被调用。<br>
     *
     * @param subtype      子类型，1/他人申请入群 2/自己(即登录号)受邀入群
     * @param sendTime     发送时间(时间戳)
     * @param fromGroup    来源群号
     * @param fromQQ       来源QQ
     * @param msg          附言
     * @param responseFlag 反馈标识(处理请求用)
     * @return 关于返回值说明, 见 {@link #privateMsg 私聊消息} 的方法
     */
    public int requestAddGroup(int subtype, int sendTime, long fromGroup, long fromQQ, String msg,
            String responseFlag) {
        // 这里处理消息

        /**
         * REQUEST_ADOPT 通过 REQUEST_REFUSE 拒绝 REQUEST_GROUP_ADD 群添加 REQUEST_GROUP_INVITE
         * 群邀请
         */
        /*
         * if(subtype == 1){ // 本号为群管理，判断是否为他人申请入群 CQ.setGroupAddRequest(responseFlag,
         * REQUEST_GROUP_ADD, REQUEST_ADOPT, null);// 同意入群 } if(subtype == 2){
         * CQ.setGroupAddRequest(responseFlag, REQUEST_GROUP_INVITE, REQUEST_ADOPT,
         * null);// 同意进受邀群 }
         */

        return MSG_IGNORE;
    }

    /**
     * 本函数会在JCQ【线程】中被调用。
     *
     * @return 固定返回0
     */
    public int menu() {
        JOptionPane.showMessageDialog(null, "暂时没有菜单哟");
        return 0;
    }

}
