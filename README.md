# Vault Sentinel (Fabric, Minecraft 1.21.11)

## What it does

Press the keybind (default **G**, changeable in Controls) while:
- you're holding an **Ominous Trial Key** in your main hand, AND
- your crosshair is on an **Ominous Vault**

...and the mod arms a watch on that exact vault. Every tick after that it
reads the vault's display-item rotation, and the instant a **Heavy Core**
shows up, it fires:
- a bold gold action-bar flash: "HEAVY CORE!"
- a sharp notification sound

The watch auto-cancels the moment either condition breaks: you swap the
key out of your hand, or your crosshair leaves that vault (or the vault
itself is gone / no longer loaded).

## What it deliberately does NOT do

It does not right-click the vault for you. The vault's display rotation
is cosmetic -- the actual loot roll happens server-side the moment you
insert the key, independent of what's spinning on screen. A mod that
auto-clicks the instant the Heavy Core is shown wouldn't actually change
your odds, and on top of that, auto-interacting with the world based on a
timed read of game state is the kind of thing most servers treat as a
macro/cheat. So this mod gives you the reaction cue and leaves the click
to you.

## Building

You'll need:
- JDK 21
- Internet access to Mojang's and FabricMC's Maven repos (this is normally
  automatic via Gradle/Loom, no manual downloads needed)

Steps:
1. Open this folder in IntelliJ IDEA (recommended) or any IDE with Gradle support.
2. Let Gradle sync / run `./gradlew genSources` once so your IDE can resolve
   Minecraft classes.
3. **Before building**, double check two things against your IDE's
   autocomplete, since exact mapping names can shift between Yarn builds:
   - `gradle.properties` -> `yarn_mappings` and `fabric_version`: pin these
     to whatever the current 1.21.11-tagged builds are (check
     https://fabricmc.net/develop/ and Modrinth's Fabric API page).
   - `SentinelManager.java` -> the line `state.get(VaultBlock.OMINOUS)`.
     If that doesn't compile, open `VaultBlock` in your IDE (Ctrl/Cmd+Click)
     and find the actual name of its "ominous" `BooleanProperty`, then swap
     it in.
4. Build the jar:
   ```
   ./gradlew build
   ```
5. Grab the result from `build/libs/vault-sentinel-1.0.0.jar` and drop it
   into your `.minecraft/mods` folder alongside Fabric API.

## Project layout

```
src/main/java/com/vaultsentinel/VaultSentinel.java        - common entrypoint (no-op, everything is client-only)
src/client/java/com/vaultsentinel/client/VaultSentinelClient.java  - registers the keybind, hooks the tick loop
src/client/java/com/vaultsentinel/client/SentinelManager.java      - all the actual arm/watch/cue logic
src/main/resources/fabric.mod.json                        - mod metadata
src/main/resources/assets/vaultsentinel/lang/en_us.json   - keybind label text
```
