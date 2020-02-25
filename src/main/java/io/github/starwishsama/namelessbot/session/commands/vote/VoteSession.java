package io.github.starwishsama.namelessbot.session.commands.vote;

import io.github.starwishsama.namelessbot.session.Session;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.LinkedList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class VoteSession extends Session {
    private final String voteContext;
    private long id;
    private int positiveVote;
    private int negativeVote;
    private String customPosVote = "要";
    private String customNegVote = "不要";
    private final long createdTime = System.currentTimeMillis();
    private List<VoteUser> voteUsers = new LinkedList<>();
    private long expireTime = 60 * 1000;

    public VoteSession(final String voteContext, final long id, final long group){
        super.setGroupId(group);
        this.voteContext = voteContext;
        this.id = id;
    }

    public VoteSession(final String voteContext, final long id, final long group, String customPosVote, String customNegVote){
        this.voteContext = voteContext;
        this.id = id;
        super.setGroupId(group);
        this.customPosVote = customPosVote;
        this.customNegVote = customNegVote;
    }

    public void addPositiveVote(){
        this.positiveVote++;
    }

    public void addNegativeVote(){
        this.negativeVote++;
    }

    public void addVoteUser(long id){
        voteUsers.add(new VoteUser(id, true));
    }

    public boolean isVoted(long id){
        return getVoteUser(id) != null;
    }

    public VoteUser getVoteUser(long id){
        for (VoteUser user : voteUsers){
            if (user.getUserId() == id){
                return user;
            }
        }
        return null;
    }
}
