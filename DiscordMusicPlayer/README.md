# DiscordMusicPlayer
SUSAN >:(

## Configuration Options
Add a file called `config.properties` into the folder from which the program is executed.
- `discord.token`: your Discord bot token.
- `discord.commands_debug_guild`: set to a guild ID to register guild commands instead of global commands for easier debugging.
- `google.api_key`: your Google Cloud project API key.

### Getting Google Cloud set up
- Create a Google Cloud project
- Under APIs & Services, enable Youtube Data API v3
- Under Credentials, create a new API Key. Copy the key to your config file.

## Building from source
- Install all Maven packages from pom.xml.
- [Follow these instructions to compile to JAR](https://stackoverflow.com/a/45303637/4699945). (Create a new artifact, "JAR from Modules",
repoint MANIFEST.MF from `.../main/java` to `.../main/resources`, and press "Build Artifacts" > "Rebuild" to create the JAR)

## Deploying with pm2
Since pm2 gets a bit strange when calling JARs with Java, we have to create a bash script that starts the Java process.
```bash
#!/bin/bash
java -jar DiscordMusicPlayer.jar
```

Then, add the process:
```bash
$ pm2 start --name musicplayer --restart-delay=5000 NAME_OF_YOUR_SCRIPT.sh
```

## Implementation Notes
- NEW: pom.xml requires Java version 1.8. I am using language features only in
1.8, but the JAR needs to be deployable to Raspberry Pi, which ships with JDK 11.
- In your IDE, make sure to set JDK and language version appropriately. JDK 11 works for me.

## Contributors
**Development: [@ericyoondotcom](https://github.com/ericyoondotcom)**

Contributors:
- [@Focus172](https://github.com/Focus172)