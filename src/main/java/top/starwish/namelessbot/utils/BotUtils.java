package top.starwish.namelessbot.utils;

import com.spotify.dns.DnsSrvResolvers;
import me.dilley.MineStat;

import com.spotify.dns.DnsException;
import com.spotify.dns.DnsSrvResolver;
import com.spotify.dns.LookupResult;
import com.spotify.dns.statistics.DnsReporter;
import com.spotify.dns.statistics.DnsTimingContext;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class BotUtils {
    private static final DnsReporter REPORTER = new StdoutReporter();

    /**
     * @param string 需要去除彩色符号的字符串
     * @return 去除彩色符号的字符串
     */
    public static String removeColor(String string){
        return string.replaceAll("§\\S", "");
    }

    /**
     * 获取 Minecraft 服务器信息
     * @author NamelessSAMA
     * @param addr
     * @param port
     * @return serverStatus
     */
    public static String getServerInfo(String addr, int port){
        MineStat server = new MineStat(addr, port);
        if (server.isServerUp()) {
            return ("在线玩家: " + server.getCurrentPlayers() + "/" + server.getMaximumPlayers()
                    + "\n延迟:" + server.getLatency() + "ms"
                    + "\nMOTD: " + server.getMotd()
                    + "\n版本: " + server.getVersion());
        } else
            return ("[Bot] 无法连接至 " + addr);
    }

    /**
     * 获取 Minecraft 服务器信息 (自定义消息样式)
     * @param addr 服务器IP
     * @param port 端口
     * @param msg 自定义消息
     * @return 服务器状态
     */
    public static String getCustomServerInfo(String addr, int port, String msg){
        MineStat server = new MineStat(addr, port);
        if (server.isServerUp()) {
            return msg.replaceAll("%延迟%", server.getLatency() + "")
                    .replaceAll("%在线玩家%", server.getCurrentPlayers())
                    .replaceAll("%换行%", "\n")
                    .replaceAll("%最大玩家%", server.getMaximumPlayers())
                    .replaceAll("%MOTD%", server.getMotd())
                    .replaceAll("%版本%", server.getVersion());
        } else
            return "[Bot] 无法连接至服务器.";
    }

    /**
     * 判断是否签到过了
     *
     * @author NamelessSAMA
     * @param currentTime 当前时间
     * @param compareTime 需要比较的时间
     * @return true/false
     */
    public static boolean isCheckInReset(Date currentTime, Date compareTime){
        SimpleDateFormat sdt = new SimpleDateFormat("yyyy-MM-dd");
        return !sdt.format(currentTime).equals(sdt.format(compareTime));
    }

    /**
     * https://github.com/spotify/dns-java/
     * @param url
     * @return List<LookupResult>
     * @author Spotify
     */

    public static List<LookupResult> getSRVRecords(String url) {
        if (url != null) {
            DnsSrvResolver resolver = DnsSrvResolvers.newBuilder()
                    .cachingLookups(true)
                    .retainingDataOnFailures(true)
                    .metered(REPORTER)
                    .dnsLookupTimeoutMillis(1000)
                    .build();
            try {
                return resolver.resolve(url);
            } catch (DnsException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static class StdoutReporter implements DnsReporter {
        @Override
        public DnsTimingContext resolveTimer() {
            return new DnsTimingContext() {
                private final long start = System.currentTimeMillis();

                @Override
                public void stop() {
                    final long now = System.currentTimeMillis();
                    final long diff = now - start;
                    System.out.println("请求耗费了 " + diff + "ms");
                }
            };
        }

        @Override
        public void reportFailure(Throwable error) {
            System.err.println("在解析 " + error + " 时发生了错误");
            error.printStackTrace(System.err);
        }

        @Override
        public void reportEmpty() {
            System.out.println("Empty response from server.");
        }
    }

    public static String StringHelper(String s){
        if (s.endsWith(".")){
            return s.substring(0, s.length() - 1);
        }
        return s;
    }
}

