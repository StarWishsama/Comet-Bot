package top.starwish.namelessbot;

import com.alibaba.fastjson.JSONObject;
import top.starwish.namelessbot.entity.RssItem;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;


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
        if (Calendar.getInstance().get(Calendar.SECOND) == 10)
            System.out.println("SolidotPush: TestMsg");
        if (botStatus){
            String temppath = "C:\\Users\\Nameless\\Desktop\\solidottemp.txt";
            String temptitle = "";
            File solidottemp = new File(temppath);

            if (!solidottemp.exists()){
                FileProcess.createFile(temppath, solidot.getTitle());
            }
            else {
                try {
                    temptitle = FileProcess.readFile(temppath);
                } catch (IOException e){
                    FileProcess.createFile(temppath, solidot.getTitle());
                    e.printStackTrace();
                }
                String title = solidot.getTitle();
                if (!temptitle.equals("") && !temptitle.equals(title)){
                    String context = solidot.getContext() + "\nSolidot 推送\nPowered by NamelessBot";
                    FileProcess.createFile(temppath, solidot.getTitle());
                    System.out.println(context);
                }
            }
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