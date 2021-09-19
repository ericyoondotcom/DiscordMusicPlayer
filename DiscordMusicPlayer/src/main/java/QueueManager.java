import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class QueueManager extends AudioEventAdapter {
    HashMap<String, ArrayList<TrackInfo>> queues = new HashMap<String, ArrayList<TrackInfo>>();

    public QueueManager(){

    }

    /**
     * Adds the guild's entry to the queues hashmap if it doesn't already exist.
     * @param guildId The ID of the guild.
     * @return Whether or not a queue was created in the hashmap.
     */
    public boolean createQueue(String guildId){
        if(queues.containsKey(guildId)) return false;
        queues.put(guildId, new ArrayList<TrackInfo>());
        return true;
    }

    public void addToQueue(TrackInfo track, VoiceChannel vc){
        String guildId = vc.getGuild().getId();
        createQueue(guildId);
        queues.get(guildId).add(track);
        if(!Main.musicPlayer.getIsPlaying(guildId)){
            startNextTrack(vc);
        }
    }

    public void startNextTrack(VoiceChannel vc){
        ArrayList<TrackInfo> queue = queues.get(vc.getGuild().getId());
        if(queue.size() == 0) return;

        Main.musicPlayer.playTrackFromURL(queue.get(0).url, vc, new TrackPlayHandler() {
            public void onTrackPlaySuccess() {
            }
            public void onFailure(String reason) {
            }
        });
    }

    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason){
        if(endReason == AudioTrackEndReason.FINISHED && endReason.mayStartNext){
//            startNextTrack();
//            TODO: need to find a way to look up which guild triggered the track end,
            // then look up the vc id for that guild
        }
    }
}