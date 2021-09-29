import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public interface TrackLoadHandler {
    void onTrackLoaded(AudioTrack track);
    void onPlaylistLoaded(AudioPlaylist playlist);
    void onFailure(String reason);
}
