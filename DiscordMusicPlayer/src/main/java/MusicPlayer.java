import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.HashMap;

public class MusicPlayer {
    HashMap<String, AudioPlayer> players = new HashMap<String, AudioPlayer>();
    public AudioPlayerManager playerManager;

    public MusicPlayer(){
        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    public AudioPlayer getPlayerForVC(VoiceChannel vc) { return players.get(vc.getGuild().getId()); }

    public boolean connect(VoiceChannel vc){
        AudioManager discordAudioManager = vc.getGuild().getAudioManager();
        if(!discordAudioManager.isConnected()){
            discordAudioManager.openAudioConnection(vc);
            AudioPlayer player = this.playerManager.createPlayer();
            player.addListener(Main.queueManager);
            players.put(vc.getGuild().getId(), player);
            discordAudioManager.setSendingHandler(new AudioPlayerSendHandler(player));
            return true;
        }
        return false;
    }

    public boolean playTrack(AudioTrack track, VoiceChannel vc){
        connect(vc);
        return getPlayerForVC(vc).startTrack(track.makeClone(), false);
    }

    public void stopTrack(VoiceChannel vc){
        getPlayerForVC(vc).stopTrack();
    }

    public void setPaused(boolean paused, VoiceChannel vc){
        getPlayerForVC(vc).setPaused(paused);
    }
    public boolean getIsPaused(String guildId) { return players.get(guildId).isPaused(); }

    public boolean getIsPlaying(String guildId) { return players.get(guildId) != null && players.get(guildId).getPlayingTrack() != null; }

    public void loadTrack(String url, final TrackLoadHandler callback){
        playerManager.loadItemOrdered(this, url, new AudioLoadResultHandler() {
            public void trackLoaded(AudioTrack audioTrack) {
                callback.onTrackLoaded(audioTrack);
            }
            public void playlistLoaded(AudioPlaylist playlist) {
                callback.onFailure(Strings.PLAYLIST_NOT_SUPPORTED_ERROR);
            }
            public void noMatches() {
                callback.onFailure(Strings.URL_NOT_FOUND_ERROR);
            }
            public void loadFailed(FriendlyException exception) {
                callback.onFailure(Strings.LOAD_FAILED_ERROR);
            }
        });
    }

    public void playTrackFromURL(String url, final VoiceChannel vc, final TrackPlayHandler callback){
        loadTrack(url, new TrackLoadHandler() {
            public void onTrackLoaded(AudioTrack track) {
                playTrack(track, vc);
                callback.onTrackPlaySuccess();
            }
            public void onFailure(String reason){
                callback.onFailure(reason);
            }
        });
    }
}
