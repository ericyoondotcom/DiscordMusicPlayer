import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.awt.*;
import java.util.ArrayList;

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

    public void addToQueue(TrackInfo track){
        queue.add(track);
        if(!Main.musicPlayer.getIsTrackPlaying(guildId)){
            startNextTrack();
        }
    }

    public void addAtIndex(TrackInfo track, int index){
        queue.add(index, track);
        if(!Main.musicPlayer.getIsTrackPlaying(guildId)){
            startNextTrack();
        }
    }

    public int queueLength() { return queue.size(); }

    public void startNextTrack(){
        if(queueLength() == 0) return;

        Main.musicPlayer.playTrackFromURL(queue.get(0).url, guildId, new TrackPlayHandler() {
            public void onTrackPlaySuccess() {
                nowPlaying = queue.remove(0);
            }
            public void onFailure(String reason) {
                queue.remove(0);
            }
        });
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
            StringBuilder fieldBuilder = new StringBuilder()
                    .append("[")
                    .append(nowPlaying.name)
                    .append("](")
                    .append(nowPlaying.url)
                    .append(") | `Requested by: ")
                    .append(addedBy.getEffectiveName())
                    .append(" (")
                    .append(addedBy.getUser().getName())
                    .append("#")
                    .append(addedBy.getUser().getDiscriminator())
                    .append(")`");
            builder.addField(new MessageEmbed.Field("Now Playing", fieldBuilder.toString(), false));
        }
        if(queueLength() > 0){
            StringBuilder fieldBuilder = new StringBuilder();
            for(int i = 0; i < queueLength(); i++){
                TrackInfo track = queue.get(i);
                Member addedBy = guild.getMemberById(track.addedByID);
                if(i != 0) fieldBuilder.append("\n");
                fieldBuilder
                        .append("`")
                        .append(i)
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
            builder.addField(new MessageEmbed.Field("Up Next", fieldBuilder.toString(), false));
        } else {
            builder.setDescription("The queue is empty! Add some tracks using `/play`.");
        }
        return builder.build();
    }
}
