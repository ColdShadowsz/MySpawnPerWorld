# MySpawnPerWorld

MySpawnPerWorld was created to fix a long‑standing issue on OneBlock and SkyBlock servers where players dying in Nether or End dimensions were always forced back to the hub by Essentials. This plugin restores proper per‑world respawn behavior and adds personal spawn points for each world.  
Built and tested on 1.20.6 — CREATED FOR KAIDMC

---

## Features

- Per‑world personal spawns: Players can set a unique spawn point for each allowed world.
- Dimension‑aware respawning: Deaths in `_nether` or `_the_end` worlds correctly fall back to the main world (e.g., `oneblock_world`).
- Essentials‑proof: Overrides hub respawns even when Essentials or EssentialsSpawn is installed.
- Bedrock support: Fully compatible with Geyser/Floodgate using UUID‑based storage.
- Admin tools:
  - `/myspawn <player>` to set a spawn for another player
  - `/removespawn <player>` to remove a player’s spawn for the current world
- Configurable world restrictions: Block specific worlds from allowing `/myspawn`.
- Persistent storage: All spawns are saved to `spawns.yml` and survive restarts.

---

## Requirements

- Java: 17–21  
- Server: PaperMC / Spigot 1.13+ (built and tested on 1.20.6)

Note: MySpawnPerWorld is officially built for 1.20.6.  
Users may adjust the `api-version` in `plugin.yml` or recompile for other versions if needed.

---

## Installation

1. Place JAR: Put `MySpawnPerWorld.jar` in your server’s `plugins` folder  
2. Start server: The plugin initializes and creates its data folder  
3. Edit config: A default `config.yml` will be generated at `plugins/MySpawnPerWorld/config.yml`

---

## Configuration

- `blocked-worlds`: A list of worlds where players are not allowed to use `/myspawn`.  
  Worlds not listed here will allow personal spawn points.

### Example `config.yml`

```yaml
# Worlds where players are NOT allowed to use /myspawn
# You can add or remove worlds freely.
blocked-worlds:
  - world
  - world_nether
  - world_the_end
  - bskyblock_world_nether
  - bskyblock_world_the_end
  - oneblock_world_nether
  - oneblock_world_the_end
