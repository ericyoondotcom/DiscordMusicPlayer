import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;
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
        if(debugGuildId != null){
            System.out.println("Using debug guild " + debugGuildId + "!");
        }
        CommandListUpdateAction updateAction;
        if(debugGuildId != null){
            Guild debugGuild = client.getGuildById(debugGuildId);
            updateAction = debugGuild.updateCommands();
        } else {
            updateAction = client.updateCommands();
        }
        String d = debugGuildId == null ? "" : "[DEBUG] ";
        // To delete commands, just comment out the following lines
        updateAction.addCommands(new CommandData("join", d+"Connect to your voice channel."));
        updateAction.addCommands(new CommandData("play", d+"Add a track to the end of the queue.").addOption(OptionType.STRING, "track", "The track to play", true));
        updateAction.addCommands(new CommandData("playtop", d+"Add a track to the beginning of the queue.").addOption(OptionType.STRING, "track", "The track to play", true));
        updateAction.addCommands(new CommandData("skip", d+"Skips the next track(s) in the queue.").addOption(OptionType.INTEGER, "count", "How many tracks to skip.", false));
        updateAction.addCommands(new CommandData("pause", d+"Pause playback."));
        updateAction.addCommands(new CommandData("resume", d+"Resume playback."));
        updateAction.addCommands(new CommandData("queue", d+"Sends the current queue."));
        updateAction.addCommands(new CommandData("clear", d+"Clears the queue."));
        updateAction.addCommands(new CommandData("leave", d+"Disconnects from the voice channel."));
        updateAction.addCommands(new CommandData("shuffle", d+"Randomizes the queue."));
        updateAction.addCommands(new CommandData("loop", d+"Toggles looping for the queue."));
        updateAction.addCommands(new CommandData("loopsong", d+"Toggles looping for the first song."));
        updateAction.queue();
    }

    @Override
    public void onReady(ReadyEvent event){
        System.out.println("Connected to Discord!");
        registerSlashCommands();
    }

    @Override
    public void onSlashCommand(final SlashCommandEvent event) {
        VoiceChannel vc = event.getMember().getVoiceState().getChannel();
        final GuildQueue queue = Main.queueManager.getOrCreateQueue(event.getMember().getGuild().getId());

        if (event.getName().equals("join"))
        {
            if (vc == null) {
                event.reply(Strings.NOT_IN_VC_ERROR).queue();
                return;
            }
            if (queue.connect(vc)) {
                if(queue.queueLength() == 0) {
                    event.reply(String.format(Strings.CONNECTED_TO_VC, vc.getName())).queue();
                    queue.resetLoop();
                }
                else {
                    event.reply(String.format(Strings.CONNECTED_TO_VC_QUEUE_PRESERVED, vc.getName())).addEmbeds(queue.displayAsEmbed()).queue();
                }
            } else {
                event.reply(String.format(Strings.ALREADY_CONNECTED_ERROR, vc.getName())).queue();
            }
        }
        else if (event.getName().equals("play") || event.getName().equals("playtop"))
        {
            final boolean playTop = event.getName().equals("playtop");
            if (vc == null) { event.reply(Strings.NOT_IN_VC_ERROR).queue(); return; }
            if (event.getOption("track") == null) { event.reply(Strings.MISSING_TRACK_OPTION_ERROR).queue(); return; }
            event.deferReply().queue();
            queue.connect(vc);
            final String searchQuery = event.getOption("track").getAsString();
            Main.apiManager.parseTrackString(searchQuery, event.getMember().getId(), new ParseTrackStringHandler() {
                public void onTrackFound(TrackInfo track) {
                    QueueAddHandler handler = new QueueAddHandler() {
                        public void onTrackLoadSuccess(TrackInfo info) {
                            if(playTop) event.getHook().sendMessage(String.format(Strings.ADDED_TO_TOP, info.name)).queue();
                            else event.getHook().sendMessage(String.format(Strings.ADDED_TO_QUEUE, info.name)).queue();
                        }
                        public void onPlaylistLoadSuccess(TrackInfo[] tracks, String playlistName) {
                            if(playTop) event.getHook().sendMessage(String.format(Strings.PLAYLIST_ADDED_TO_TOP, tracks.length, playlistName)).queue();
                            else event.getHook().sendMessage(String.format(Strings.PLAYLIST_ADDED_TO_QUEUE, tracks.length, playlistName)).queue();
                        }
                        public void onFailure(String reason) {
                            event.getHook().sendMessage(reason).queue();
                        }
                    };
                    if(playTop) queue.addAtIndex(track, 0, handler);
                    else queue.addToQueue(track, handler);
                }
                public void onNoSearchResults() {
                    event.getHook().sendMessage(String.format(Strings.NO_RESULTS_FOUND, searchQuery)).queue();
                }
                public void onError(Exception e) { event.getHook().sendMessage(Strings.UNKNOWN_ERROR).queue(); }
            });
        }
        else if (event.getName().equals("skip"))
        {
            queue.connect(vc);
            queue.internal_onTrackEnd(true);
            event.reply(Strings.TRACK_SKIPPED).queue();
        }
        else if (event.getName().equals("pause"))
        {
            Main.musicPlayer.setPaused(true, vc.getGuild().getId());
            event.reply(Strings.PAUSED).queue();
        }
        else if (event.getName().equals("resume"))
        {
            Main.musicPlayer.setPaused(false, vc.getGuild().getId());
            event.reply(Strings.RESUMED).queue();
        }
        else if (event.getName().equals("queue"))
        {
            event.replyEmbeds(queue.displayAsEmbed()).queue();
        }
        else if(event.getName().equals("clear"))
        {
            queue.clearQueue();
            event.reply(Strings.QUEUE_CLEARED).queue();
        }
        else if(event.getName().equals("leave"))
        {
            if(Main.musicPlayer.disconnect(queue.guildId)){
//                queue.clearQueue();
                queue.resetLoop();
                event.reply(Strings.BOT_DISCONNECT_SUCCESS).queue();
            } else {
                event.reply(Strings.BOT_NOT_CONNECTED_ERROR).queue();
            }
        }
        else if(event.getName().equals("shuffle"))
        {
            queue.shuffleQueue();
            event.reply(Strings.QUEUE_SHUFFLED).queue();
        }
        else if(event.getName().equals("loop"))
        {
            if (queue.toggleLoopQueue()) {
                event.reply(Strings.QUEUE_LOOPED).queue();
            } else {
                event.reply(Strings.QUEUE_UNLOOPED).queue();
            }
        }
        else if(event.getName().equals("loopsong"))
        {
            if (queue.toggleLoopSong()) {
                event.reply(Strings.SONG_LOOPED).queue();
            } else {
                event.reply(Strings.SONG_UNLOOPED).queue();
            }
        }
        else
        {
            System.out.println("Unrecognized command: " + event.getName());
        }
    }
}
