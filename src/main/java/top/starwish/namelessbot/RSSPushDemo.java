package top.starwish.namelessbot;

import com.rometools.rome.feed.synd.*;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RSSPushDemo {

    public static void main(String[] args) {
        process();
    }

    public static void process() {
        String jikeDaily = "https://rsshub.app/jike/daily";
        String jikeWakeup = "https://rsshub.app/jike/topic/text/553870e8e4b0cafb0a1bef68";
        String jikeTechNews = "https://rsshub.app/jike/topic/597ae4ac096cde0012cf6c06";

        List<String> l = new ArrayList<String>();
        l.add(jikeDaily);
        l.add(jikeWakeup);
        l.add(jikeTechNews);

            try {
                URL url = new URL(l.get(2));
                // 读取Rss源
                XmlReader reader = new XmlReader(url);
                SyndFeedInput input = new SyndFeedInput();
                // 得到SyndFeed对象，即得到Rss源里的所有信息
                SyndFeed feed = input.build(reader);
                // 得到Rss新闻中子项列表
                List entries = feed.getEntries();
                SyndEntry entry = (SyndEntry) entries.get(0);
                String value = entry.getDescription().getValue().replaceAll("<br/>","\n");
                System.out.println(entry.getTitle() + "\n" + value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
