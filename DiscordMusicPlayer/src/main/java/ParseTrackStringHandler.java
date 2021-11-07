public interface ParseTrackStringHandler {
    void onTrackFound(TrackInfo track);
    void onNoSearchResults();
    void onError(Exception e);
}
