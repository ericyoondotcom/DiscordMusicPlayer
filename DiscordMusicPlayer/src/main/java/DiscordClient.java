import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
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
        updateAction.addCommands(new CommandData("join", "Connect to your voice channel."));
        updateAction.addCommands(new CommandData("play", "Add a track to the end of the queue.").addOption(OptionType.STRING, "track", "The track to play", true));
        updateAction.addCommands(new CommandData("playtop", "Add a track to the beginning of the queue.").addOption(OptionType.STRING, "track", "The track to play", true));
        updateAction.addCommands(new CommandData("skip", "Skips the next track(s) in the queue.").addOption(OptionType.INTEGER, "count", "How many tracks to skip.", false));
        updateAction.addCommands(new CommandData("pause", "Pause playback."));
        updateAction.addCommands(new CommandData("resume", "Resume playback."));
        updateAction.addCommands(new CommandData("queue", "Sends the current queue."));
        updateAction.queue();
    }

    @Override
    public void onReady(ReadyEvent event){
        System.out.println("Connected to Discord!");
        registerSlashCommands();
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        VoiceChannel vc = event.getMember().getVoiceState().getChannel();
        GuildQueue queue = Main.queueManager.getOrCreateQueue(event.getMember().getGuild().getId());

        if (event.getName().equals("join")) {
            if (vc == null) {
                event.reply(Strings.NOT_IN_VC_ERROR).queue();
                return;
            }
            if (queue.connect(vc))
                event.reply(String.format(Strings.CONNECTED_TO_VC, vc.getName())).queue();
            else
                event.reply(String.format(Strings.ALREADY_CONNECTED_ERROR, vc.getName())).queue();
        } else if (event.getName().equals("play")) {
            if (vc == null) { event.reply(Strings.NOT_IN_VC_ERROR).queue(); return; }
            if (event.getOption("track") == null) { event.reply(Strings.MISSING_TRACK_OPTION_ERROR).queue(); return; }
            event.deferReply().queue();
            queue.connect(vc);
            TrackInfo track = Main.apiManager.parseTrackString(event.getOption("track").getAsString(), event.getMember().getId());
            queue.addToQueue(track);
            event.getHook().sendMessage(Strings.ADDED_TO_QUEUE).queue();
        } else if (event.getName().equals("playtop")) {
            if (vc == null) { event.reply(Strings.NOT_IN_VC_ERROR).queue(); return; }
            if (event.getOption("track") == null) { event.reply(Strings.MISSING_TRACK_OPTION_ERROR).queue(); return; }
            event.deferReply().queue();
            queue.connect(vc);
            TrackInfo track = Main.apiManager.parseTrackString(event.getOption("track").getAsString(), event.getMember().getId());
            queue.addAtIndex(track, 0);
            event.getHook().sendMessage(Strings.ADDED_TO_TOP).queue();
        } else if (event.getName().equals("skip")) {
            queue.connect(vc);
            queue.startNextTrack();
            event.reply(Strings.TRACK_SKIPPED).queue();
        } else if (event.getName().equals("pause")) {
            Main.musicPlayer.setPaused(true, vc.getGuild().getId());
            event.reply(Strings.PAUSED).queue();
        } else if (event.getName().equals("resume")) {
            Main.musicPlayer.setPaused(false, vc.getGuild().getId());
            event.reply(Strings.RESUMED).queue();
        } else if (event.getName().equals("queue")) {
            event.replyEmbeds(queue.displayAsEmbed()).queue();
        } else {
            System.out.println("Unrecognized command: " + event.getName());
        }
    }
}
