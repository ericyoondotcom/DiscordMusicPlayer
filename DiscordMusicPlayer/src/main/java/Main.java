import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;

import java.io.File;
import java.util.*;

public class Main {
    public static DiscordClient discord;
    public static APIManager apiManager;
    public static MusicPlayer musicPlayer;
    public static QueueManager queueManager;

    static Configurations configs = new Configurations();
    public static Configuration config;

    public static void main(String[] args) throws Exception {
        File configFile = new File("config.properties");
        if(configFile.createNewFile()) System.out.println("No configuration file found. One has been created at " + configFile.getCanonicalPath());
        config = configs.properties(configFile);

        queueManager = new QueueManager();
        discord = new DiscordClient();
        apiManager = new APIManager();
        musicPlayer = new MusicPlayer();
    }

}
