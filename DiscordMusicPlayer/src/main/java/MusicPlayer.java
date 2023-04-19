import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.HashMap;

public class MusicPlayer {
    HashMap<String, GuildConnectionInfo> guilds = new HashMap<String, GuildConnectionInfo>();
    public AudioPlayerManager playerManager;

    public MusicPlayer(){
        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    public GuildConnectionInfo getGuild(String guildId) { return guilds.get(guildId); }
    public boolean getIsConnected(String guildId) {
        GuildConnectionInfo guild = guilds.get(guildId);
        if(guild == null) return false;
        return guild.getDiscordAudioManager().isConnected();
    }

    public boolean connect(AudioChannel vc, GuildQueue guildQueue){
        AudioManager discordAudioManager = vc.getGuild().getAudioManager();
        if(!discordAudioManager.isConnected()){
            discordAudioManager.openAudioConnection(vc);
            AudioPlayer player = this.playerManager.createPlayer();
            player.addListener(guildQueue);
            guilds.put(vc.getGuild().getId(), new GuildConnectionInfo(vc.getId(), discordAudioManager, player));
            discordAudioManager.setSendingHandler(new AudioPlayerSendHandler(player));
            return true;
        }
        return false;
    }

    public boolean disconnect(String guildId){
        if(!getIsConnected(guildId)) return false;
        getGuild(guildId).getDiscordAudioManager().closeAudioConnection();
        return true;
    }

    public boolean playTrack(AudioTrack track, String guildId){
        if(!getIsConnected(guildId)) return false;
        return getGuild(guildId).getPlayer().startTrack(track.makeClone(), false);
    }

    public boolean stopTrack(String guildId) {
        if(!getIsConnected(guildId)) return false;
        getGuild(guildId).getPlayer().stopTrack();
        return true;
    }
    public boolean setPaused(boolean paused, String guildId){
        if(!getIsConnected(guildId)) return false;
        getGuild(guildId).getPlayer().setPaused(paused);
        return true;
    }
    public boolean getIsPaused(String guildId) {
        if(!getIsConnected(guildId)) return false;
        return getGuild(guildId).getPlayer().isPaused();
    }
    public boolean getIsTrackPlaying(String guildId) {
        if(!getIsConnected(guildId)) return false;
        return getGuild(guildId).getPlayer().getPlayingTrack() != null;
    }

    public void loadTrack(String url, final TrackLoadHandler callback){
        playerManager.loadItemOrdered(this, url, new AudioLoadResultHandler() {
            public void trackLoaded(AudioTrack audioTrack) {
                callback.onTrackLoaded(audioTrack);
            }
            public void playlistLoaded(AudioPlaylist playlist) {
                callback.onPlaylistLoaded(playlist);
            }
            public void noMatches() {
                callback.onFailure(Strings.URL_NOT_FOUND_ERROR);
            }
            public void loadFailed(FriendlyException exception) {
                callback.onFailure(Strings.LOAD_FAILED_ERROR);
            }
        });
    }

    public void loadAndPlayTrackFromURL(String url, final String guildId, final TrackPlayHandler callback){
        loadTrack(url, new TrackLoadHandler() {
            public void onTrackLoaded(AudioTrack track) {
                playTrack(track, guildId);
                System.out.println(track.getInfo().title);
                callback.onTrackPlaySuccess();
            }
            public void onPlaylistLoaded(AudioPlaylist playlist){

            }
            public void onFailure(String reason){
                callback.onFailure(reason);
            }
        });
    }
}
