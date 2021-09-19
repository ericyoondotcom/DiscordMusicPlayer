import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

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
        String debugGuildId = Main.config.getString("discord.commands_debug_guild");
        CommandListUpdateAction updateAction;
        if(debugGuildId != null){
            Guild debugGuild = client.getGuildById(debugGuildId);
            updateAction = debugGuild.updateCommands();
        } else {
            updateAction = client.updateCommands();
        }
        updateAction.addCommands(new CommandData("play", "Add a track to the end of the queue.").addOption(OptionType.STRING, "track", "The track to play", true));
        updateAction.addCommands(new CommandData("playtop", "Add a track to the beginning of the queue.").addOption(OptionType.STRING, "track", "The track to play", true));
        updateAction.addCommands(new CommandData("skip", "Skips the next track(s) in the queue.").addOption(OptionType.INTEGER, "count", "How many tracks to skip.", false));
        updateAction.addCommands(new CommandData("pause", "Pause playback."));
        updateAction.addCommands(new CommandData("resume", "Resume playback."));
        updateAction.queue();
    }

    @Override
    public void onReady(ReadyEvent event){
        System.out.println("Connected to Discord!");
        registerSlashCommands();
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event){
        if(event.getName().equals("play")){
            event.deferReply().queue();
            // TODO: after operation, event.getHook().sendMessage("blah").queue();
        } else if(event.getName().equals("playtop")){
            // TODO
        } else if(event.getName().equals("skip")){
            // TODO
        } else if(event.getName().equals("pause")){
            // TODO
        } else if(event.getName().equals("resume")){
            // TODO
        } else {
            System.out.println("Unrecognized command: " + event.getName());
        }
    }
}
