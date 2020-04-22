package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.GroupCommand;
import cc.moecraft.icq.event.events.message.EventGroupMessage;
import cc.moecraft.icq.sender.returndata.returnpojo.get.RGroupMemberInfo;
import cc.moecraft.icq.user.Group;
import cc.moecraft.icq.user.GroupUser;
import cc.moecraft.utils.ArrayUtils;
import cc.moecraft.utils.StringUtils;
import io.github.starwishsama.namelessbot.BotConstants;
import io.github.starwishsama.namelessbot.enums.UserLevel;
import io.github.starwishsama.namelessbot.listeners.SendMessageListener;
import io.github.starwishsama.namelessbot.objects.user.BotUser;
import io.github.starwishsama.namelessbot.objects.user.ClockInData;
import io.github.starwishsama.namelessbot.utils.BotUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Nameless
 */
public class AdminCommand implements GroupCommand {
    @Override
    public String groupMessage(EventGroupMessage event, GroupUser sender, Group group, String command, ArrayList<String> args) {
        if (BotUser.isBotAdmin(sender.getId())){
            if (args.size() > 0) {
                switch (args.get(0)) {
                    case "set":
                        if (args.size() == 2) {
                            if (BotUser.isBotOwner(sender.getId()) && args.get(1) != null){
                                long qq = StringUtils.isNumeric(args.get(1)) ? Long.parseLong(args.get(1)) : BotUtils.parseAt(args.get(1));
                                if (qq != -1000L) {
                                    BotUser user = BotUser.getUser(sender.getId());
                                    if (user == null){
                                        user = new BotUser(sender.getId());
                                        BotConstants.users.add(user);
                                    } else {
                                        if (user.getLevel().ordinal() > UserLevel.VIP.ordinal()){
                                            user.setLevel(UserLevel.USER);
                                        } else {
                                            user.setLevel(UserLevel.ADMIN);
                                        }
                                    }
                                } else {
                                    return BotUtils.getLocalMessage("msg.bot-prefix") + "请检查QQ号是否正确!";
                                }
                            }
                        }
                        break;
                    case "filter":
                        if (args.size() > 1){
                            BotConstants.cfg.getFilterWords().add(ArrayUtils.getTheRestArgsAsString(args, 1));
                            return BotUtils.getLocalMessage("msg.bot-prefix") + "添加屏蔽词成功";
                        }
                        break;
                    case "help":
                        return "Nothing here now";
                    case "musicapi":
                        if (args.size() == 2){
                            switch (args.get(1).toLowerCase()) {
                                case "网易":
                                case "wy":
                                case "netease":
                                    BotConstants.cfg.setApi(MusicCommand.MusicType.NETEASE);
                                    return BotUtils.getLocalMessage("msg.bot-prefix") + "音乐 API 已设置为网易";
                                case "qq":
                                    BotConstants.cfg.setApi(MusicCommand.MusicType.QQ);
                                    return BotUtils.getLocalMessage("msg.bot-prefix") + "音乐 API 已设置为 QQ";
                                default:
                                    return BotUtils.getLocalMessage("msg.bot-prefix") + "/debug setapi [QQ/网易]";
                            }
                        } else
                            return BotUtils.getLocalMessage("msg.bot-prefix") + "/debug setapi [音乐API]";
                    case "upgrade":
                        BotUser user;
                        if (args.size() == 1) {
                            user = BotUser.getUser(sender.getId());
                            if (user != null) {
                                if (BotConstants.cfg.getBotAdmins().contains(sender.getId())) {
                                    user.setLevel(UserLevel.ADMIN);
                                    return BotUtils.sendLocalMessage("msg.bot-prefix", "完成");
                                } else if (BotConstants.cfg.getOwnerID() == sender.getId()) {
                                    user.setLevel(UserLevel.OWNER);
                                    return BotUtils.sendLocalMessage("msg.bot-prefix", "完成");
                                }
                            } else
                                return BotUtils.getLocalMessage("msg.bot-prefix") + BotUtils.getLocalMessage("checkin.first-time");
                        } else if (args.size() == 2){
                            long id = StringUtils.isNumeric(args.get(1)) ? Integer.parseInt(args.get(1)) : BotUtils.parseAt(args.get(1));
                            user = BotUser.getUser(id);
                            if (user != null){
                                user.setLevel(UserLevel.VIP);
                            } else {
                                return BotUtils.sendLocalMessage("msg.bot-prefix", "此用户未注册");
                            }
                        }
                        break;
                    case "switch":
                        SendMessageListener.botSwitch = !SendMessageListener.botSwitch;
                        break;
                    case "data":
                        List<RGroupMemberInfo> infoList = getUncheckUser(event.getGroupId(),event);
                        StringBuilder sb = new StringBuilder();
                        if (!infoList.isEmpty()){
                            for (RGroupMemberInfo info : infoList) {
                                String displayName = info.getCard().isEmpty() ? event.getHttpApi().getStrangerInfo(info.getUserId()).getData().getNickname() : info.getCard();
                                sb.append(displayName).append(" ");
                            }
                        }

                        String message = "该群未签到人数: " + infoList.size() + "\n未签到: " + sb.toString().trim();

                        return BotUtils.getLocalMessage("msg.bot-prefix") + message;
                    case "start":
                    case "打卡":
                        if (!BotConstants.data.containsKey(event.getGroupId())) {
                            if (args.size() == 3) {
                                ClockInData data = new ClockInData();
                                data.setList(event.getHttpApi().getGroupMemberList(event.getGroupId()).getData());
                                LocalDateTime createTime = LocalDateTime.now();
                                LocalDateTime startTime = LocalDateTime.of(createTime.toLocalDate(), LocalTime.parse(args.get(1), DateTimeFormatter.ofPattern("HH:mm")));
                                LocalDateTime endTime = LocalDateTime.of(createTime.toLocalDate(), LocalTime.parse(args.get(2), DateTimeFormatter.ofPattern("HH:mm")));
                                if (startTime.isAfter(endTime) || createTime.isAfter(startTime)) {
                                    return BotUtils.getLocalMessage("msg.bot-prefix") + "在吗 为什么时空穿越";
                                } else {
                                    data.setStartTime(startTime);
                                    data.setEndTime(endTime);
                                    BotConstants.data.put(event.getGroupId(), data);
                                }
                            } else if (args.size() == 2) {
                                ClockInData data = new ClockInData();
                                data.setList(event.getHttpApi().getGroupMemberList(event.getGroupId()).getData());
                                LocalDateTime startTime = LocalDateTime.now();
                                LocalDateTime endTime = LocalDateTime.of(LocalDate.now(), LocalTime.parse(args.get(1), DateTimeFormatter.ofPattern("HH:mm")));
                                if (endTime.isBefore(startTime)) {
                                    return BotUtils.getLocalMessage("msg.bot-prefix") + "在吗 为什么时空穿越";
                                } else {
                                    data.setStartTime(startTime);
                                    data.setEndTime(endTime);
                                    BotConstants.data.put(event.getGroupId(), data);
                                    return BotUtils.getLocalMessage("msg.bot-prefix") + "打卡已开启, 请使用 #dk 进行打卡";
                                }
                            } else {
                                return BotUtils.getLocalMessage("msg.bot-prefix") + "/admin start <开始时间> [结束时间]";
                            }
                        } else {
                            return BotUtils.getLocalMessage("msg.bot-prefix") + "本群还有正在进行的打卡!";
                        }
                    case "stop":
                    case "关闭打卡":
                        if (BotConstants.data.containsKey(event.getGroupId())) {
                            ClockInData data = BotConstants.data.get(event.getGroupId());
                            List<RGroupMemberInfo> allMembers = data.getList();
                            List<RGroupMemberInfo> checkedUsers = data.getCheckedUser();
                            List<RGroupMemberInfo> lateUsers = data.getLateUser();

                            allMembers.removeAll(checkedUsers);

                            StringBuilder uncheck = new StringBuilder();
                            for (RGroupMemberInfo i : allMembers){
                                if (i.getUserId() != event.getBotAccount().getId() || !(i.getCard() == null ? i.getNickname() : i.getCard()).contains("老师")) {
                                    uncheck.append(i.getCard() == null ? i.getNickname() : i.getCard()).append(" ");
                                }
                            }

                            StringBuilder late = new StringBuilder();
                            for (RGroupMemberInfo i : lateUsers){
                                late.append(i.getCard() == null ? i.getNickname() : i.getCard()).append(" ");
                            }

                            String temp1 = uncheck.toString();
                            String temp2 = late.toString();
                            BotConstants.data.remove(event.getGroupId());
                            return BotUtils.getLocalMessage("msg.bot-prefix") + "打卡已结束!\n已签到人数: " + checkedUsers.size()
                                    + "\n未签到: " + (temp1.trim().isEmpty() ? "无" : temp1.trim())
                                    + "\n迟到: " + (temp2.trim().isEmpty() ? "无" : temp2.trim());
                        } else {
                            return BotUtils.getLocalMessage("msg.bot-prefix") + "本群没有正在进行的打卡!";
                        }
                    default:
                        return null;
                }
            } else {
                return BotUtils.getLocalMessage("msg.bot-prefix") + "/admin help";
            }
        }
        return null;
    }

    @Override
    public CommandProperties properties() {
        return new CommandProperties("admin", "管理");
    }

    private List<RGroupMemberInfo> getUncheckUser(long groupId, EventGroupMessage event){
        List<RGroupMemberInfo> info = event.getHttpApi().getGroupMemberList(groupId).data;
        List<RGroupMemberInfo> result = new ArrayList<>();

        if (info != null && !info.isEmpty()){
            for (RGroupMemberInfo i: info){
                BotUser user = BotUser.getUser(i.getUserId());
                if (user != null && !BotUtils.isChecked(user)){
                    result.add(i);
                }
            }
        }
        return result;
    }
}
