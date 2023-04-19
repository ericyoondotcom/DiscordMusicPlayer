import java.util.HashMap;

public class QueueManager {
    HashMap<String, GuildQueue> guildQueues = new HashMap<String, GuildQueue>();

    public QueueManager(){}

    public GuildQueue getOrCreateQueue(String guildId){
        if(!guildQueues.containsKey(guildId)) {
            guildQueues.put(guildId, new GuildQueue(guildId));
        }
        return guildQueues.get(guildId);
    }
}