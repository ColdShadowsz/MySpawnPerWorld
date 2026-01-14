package com.cold.myspawn;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class Main extends JavaPlugin implements Listener {

    private SpawnManager spawnManager;

    @Override
    public void onEnable() {

        // Default blocked worlds
        getConfig().addDefault("blocked-worlds", java.util.Arrays.asList(
                "world",
                "world_nether",
                "world_the_end",
                "bskyblock_world_nether",
                "bskyblock_world_the_end",
                "oneblock_world_nether",
                "oneblock_world_the_end"
        ));
        getConfig().options().copyDefaults(true);
        saveConfig();

        spawnManager = new SpawnManager(this);
        Bukkit.getPluginManager().registerEvents(this, this);

        getCommand("myspawn").setExecutor(new MySpawnCommand());
        getCommand("removespawn").setExecutor(new RemoveSpawnCommand());
    }

    // ============================================================
    // RESPAWN HANDLER — REAL DEATH WORLD + DIMENSION FALLBACK
    // ============================================================
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent event) {
        Player p = event.getPlayer();

        // Get REAL death world (Essentials cannot override this)
        World deathWorld = null;

        if (p.getLastDeathLocation() != null) {
            deathWorld = p.getLastDeathLocation().getWorld();
        }

        // Fallback if server doesn't support lastDeathLocation
        if (deathWorld == null) {
            deathWorld = event.getRespawnLocation().getWorld();
        }

        if (deathWorld == null) return;

        String worldName = deathWorld.getName();

        // OneBlock world mapping
        String baseWorld = worldName
                .replace("_nether", "")
                .replace("_the_end", "");

        // Try exact world spawn first
        Location saved = spawnManager.getSpawn(p.getUniqueId(), worldName);

        // Fallback to main OneBlock world
        if (saved == null) {
            saved = spawnManager.getSpawn(p.getUniqueId(), baseWorld);
        }

        // Override Essentials hub spawn
        if (saved != null) {
            event.setRespawnLocation(saved);
        }
    }

    // ============================================================
    // SPAWN MANAGER (INNER CLASS)
    // ============================================================
    public class SpawnManager {

        private final Main plugin;
        private final File file;
        private final FileConfiguration data;

        public SpawnManager(Main plugin) {
            this.plugin = plugin;
            file = new File(plugin.getDataFolder(), "spawns.yml");
            if (!file.exists()) {
                try {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                } catch (IOException ignored) {}
            }
            data = YamlConfiguration.loadConfiguration(file);
        }

        public void setSpawn(UUID uuid, String world, Location loc) {
            String base = uuid.toString() + "." + world;
            data.set(base + ".x", loc.getX());
            data.set(base + ".y", loc.getY());
            data.set(base + ".z", loc.getZ());
            data.set(base + ".yaw", loc.getYaw());
            data.set(base + ".pitch", loc.getPitch());
            save();
        }

        public void removeSpawn(UUID uuid, String world) {
            data.set(uuid.toString() + "." + world, null);
            save();
        }

        public Location getSpawn(UUID uuid, String world) {
            String base = uuid.toString() + "." + world;
            if (!data.contains(base)) return null;

            World w = plugin.getServer().getWorld(world);
            if (w == null) return null;

            return new Location(
                    w,
                    data.getDouble(base + ".x"),
                    data.getDouble(base + ".y"),
                    data.getDouble(base + ".z"),
                    (float) data.getDouble(base + ".yaw"),
                    (float) data.getDouble(base + ".pitch")
            );
        }

        private void save() {
            try {
                data.save(file);
            } catch (IOException ignored) {}
        }
    }

    // ============================================================
    // /myspawn COMMAND
    // ============================================================
    public class MySpawnCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

            // Player self: /myspawn
            if (args.length == 0) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cOnly players can use this.");
                    return true;
                }

                Player p = (Player) sender;
                World world = p.getWorld();

                if (getConfig().getStringList("blocked-worlds").contains(world.getName())) {
                    p.sendMessage("§cYou cannot set a spawn in this world.");
                    return true;
                }

                spawnManager.setSpawn(p.getUniqueId(), world.getName(), p.getLocation());
                p.sendMessage("§aYour spawn for this world has been set.");
                return true;
            }

            // Admin: /myspawn <player>
            if (!sender.hasPermission("myspawn.admin")) {
                sender.sendMessage("§cNo permission.");
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage("§cPlayer not found.");
                return true;
            }

            World world = target.getWorld();
            spawnManager.setSpawn(target.getUniqueId(), world.getName(), target.getLocation());
            sender.sendMessage("§aSpawn set for " + target.getName() + " in world " + world.getName());
            return true;
        }
    }

    // ============================================================
    // /removespawn COMMAND
    // ============================================================
    public class RemoveSpawnCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

            if (!sender.hasPermission("myspawn.admin")) {
                sender.sendMessage("§cNo permission.");
                return true;
            }

            if (args.length != 1) {
                sender.sendMessage("§cUsage: /removespawn <player>");
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage("§cPlayer not found.");
                return true;
            }

            spawnManager.removeSpawn(target.getUniqueId(), target.getWorld().getName());
            sender.sendMessage("§aRemoved spawn for " + target.getName() + " in world " + target.getWorld().getName());
            return true;
        }
    }
}
