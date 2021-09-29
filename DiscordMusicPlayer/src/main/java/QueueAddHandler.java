public interface QueueAddHandler {
    public void onTrackLoadSuccess(TrackInfo info);
    public void onPlaylistLoadSuccess(TrackInfo[] tracks, String playlistName);
    public void onFailure(String reason);
}
