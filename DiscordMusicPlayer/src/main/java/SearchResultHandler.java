public interface SearchResultHandler {
    void onResultFound(String trackURL, String trackName);
    void onNoResults();
    void onError(Exception e);
}
