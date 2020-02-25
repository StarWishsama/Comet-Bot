package io.github.starwishsama.namelessbot.session.commands.vote;

import io.github.starwishsama.namelessbot.session.SessionUser;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class VoteUser extends SessionUser {
    private boolean voteStatus;

    public VoteUser(final long id, boolean voteStatus){
        super.setUserId(id);
        this.voteStatus = voteStatus;
    }
}
