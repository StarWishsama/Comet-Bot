package top.starwish.namelessbot;

import static com.sobte.cqp.jcq.event.JcqApp.CQ;

public class QQGroup {
    private long groupID;
    private boolean autoAcceptJoinRequest;
    private String joinMsg;
    private String kickMsg;
    private boolean isAdmin;

    public QQGroup() {
    }
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

    public boolean isAdmin(long fromGroup, long fromQQ){
        if (CQ.getGroupMemberInfoV2(fromGroup, fromQQ).getAuthority() > 1){
            return true;
        }
        return false;
    }
}
