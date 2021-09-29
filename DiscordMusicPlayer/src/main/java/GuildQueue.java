import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class GuildQueue extends AudioEventAdapter {
    TrackInfo nowPlaying = null;
    ArrayList<TrackInfo> queue = new ArrayList<TrackInfo>();
    String guildId;

    public GuildQueue(String guildId){
        this.guildId = guildId;
    }

    public boolean connect(VoiceChannel vc){
        return Main.musicPlayer.connect(vc, this);
    }

    public void addToQueue(final TrackInfo track, final QueueAddHandler handler){
        addAtIndex(track, queue.size(), handler);
    }

    public void addAtIndex(final TrackInfo track, final int index, final QueueAddHandler handler){
        Main.musicPlayer.loadTrack(track.url, new TrackLoadHandler() {
            public void onTrackLoaded(AudioTrack audioTrack) {
                queue.add(index, track);
                track.name = audioTrack.getInfo().title;
                track.loadedAudioTrack = audioTrack;
                if(!Main.musicPlayer.getIsTrackPlaying(guildId)){
                    startNextTrack();
                }
                handler.onTrackLoadSuccess(track);
            }
            public void onPlaylistLoaded(AudioPlaylist playlist){
                int i = 0;
                TrackInfo[] ret = new TrackInfo[playlist.getTracks().size()];
                for(AudioTrack audioTrack : playlist.getTracks()){
                    TrackInfo newTrack = new TrackInfo(audioTrack.getInfo().uri, audioTrack.getInfo().title, track.addedByID, audioTrack);
                    ret[i] = newTrack;
                    queue.add(index + i, newTrack);
                    i++;
                }
                if(!Main.musicPlayer.getIsTrackPlaying(guildId)){
                    startNextTrack();
                }
                handler.onPlaylistLoadSuccess(ret, playlist.getName());
            }
            public void onFailure(String reason){
                handler.onFailure(reason);
            }
        });

    }

    public int queueLength() { return queue.size(); }

    public void clearQueue(){
        queue.clear();
    }

    public void shuffleQueue(){
        Collections.shuffle(queue, new Random());
    }

    public void startNextTrack(){
        if(queueLength() == 0) return;
        Main.musicPlayer.playTrack(queue.get(0).loadedAudioTrack, guildId);
        nowPlaying = queue.remove(0);
    }

    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason){
        nowPlaying = null;
        if(endReason == AudioTrackEndReason.FINISHED && endReason.mayStartNext){
            startNextTrack();
        }
    }

    public MessageEmbed displayAsEmbed(){
        Guild guild = Main.discord.client.getGuildById(guildId);
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Queue for " + guild.getName()).setColor(Color.CYAN);
        if(nowPlaying != null) {
            Member addedBy = guild.getMemberById(nowPlaying.addedByID);
            builder.getDescriptionBuilder()
                    .append("**Now Playing**\n[")
                    .append(nowPlaying.name)
                    .append("](")
                    .append(nowPlaying.url)
                    .append(") | `Requested by: ")
                    .append(addedBy.getEffectiveName())
                    .append(" (")
                    .append(addedBy.getUser().getName())
                    .append("#")
                    .append(addedBy.getUser().getDiscriminator())
                    .append(")`\n\n");
        }
        if(queueLength() > 0){
            // TODO: Pagination
            for(int i = 0; i < Math.min(10, queueLength()); i++){
                TrackInfo track = queue.get(i);
                Member addedBy = guild.getMemberById(track.addedByID);
                builder.getDescriptionBuilder()
                        .append(i == 0 ? "**Queue**\n" : "\n")
                        .append("`")
                        .append(i + 1)
                        .append(".` ")
                        .append("[")
                        .append(track.name)
                        .append("](")
                        .append(track.url)
                        .append(") | `Requested by: ")
                        .append(addedBy.getEffectiveName())
                        .append(" (")
                        .append(addedBy.getUser().getName())
                        .append("#")
                        .append(addedBy.getUser().getDiscriminator())
                        .append(")`");
            }
        } else {
            builder.setDescription("The queue is empty! Add some tracks using `/play`.");
        }
        return builder.build();
    }
}
