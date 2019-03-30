package top.starwish.namelessbot;

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
}
