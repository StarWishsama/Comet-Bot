package io.github.starwishsama.namelessbot.objects;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.text.NumberFormat;

@Data
@AllArgsConstructor
public class RandomResult {
    private long id;
    private double chance;
    private String eventName;

    private enum EventRate {
        HIGHEST("大吉"),
        HIGH("中吉"),
        NORMAL("小吉"),
        LOW("末吉"),
        LOWEST("凶"),
        NEVER("大凶");

        private String event;

        EventRate(String event) {
            this.event = event;
        }
    }

    public static String getChance(RandomResult result){
        if (result != null) {
            NumberFormat nf = NumberFormat.getPercentInstance();
            nf.setMaximumIntegerDigits(3);
            nf.setMinimumFractionDigits(2);
            double chance = result.getChance();
            return getChance(result.getEventName(), chance);
        }
        return null;
    }

    public static String getChance(String eventName, double chance){
        NumberFormat nf = NumberFormat.getPercentInstance();
        nf.setMaximumIntegerDigits(3);
        nf.setMinimumFractionDigits(2);
        String finalRate = nf.format(chance);
        if (chance > 0.8 && chance <= 1.0){
            return "结果是" + EventRate.HIGHEST.event + " (" + finalRate + "), 今天非常适合" + eventName + "!";
        } else if (chance > 0.6 && chance <= 0.8){
            return "结果是" + EventRate.HIGH.event + " (" + finalRate + "), 今天很适合" + eventName + "!";
        } else if (chance > 0.5 && chance <= 0.6){
            return "结果是" + EventRate.NORMAL.event + " (" + finalRate + "), 今天适合" + eventName + "!";
        } else if (chance > 0.3 && chance <= 0.5){
            return "结果是" + EventRate.LOW.event + " (" + finalRate + "), 今天不太适合" + eventName + "...";
        } else if (chance > 0.1 && chance <= 0.3){
            return "结果是" + EventRate.LOWEST.event + " (" + finalRate + "), 今天最好不要" + eventName + "...";
        } else if (chance <= 0.1){
            return "结果是" + EventRate.NEVER.event + " (" + finalRate + "), 千万别" + eventName + "!";
        } else {
            return "你要占卜的东西有点怪呢, 我无法占卜出结果哦.";
        }
    }
}
