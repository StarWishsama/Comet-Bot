package top.starwish.namelessbot;

import com.rometools.rome.feed.synd.*;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import java.net.URL;
import java.util.List;

public class RssItem {
    private String address = "";
    private boolean ifEnabled = true;
    
    // main 函数仅供调试使用
    public static void main(String[] args) {
        System.out.println(getFromURL("https://rsshub.app/jike/topic/text/553870e8e4b0cafb0a1bef68"));
    }

    public RssItem() {
        ifEnabled = true;
    }

    public RssItem(String addr) {
        address = addr;
        ifEnabled = true;
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

    public void setAddress(String addr) {
        address = addr;
    }

    public String getContext(){
        return(getFromURL(address));
    }

    // 此函数仅供内部调用，正常情况下不应调用
    private static String getFromURL(String addr){
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
            String value = entry.getDescription().getValue().replaceAll("<br />", "\n");
            return(entry.getTitle() + "\n" + value);
        } catch (Exception e) {
            e.printStackTrace();
            return("error");
        }
    }
    
}