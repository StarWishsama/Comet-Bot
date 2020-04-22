package io.github.starwishsama.namelessbot.objects.draws;

import lombok.Data;

import java.util.List;

@Data
public class ArkNightOperator {
    /**
     * name : 能天使
     * desc : 优先攻击空中单位
     * rare : 6
     * prof : 狙击
     * tagList : ["输出","远程位"]
     * obtain : ["公开招募","标准寻访"]
     * birth : 拉特兰
     * camp : 企鹅物流
     * team : 企鹅物流
     * race : ["萨科塔"]
     * build : ["贸易站"]
     */

    private String name;
    private String desc;
    private int rare;
    private String prof;
    private String birth;
    private String camp;
    private String team;
    private List<String> tagList;
    private List<String> obtain;
    private List<String> race;
    private List<String> build;
}
