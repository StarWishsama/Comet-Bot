package io.github.starwishsama.namelessbot.objects.dtos;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.Data;

import java.io.IOException;
import java.net.URL;
import java.util.List;

@Data
public class RssItem {
    private String address;
    private boolean ifEnabled;
    private List<Long> subscribers;

    public RssItem(String address){
        this.address = address;
    }

    public static void main(String[] args){
    }

    public String getContext() {
        return (simplifyHTML(getFromURL(address)));
    }

    public String getTitle() {
        return (simplifyHTML(getTitleFromURL(address)));
    }

    public SyndEntry getEntry(){
        return getEntryFromURL(address);
    }

    // 此函数仅供内部调用，正常情况下不应调用
    private static String getFromURL(String address) {
        try {
            URL url = new URL(address);
            // 读取RSS源
            XmlReader reader = new XmlReader(url);
            SyndFeedInput input = new SyndFeedInput();
            // 得到SyndFeed对象，即得到RSS源里的所有信息
            SyndFeed feed = input.build(reader);
            // 得到Rss新闻中子项列表
            List<SyndEntry> entries = feed.getEntries();
            SyndEntry entry = entries.get(0);
            return (entry.getTitle() + "\n" + "-------------------------\n" + entry.getDescription().getValue().trim());
        } catch (Exception e) {
            e.printStackTrace();
            return "Encountered a wrong URL or a network error.";
        }
    }

    private static SyndEntry getEntryFromURL(String address) {
        try {
            URL url = new URL(address);
            // 读取RSS源
            XmlReader reader = new XmlReader(url);
            SyndFeedInput input = new SyndFeedInput();
            // 得到SyndFeed对象，即得到RSS源里的所有信息
            SyndFeed feed = input.build(reader);
            // 得到Rss新闻中子项
            return feed.getEntries().get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getTitleFromURL(String address){
        try {
            URL url = new URL(address);
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

    public static String simplifyHTML(String context) {
        context = context.replaceAll("<br />", "\n").replaceAll("<br>", "\n").replaceAll("</p><p>", "\n")
                .replaceAll("	", "");
        while (context.indexOf('<') != -1) {
            int l = context.indexOf('<');
            int r = context.indexOf('>');
            context = context.substring(0, l) + context.substring(r + 1);
        }
        while (context.contains("\n\n")) {
            context = context.replaceAll("\n\n", "\n");
        }
        return context;
    }

    public static SyndEntry getRSSItem(String address){
        try {
            URL url = new URL(address);
            XmlReader reader = new XmlReader(url);
            SyndFeedInput input = new SyndFeedInput();
            // 得到SyndFeed对象，即得到RSS源里的所有信息
            SyndFeed feed = input.build(reader);
            // 得到RSS源中子项列表
            if (feed != null)
                return feed.getEntries().get(0);
        } catch (FeedException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<SyndEntry> getRSSItems(String address){
        try {
            URL url = new URL(address);
            XmlReader reader = new XmlReader(url);
            SyndFeedInput input = new SyndFeedInput();
            // 得到SyndFeed对象，即得到RSS源里的所有信息
            SyndFeed feed = input.build(reader);
            // 得到RSS源中子项列表
            return feed.getEntries();
        } catch (FeedException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}