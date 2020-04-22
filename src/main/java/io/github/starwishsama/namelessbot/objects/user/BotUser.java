package io.github.starwishsama.namelessbot.objects.user;

import cc.moecraft.icq.user.User;
import io.github.starwishsama.namelessbot.BotConstants;
import io.github.starwishsama.namelessbot.BotMain;
import io.github.starwishsama.namelessbot.enums.UserLevel;
import io.github.starwishsama.namelessbot.utils.BotUtils;
import lombok.Data;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Data
public class BotUser {
    private long userQQ;
    private UUID userUUID;
    private Calendar lastCheckInTime = Calendar.getInstance();
    private double checkInPoint;
    private int checkInTime = 0;
    private String bindServerAccount;
    private int msgVL;
    private String r6sAccount;
    private UserLevel level = UserLevel.USER;
    private int randomTime = 50;
    private Pet pet;
    private long checkInGroup;
    private List<Long> biliSubs = new LinkedList<>();

    public BotUser(long qq){
        userQQ = qq;
        userUUID = BotUtils.generateUuid();
    }

    public static boolean isUserExist(long qq) {
        return getUser(qq) != null;
    }

    public static boolean isBotAdmin(long id){
        return BotUtils.getLevel(id).ordinal() > 1;
    }

    public static boolean isBotOwner(long id){
        return BotUtils.getLevel(id) == UserLevel.OWNER || BotConstants.cfg.getOwnerID() == id;
    }

    public static boolean isBotOwner(User sender){
        return isBotOwner(sender.getId());
    }

    public static boolean isBotAdmin(User sender){
        return isBotAdmin(sender.getId());
    }

    public void decreaseTime(){
        if (level == UserLevel.USER) {
            this.randomTime--;
        }
    }

    public void decreaseTime(int time){
        if (level == UserLevel.USER) {
            for (int i = 0; i < time; i++) {
                this.randomTime--;
            }
        }
    }

    public void updateTime() {
        if (level == UserLevel.USER && randomTime < 20) {
            this.randomTime++;
        }
    }

    public void addPoint(double point) {
        checkInPoint = checkInPoint + point;
    }

    public void addTime(int time) {
        if (level == UserLevel.USER && randomTime < 20) {
            randomTime = randomTime + time;
        }
    }

    public void cost(double point){
        checkInPoint = checkInPoint - point;
    }

    public static BotUser quickRegister(long id){
        BotUser user = new BotUser(id);
        BotConstants.users.add(user);
        return user;
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
}
