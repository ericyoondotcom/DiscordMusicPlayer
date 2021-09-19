import java.util.HashMap;

public class APIManager {
    HashMap<String, APIService> apiServices = new HashMap<String, APIService>();

    public APIService getService(String name) { return apiServices.get(name); }

    public APIManager() {
        apiServices.put("youtube", new YoutubeAPI());
    }
}
