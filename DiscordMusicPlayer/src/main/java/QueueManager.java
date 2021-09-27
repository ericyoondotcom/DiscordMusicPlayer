import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.util.HashMap;

public class QueueManager {
    HashMap<String, GuildQueue> guildQueues = new HashMap<String, GuildQueue>();

    public QueueManager(){}

    public GuildQueue getOrCreateQueue(String guildId){
        if(!guildQueues.containsKey(guildId)) {
            guildQueues.put(guildId, new GuildQueue(guildId));
        }
        return guildQueues.get(guildId);
    }
}