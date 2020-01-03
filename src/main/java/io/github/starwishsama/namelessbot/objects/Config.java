package io.github.starwishsama.namelessbot.objects;

import com.google.gson.annotations.SerializedName;
import io.github.starwishsama.namelessbot.commands.MusicCommand;
import lombok.Data;

import java.util.List;

@Data
public class Config {
    @SerializedName("owner_id")
    private long ownerID;
    @SerializedName("auto_save_time")
    private int autoSaveTime = 15;
    @SerializedName("bot_admins")
    private List<Long> botAdmins;
    @SerializedName("post_port")
    private int postPort = 5700;
    @SerializedName("post_url")
    private String postUrl = "127.0.0.1";
    @SerializedName("bot_name")
    private String botName = "Bot";
    @SerializedName("bot_port")
    private int botPort = 5703;
    @SerializedName("rcon_url")
    private String rconUrl;
    @SerializedName("rcon_port")
    private int rconPort;
    @SerializedName("rcon_password")
    private byte[] rconPwd;
    @SerializedName("netease_api_url")
    private String netEaseApi;
    @SerializedName("cmd_prefix")
    private String[] cmdPrefix = new String[]{"/", "#", "!"};
    @SerializedName("bind_minecraft_account")
    private boolean bindMCAccount = false;
    @SerializedName("anti_spam")
    private boolean antiSpam = false;
    @SerializedName("spam_mute_time")
    private int spamMuteTime = 60;
    @SerializedName("cool_down_time")
    private int coolDownTime = 15;
    @SerializedName("filter_words")
    private List<String> filterWords;
    @SerializedName("default_music_api")
    private MusicCommand.MusicType api = MusicCommand.MusicType.QQ;
}
