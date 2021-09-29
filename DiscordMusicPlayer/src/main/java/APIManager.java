import java.util.HashMap;

public class APIManager {
    HashMap<String, APIService> apiServices = new HashMap<String, APIService>();

    public APIService getService(String name) { return apiServices.get(name); }

    public APIManager() {
        apiServices.put("youtube", new YoutubeAPI());
    }

    /**
     * Parses a human-inputted track string.
     * @param str The track string provided by the user.
     * @return The information of the track, or null if no track was found.
     */
    public TrackInfo parseTrackString(String str, String messageAuthorId){
        // TODO
        return new TrackInfo(str, str, messageAuthorId, null);
    }
}
