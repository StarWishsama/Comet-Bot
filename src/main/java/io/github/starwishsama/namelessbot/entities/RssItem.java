package io.github.starwishsama.namelessbot.entities;

import com.rometools.rome.feed.synd.*;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import java.net.URL;
import java.util.*;

public class RssItem {
    private String address;
    private boolean ifEnabled;
    private List<Long> subscribers;

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

    public List<Long> getSubscribers(){
        return subscribers;
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

    public void setSubscribers(List<Long> subscribers) {
        this.subscribers = subscribers;
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
            SyndEntry entry = entries.get(0);
            return ("回形针 PaperClip 发布了新视频!\n标题:" + entry.getTitle() + "\n简介:" + entry.getDescription().getValue().trim() + "\n链接:" + entry.getLink());
        } catch (Exception e) {
            e.printStackTrace();
            return "Encountered a wrong URL or a network error.";
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
        while (context.contains("\n\n")) {
            context = context.replaceAll("\n\n", "\n");
        }
        return context;
    }
}