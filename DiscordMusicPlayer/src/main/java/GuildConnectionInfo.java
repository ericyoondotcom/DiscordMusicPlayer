import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.api.managers.AudioManager;

public class GuildConnectionInfo {
    AudioPlayer player;
    AudioManager discordAudioManager;
    String activeVCid;

    public GuildConnectionInfo(String activeVCid, AudioManager discordAudioManager, AudioPlayer player){
        this.activeVCid = activeVCid;
        this.discordAudioManager = discordAudioManager;
        this.player = player;
    }

    public AudioPlayer getPlayer() {
        return player;
    }
    public AudioManager getDiscordAudioManager() {
        return discordAudioManager;
    }
    public String getActiveVCid() {
        return activeVCid;
    }
}
