package top.starwish.namelessbot.entity;

import me.dilley.MineStat;

public class MCServer {
    private String serverIP;
    private int serverPort;
    private String infoMessage;
    private boolean isEnabled;

    public MCServer(String addr, int port){
        serverIP = addr;
        serverPort = port;
    }

    public MCServer(String addr, int port, String msg){
        serverIP = addr;
        serverPort = port;
        infoMessage = msg;
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

    public String getInfoMessage(){
        return infoMessage;
    }
    public void setInfoMessage(String infoMessage){
        this.infoMessage = infoMessage;
    }
    public boolean ifEnabled(){
        return isEnabled;
    }
    public void setEnabled(boolean isEnabled){
        this.isEnabled = isEnabled;
    }

    public String getServerInfo(){
        return ServerInfo(serverIP, serverPort);
    }

    public String getCustomServerInfo(){
        return CustomServerInfo(serverIP, serverPort, infoMessage);
    }

    /**
     * 获取 Minecraft 服务器信息
     * @author NamelessSAMA
     * @param addr
     * @param port
     * @return
     */
    private static String ServerInfo(String addr, int port){
        MineStat server = new MineStat(addr, port);
        if (server.isServerUp()) {
            return ("在线玩家: " + server.getCurrentPlayers() + "/" + server.getMaximumPlayers()
                    + "\n延迟:" + server.getLatency() + "ms"
                    + "\nMOTD: " + server.getMotd()
                    + "\n版本: " + server.getVersion());
        } else
            return ("[Bot] 无法连接至 " + addr);
    }

    private static String CustomServerInfo(String addr, int port, String msg){
        MineStat server = new MineStat(addr, port);
        if (server.isServerUp()) {
            return msg;
        } else
            return "[Bot] 无法连接至服务器.";
    }
}
