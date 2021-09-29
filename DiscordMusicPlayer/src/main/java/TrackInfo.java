import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public class TrackInfo {
    public String name;
    public String url;
    public String addedByID;
    public AudioTrack loadedAudioTrack;
    public TrackInfo(String url, String name, String addedByID, AudioTrack loadedAudioTrack){
        this.url = url;
        this.name = name;
        this.addedByID = addedByID;
        this.loadedAudioTrack = loadedAudioTrack;
    }
}
