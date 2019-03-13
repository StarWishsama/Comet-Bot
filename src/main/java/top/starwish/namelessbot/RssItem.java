package top.starwish.namelessbot;

import com.rometools.rome.feed.synd.*;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import java.net.URL;
import java.util.List;

public class RssItem {
    private String address;
    private boolean ifEnabled;

    // main 函数仅供调试使用
    public static void main(String[] args) {
        String URL = "https://www.solidot.org/index.rss";
        System.out.println(new RssItem(URL).getContext());
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