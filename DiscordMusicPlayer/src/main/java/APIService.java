import java.net.URL;

public interface APIService {
    public void searchForTrack(String query, SearchResultHandler callback);
}
