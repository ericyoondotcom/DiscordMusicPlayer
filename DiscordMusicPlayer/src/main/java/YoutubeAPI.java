import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class YoutubeAPI implements APIService {
    YouTube youtube;
    HttpTransport httpTransport;
    JsonFactory jsonFactory;
    public YoutubeAPI(){
        httpTransport = new NetHttpTransport();
        jsonFactory = GsonFactory.getDefaultInstance();
        youtube = new YouTube.Builder(httpTransport, jsonFactory, new HttpRequestInitializer() {
            public void initialize(HttpRequest httpRequest) throws IOException {
            }
        }).setApplicationName("discord-music-player").build();
    }
    public void searchForTrack(String query, SearchResultHandler callback){
        try {
            YouTube.Search.List search = youtube.search().list("id,snippet");
            search.setKey(Main.config.getString("google.api_key"));
            search.setQ(query);
            search.setType("video");
            search.setFields("items(id/kind,id/videoId,snippet/title)");
            search.setMaxResults(1L);
            SearchListResponse response = search.execute();
            List<SearchResult> results = response.getItems();
            if(results == null || results.size() == 0) {
                callback.onNoResults();
                return;
            }
            ArrayList<SearchResult> searchResultList = new ArrayList<SearchResult>(results);
            SearchResult best = searchResultList.get(0);
            callback.onResultFound("https://youtube.com/watch?v=" + best.getId().getVideoId(), best.getSnippet().getTitle());
        } catch(Exception e){
            callback.onError(e);
        }

    }
}
