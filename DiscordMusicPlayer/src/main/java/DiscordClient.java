import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class DiscordClient extends ListenerAdapter {
    JDA client;

    public DiscordClient() throws Exception {
        String token = Main.config.getString("discord.token");
        if(token == null){
            System.out.println("Error: no Discord token (key discord.token) provided in config.");
            return;
        }
        client = JDABuilder.createDefault(token)
            .addEventListeners(this)
            .build();
    }

    void registerSlashCommands(){
        client.upsertCommand("play", "Play a track.").queue();
    }

    @Override
    public void onReady(ReadyEvent event){
        System.out.println("Connected to Discord!");
        registerSlashCommands();
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event){

    }
}
