package io.github.starwishsama.namelessbot.objects.user;

import cc.moecraft.icq.sender.returndata.returnpojo.get.RGroupMemberInfo;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

@Data
public class ClockInData {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<RGroupMemberInfo> list = new LinkedList<>();
    private List<RGroupMemberInfo> checkedUser = new LinkedList<>();
    private List<RGroupMemberInfo> lateUser = new LinkedList<>();
}
