package top.starwish.namelessbot.entity;

import me.dilley.MineStat;

public class MCServer {
    private String serverIP;
    private int serverPort;
    private String infoMessage;
    private boolean isEnabled;

    public MCServer(){}
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
    public boolean isEnabled(){
        return isEnabled;
    }
    public void setEnabled(boolean isEnabled){
        this.isEnabled = isEnabled;
    }
}
