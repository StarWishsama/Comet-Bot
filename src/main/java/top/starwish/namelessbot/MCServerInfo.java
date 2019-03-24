package top.starwish.namelessbot;

import me.dilley.MineStat;

public class MCServerInfo {
    private String serverIP;
    private int serverPort;

    public MCServerInfo(){
    }

    public MCServerInfo(String addr, int port){
        serverIP = addr;
        serverPort = port;
    }

    public String getServerIP(){
        return serverIP;
    }
    public void setServerIP(String serverIP){
        this.serverIP = serverIP;
    }
    public int getServerPort(){
        return serverPort;
    }
    public void setServerPort(int serverPort){
        this.serverPort = serverPort;
    }
    public String getServerInfo(){
        return ServerInfo(serverIP, serverPort);
    }

    private static String ServerInfo(String addr, int port){
        MineStat server = new MineStat(addr, port);
        if (server.isServerUp()) {
            return ("在线玩家: " + server.getCurrentPlayers() + "/" + server.getMaximumPlayers()
                    + "\n延迟:" + server.getLatency() + "ms"
                    + "\nMOTD: " + server.getMotd()
                    + "\n版本: " + server.getVersion());
        } else
            return ("[Bot] Couldn't connect to " + addr);
    }
}
