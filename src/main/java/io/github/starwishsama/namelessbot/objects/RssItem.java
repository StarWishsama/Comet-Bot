package io.github.starwishsama.namelessbot.objects;

import com.rometools.rome.feed.synd.*;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import lombok.Data;

import java.io.IOException;
import java.net.URL;
import java.util.*;

@Data
public class RssItem {
    private String address;
    private boolean ifEnabled;

    public RssItem(String address) {
        this.address = address;
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
            return feed.getEntries().get(0);
        } catch (FeedException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static SyndEntry getRSSItem(URL url){
        try {
            XmlReader reader = new XmlReader(url);
            SyndFeedInput input = new SyndFeedInput();
            // 得到SyndFeed对象，即得到RSS源里的所有信息
            SyndFeed feed = input.build(reader);
            // 得到RSS源中子项列表
            return feed.getEntries().get(0);
        } catch (FeedException | IOException e) {
            e.printStackTrace();
            return null;
        }
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

    public static List<SyndEntry> getRSSItems(URL url){
        try {
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