package top.starwish.namelessbot;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sobte.cqp.jcq.entity.CoolQ;
import com.sobte.cqp.jcq.event.JcqAppAbstract;


// 此 Class 仅供调试使用
public class TestClass {
    static RssItem solidot = new RssItem();
    static RssItem jikeWakeUp = new RssItem();
    static String msg = "!mute 1448839220 1h10m";
    static boolean isAdmin = true;
    static boolean botStatus = false;
    static long fromGroup = 0;
    static String configPath= System.getProperty("user.dir") + "\\build\\myfile.json";


    public static void main(String[] args) {
    }

    
    public static void saveConf() {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("botStatus", botStatus);
        jsonObject.put("solidot", solidot.getStatus());
        jsonObject.put("jikeWakeUp", jikeWakeUp.getStatus());
        if (!FileProcess.createFile(configPath, jsonObject.toJSONString()))
            System.out.println("[ERROR] "+ "Encountered an error when saving configuration!");

    }
}