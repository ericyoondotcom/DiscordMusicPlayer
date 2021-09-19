import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public interface TrackLoadHandler {
    void onTrackLoaded(AudioTrack track);
    void onFailure(String reason);
}
