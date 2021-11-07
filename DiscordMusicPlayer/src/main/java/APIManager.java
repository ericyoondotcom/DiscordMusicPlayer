import java.net.MalformedURLException;
import java.util.HashMap;
import java.net.URL;

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
    public void parseTrackString(String str, final String messageAuthorId, final ParseTrackStringHandler callback){
        String clean = str.trim();
        boolean isUrl;
        try { new URL(clean); isUrl = true; } catch(MalformedURLException e) { isUrl = false; }
        if(isUrl){
            callback.onTrackFound(new TrackInfo(clean, clean, messageAuthorId, null));
            return;
        }
        getService("youtube").searchForTrack(clean, new SearchResultHandler() {
            public void onResultFound(String trackURL, String trackName) {
                callback.onTrackFound(new TrackInfo(trackURL, trackName, messageAuthorId, null));
            }
            public void onNoResults() { callback.onNoSearchResults(); return; }
            public void onError(Exception e) {
                System.out.println(e.toString());
                callback.onError(e);
                return;
            }
        });
    }
}
