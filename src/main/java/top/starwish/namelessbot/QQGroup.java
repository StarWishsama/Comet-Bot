package top.starwish.namelessbot;

public class QQGroup {
    private long groupID;
    private boolean autoAcceptJoinRequest;
    private String joinMsg;
    private String kickMsg;

    public boolean isAutoAcceptJoinRequest() {
        return autoAcceptJoinRequest;
    }

    public void setAutoAcceptJoinRequest(boolean autoAcceptJoinRequest) {
        this.autoAcceptJoinRequest = autoAcceptJoinRequest;
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

    public void setJoinMsg(){
        this.joinMsg = joinMsg;
    }

    public String getKickMsg(){
        return kickMsg;
    }

    public void setKickMsg(){
        this.kickMsg = kickMsg;
    }
}
