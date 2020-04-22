package io.github.starwishsama.namelessbot.objects.group;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
public class GroupConfig {
    @SerializedName("group_id")
    private Long groupId;
    @SerializedName("auto_accept")
    private boolean autoAccept;
    private List<Long> admins = new LinkedList<>();
    @SerializedName("mc_server_info")
    private boolean mcServerInfo;
    @SerializedName("mc_server_address")
    private String mcServerAddress;

    public GroupConfig(Long id){
        groupId = id;
    }
}
