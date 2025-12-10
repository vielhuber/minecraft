# ⚔️ minecraft ⚔️

A custom Minecraft Paper plugin featuring epic weapons, challenging horde battles, and custom resource packs. Built for Minecraft 1.21.

## Features

-   **Custom Weapons**

    -   **Machtklinge** - Powerful sword with instant block breaking
    -   **Donneraxt** - Thunder axe that summons lightning strikes

-   **Horde Mode** (`/horde`)

    -   Spawns waves of enemies: Chicken Jockeys, Vindicators, Shulkers & custom "Hirsch" (Warden)
    -   Full combat gear: Netherite armor, enchanted weapons, Elytra, Golden Apples, Totems
    -   Dramatic effects: Night time, lighting, custom sounds
    -   Team system prevents friendly fire between mobs
    -   Keep inventory enabled during battle

-   **Resource Pack Integration**

    -   Custom weapon textures
    -   Custom sounds
    -   Automatic SHA1 hash management

-   **Developer Tools**
    -   `/reset` - Clear all mobs, restore day, reset inventory
    -   `/hirsch` - Spawn custom Warden boss
    -   Build scripts for local and production deployment

## Development

### Setup

-   Java
    -   Install Adoptium: https://adoptium.net/en-GB/temurin/releases/?os=windows&package=jdk&version=21
    -   Version: 21
    -   Set JAVA_HOME environment variable: Yes
    -   Test: `java -version`
    -   If that doesn't work: Check PATH and remove old entries (`which java`)
-   Clone existing project
    -   `mkdir -p $(wslpath "$(cmd.exe /c 'echo %USERPROFILE%' 2>/dev/null | tr -d '\r')")/Minecraft/test`
    -   `cd $(wslpath "$(cmd.exe /c 'echo %USERPROFILE%' 2>/dev/null | tr -d '\r')")/Minecraft/test`
    -   `git clone git@github.com:vielhuber/minecraft.git .`
-   Create new project
    -   Intellij IDEA Community
        -   Settings
            -   Plugins > "Deutsch (German) Language Localization" (free version)
            -   Appearance > Languages > German > Restart
            -   Plugins > "Minecraft Development"
            -   Editor > Inspections > Proofreading > Disable
            -   Editor > Add Unambiguous Imports during this process: on
            -   Tools > Actions on Save > Reformat Code + Optimize Imports
-   New project
-   Project name: "test"
-   Location: `%userprofile%/Minecraft`
-   Plugin + Paper + Gradle + Java
-   Minecraft version: 1.21.8
-   Group ID: me.42
-   JDK: 21 (again afterwards if necessary: "Set up SDK:")
-   Build (local + production)
-   Gradle > Tasks > build > build
-   Copy `/test/build/libs/\*.jar` to `/plugins`
-   Restart server cleanly (not `/reload`)
-   Set up local server
    -   Download Paper at https://papermc.io/downloads/paper
    -   Copy `*.jar` to `%userprofile%/Minecraft/paper/paper.jar`
    -   Start with `java -Xms4G -Xmx4G -jar paper.jar --nogui`
    -   Edit eula.txt: eula=true
    -   Connect to "localhost" in Vanilla Minecraft
    -   Upgrade: Simply download the new `*.jar` and replace `paper.jar`
-   Set up production server
    -   Book server at https://mine-hoster.de
    -   Administration: https://gamepanel.mine-hoster.de
    -   Versions > Paper > Latest version
-   Grant permissions
    -   `/op rebuhleiv` in the server console
    -   Also write the `ops.json` correctly
    -   Test with `/time set day`
-   Start Minecraft Launcher and connect to `localhost`

### Usage

-   VSCode (WSL)
    -   `cd $(wslpath "$(cmd.exe /c 'echo %USERPROFILE%' 2>/dev/null | tr -d '\r')")/Minecraft/test`
    -   `code .`
-   Scripts (cmd without admin)
    -   `cd %userprofile%/Minecraft/test/scripts`
    -   `script.bat local`
    -   `script.bat prod`

## Notes

### Server configuration

-   Name: Server properties > motd (Generator: https://minecraft.tools/de/motd.php)
-   Icon: Place `/server-icon.png` (64x64) in the root directory
-   Domain: Point A record from external domain to IP address

### Remote access

-   server.properties
    -   `enable-rcon=true`
    -   `rcon.password=xxxxxxxxx`
    -   `rcon.port=25575`
-   mcrcon-0.7.2-windows-x86-64.zip

### Resource packs

-   `cd ./resourcepack/data`
-   `rm ./../data.zip`
-   `zip ./../data.zip -r .`
-   `sha1sum ./../data.zip`
-   Upload and edit `server.properties`

```
require-resource-pack=true
resource-pack=https\://tld.com/path/to/data.zip
resource-pack-id=
resource-pack-prompt={"text"\:"Server pack for your weapons","color"\:"gold"}
resource-pack-sha1=0086e2294ef390c7fddfde527927423fae2075f9
```

-   Local folder: `%APPDATA%\.minecraft\downloads\`
-   Logs: `%APPDATA%\.minecraft\logs\latest.log`
-   Resource pack refresh: F3+T
-   Rendered textures: F3+S: `%APPDATA%\.minecraft\screenshots\debug\minecraft_textures_atlas_blocks.png_0.png`
-   Only one resource pack can be used at a time

### Simple Voice Chat

-   Install on the server side in `/plugins`
-   https://modrinth.com/plugin/simple-voice-chat/versions?g=1.21.8&l=paper
-   Install on the client side, e.g., with `CurseForge`

### Worlds

-   Create a new world with seed
-   `server.properties`: `level-seed=123456789`
-   `mv world world_backup_2025-01-01`
-   `mv world_nether world_nether_backup_2025-01-01`
-   `mv world_the_end world_the_end_backup_2025-01-01`
-   Restart
