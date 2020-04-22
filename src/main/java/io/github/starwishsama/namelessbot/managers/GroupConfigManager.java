package io.github.starwishsama.namelessbot.managers;

import io.github.starwishsama.namelessbot.objects.group.GroupConfig;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

public class GroupConfigManager {
    @Setter
    @Getter
    private static Map<Long, GroupConfig> configMap = new LinkedHashMap<>();

    public static GroupConfig getConfig(Long groupId){
        return configMap.getOrDefault(groupId, null);
    }

    public static void addConfig(Long groupId, GroupConfig cfg) {
        if (!configMap.containsKey(groupId)) {
            configMap.put(groupId, cfg);
        }
    }

    public static void removeConfig(Long groupId) {
        if (!configMap.containsKey(groupId)) {
            configMap.remove(groupId);
        }
    }

    public static boolean isValidGroupConfig(Long groupId){
        return getConfig(groupId) != null;
    }
}
