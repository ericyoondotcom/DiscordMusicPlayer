# DiscordMusicPlayer
SUSAN >:(

## Configuration Options
- `discord.token`: your Discord bot token.
- `discord.commands_debug_guild`: set to a guild ID to register guild commands intead of global commands for easier debugging.

## Building from source
- Install all Maven packages from pom.xml.
- [Follow these instructions to compile to JAR](https://stackoverflow.com/a/45303637/4699945). (Create a new artifact, "JAR from Modules",
repoint MANIFEST.MF from `.../main/java` to `.../main/resources`, and press "Build Artifacts" to create the JAR)
