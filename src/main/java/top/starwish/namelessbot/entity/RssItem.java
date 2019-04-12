package top.starwish.namelessbot.entity;

import com.rometools.rome.feed.synd.*;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import java.net.URL;
import java.util.*;

public class RssItem {
    private String address;
    private boolean ifEnabled;

    // main 函数仅供调试使用
    public static void main(String[] args) {
        String URL = "http://api.lssdjt.com/?ContentType=xml&appkey=rss.xml";
        String text = new RssItem(URL).getContext();
        text = "CC.face(74)" + "各位时光隧道玩家早上好" + "\n今天是" + Calendar.getInstance().get(Calendar.YEAR) + "年"
        + (Calendar.getInstance().get(Calendar.MONTH) + 1) + "月"
        + Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + "日" + "，"
        + text.substring(0, text.indexOf("\n")).replaceFirst("-", "的今天是")
        + "的日子，一小时之后我会推送今天的早间新闻\n新的一天开始了！" + "CC.face(190)" + "今天别忘了去服务器领取签到奖励噢~~";
        System.out.println(text);
    }

    public RssItem() {
    }

    public RssItem(String addr) {
        address = addr;
    }

    public boolean getStatus() {
        return ifEnabled;
    }

    public String getAddress() {
        return address;
    }

    public void disable() {
        ifEnabled = false;
    }

    public void enable() {
        ifEnabled = true;
    }

    public void setStatus(boolean stat) {
        ifEnabled = stat;
    }

    public void setAddress(String addr) {
        address = addr;
    }

    public String getContext() {
        return (simplifyHTML(getFromURL(address)));
    }

    public String getTitle() {
        return (simplifyHTML(getTitleFromURL(address)));
    }

    // 此函数仅供内部调用，正常情况下不应调用
    private static String getFromURL(String addr) {
        try {
            URL url = new URL(addr);
            // 读取RSS源
            XmlReader reader = new XmlReader(url);
            SyndFeedInput input = new SyndFeedInput();
            // 得到SyndFeed对象，即得到RSS源里的所有信息
            SyndFeed feed = input.build(reader);
            // 得到Rss新闻中子项列表
            List<SyndEntry> entries = feed.getEntries();
            SyndEntry entry = (SyndEntry) entries.get(0);
            return (entry.getTitle() + "\n" + "-------------------------\n" + entry.getDescription().getValue().trim());
        } catch (Exception e) {
            e.printStackTrace();
            return ("Encountered a wrong URL or a network error.");
        }
    }

    private static String getTitleFromURL(String addr){
        try {
            URL url = new URL(addr);
            XmlReader reader = new XmlReader(url);
            SyndFeedInput input = new SyndFeedInput();
            // 得到SyndFeed对象，即得到RSS源里的所有信息
            SyndFeed feed = input.build(reader);
            // 得到Rss新闻中子项列表
            List<SyndEntry> entries = feed.getEntries();
            SyndEntry entry = entries.get(0);
            return entry.getTitle();
        } catch (Exception e){
                e.printStackTrace();
            return ("Encountered a wrong URL or a network error.");
        }
    }

    private static String simplifyHTML(String context) {
        context = context.replaceAll("<br />", "\n").replaceAll("<br>", "\n").replaceAll("</p><p>", "\n")
                .replaceAll("	", "");
        while (context.indexOf('<') != -1) {
            int l = context.indexOf('<');
            int r = context.indexOf('>');
            context = context.substring(0, l) + context.substring(r + 1);
        }
        while (context.indexOf("\n\n") != -1) {
            context = context.replaceAll("\n\n", "\n");
        }
        return context;
    }
}