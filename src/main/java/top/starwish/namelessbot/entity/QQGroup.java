package top.starwish.namelessbot.entity;

import java.util.List;

public class QQGroup {
    private long groupID;
    private boolean autoAcceptRequest;
    private String joinMsg;
    private String leaveMsg;
    private String kickMsg;
    private List<Long> groupBotAdmins;
    private List<String> groupAliases;
    private String serverIP;
    private int serverPort = 0;
    private String infoMessage;
    private String RConAddress;
    private int RConPort;
    private byte[] RConPwd;

    public QQGroup() {
    }

    public boolean isAutoAcceptRequest() {
        return autoAcceptRequest;
    }

    public void setAutoAcceptRequest(boolean autoAcceptRequest) {
        this.autoAcceptRequest = autoAcceptRequest;
    }

    public void setGroupID(long groupID) {
        this.groupID = groupID;
    }

    public long getGroupID() {
        return groupID;
    }

    public String getJoinMsg() {
        return joinMsg;
    }

    public void setJoinMsg(String joinMsg){
        this.joinMsg = joinMsg;
    }

    public String getKickMsg(){
        return kickMsg;
    }

    public void setKickMsg(String kickMsg){
        this.kickMsg = kickMsg;
    }

    public String getLeaveMsg(){
        return leaveMsg;
    }

    public void setLeaveMsg(String msg){
        leaveMsg = msg;
    }

    public void setGroupBotAdmins(List<Long> admins) {
        groupBotAdmins = admins;
    }

    public List<Long> getGroupBotAdmins(){
        return groupBotAdmins;
    }

    public void setGroupAliases(List<String> aliases){
        groupAliases = aliases;
    }

    public List<String> getGroupAliases(){
        return groupAliases;
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

    public void setRConAddress(String addr){
        RConAddress = addr;
    }

    public String getRConAddress(){
        return RConAddress;
    }

    public void setRConPort(int port){
        RConPort = port;
    }

    public int getRConPort(){
        return RConPort;
    }

    public void setRConPwd(String pwd){
        RConPwd = pwd.getBytes();
    }

    public byte[] getRConPwd(){
        return RConPwd;
    }
}
