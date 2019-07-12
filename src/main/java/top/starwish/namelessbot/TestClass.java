package top.starwish.namelessbot;

import com.alibaba.fastjson.JSONObject;
import com.spotify.dns.DnsException;
import com.spotify.dns.DnsSrvResolver;
import com.spotify.dns.DnsSrvResolvers;
import com.spotify.dns.LookupResult;
import com.spotify.dns.statistics.DnsReporter;
import com.spotify.dns.statistics.DnsTimingContext;
import top.starwish.namelessbot.entity.RssItem;
import top.starwish.namelessbot.utils.BotUtils;
import top.starwish.namelessbot.utils.FileProcess;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.CacheRequest;
import java.util.List;


// 此 Class 仅供调试使用
public class TestClass {
    static  RssItem solidot = new RssItem("https://www.solidot.org/index.rss");
    static RssItem jikeWakeUp = new RssItem();
    static String msg = "!mute 1448839220 1h10m";
    static boolean isAdmin = true;
    static boolean botStatus = true;
    static long fromGroup = 0;
    static String configPath= System.getProperty("user.dir") + "\\build\\myfile.json";



    public static void main(String[] args) {
        List<LookupResult> nodes = BotUtils.getSRVRecords("_minecraft._tcp.anyaddress.com");
        for (LookupResult node : nodes){
            String url = node.host();
            int port = node.port();
            System.out.println("host=" + url + "\nport=" + port);
        }
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