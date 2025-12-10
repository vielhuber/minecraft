package me.david.test;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Shulker;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Wither;
import org.bukkit.entity.Entity;

import javax.annotation.ParametersAreNonnullByDefault;

@SuppressWarnings({"deprecation", "DataFlowIssue"})
@ParametersAreNonnullByDefault

public final class Reset implements Listener, CommandExecutor {

    private JavaPlugin plugin;

    public void register(JavaPlugin plugin) {
        this.plugin = plugin;

        this.plugin.getCommand("reset").setExecutor(this);

        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Nur im Spiel nutzbar!");
            return true;
        }


        if (cmd.getName().equalsIgnoreCase("reset")) {
            World world = p.getWorld();

            // Team löschen
            org.bukkit.scoreboard.Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
            org.bukkit.scoreboard.Team hordeTeam = scoreboard.getTeam("horde");
            if (hordeTeam != null) {
                hordeTeam.unregister();
            }

            // KeepInventory deaktivieren
            world.setGameRule(GameRule.KEEP_INVENTORY, false);

            // Inventar aller Spieler leeren und Effekte entfernen
            for (Player player : world.getPlayers()) {
                player.getInventory().clear();
                player.getInventory().setArmorContents(null);
                player.removePotionEffect(PotionEffectType.NIGHT_VISION);
            }

            int killed = 0;
            for (Entity entity : world.getEntities()) {
                // Nur feindliche Mobs töten
                if (entity instanceof Monster ||
                        entity instanceof Shulker ||
                        entity instanceof Slime ||
                        entity instanceof Phantom ||
                        entity instanceof EnderDragon ||
                        entity instanceof Wither) {
                    entity.remove();
                    killed++;
                }
            }
            for (org.bukkit.entity.Entity entity : world.getEntities()) {
                if (entity instanceof Chicken chicken && !chicken.getPassengers().isEmpty()) {
                    chicken.remove();
                    killed++;
                }
            }

            // Lichtblöcke in der Umgebung entfernen
            Location playerLoc = p.getLocation();
            int radius = 50;
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        Block block = world.getBlockAt(
                                playerLoc.getBlockX() + x,
                                playerLoc.getBlockY() + y,
                                playerLoc.getBlockZ() + z);
                        if (block.getType() == Material.LIGHT) {
                            block.setType(Material.AIR);
                        }
                    }
                }
            }

            // Uhrzeit auf Tag setzen
            world.setTime(1000); // 1000 = Morgens, voller Tag
            world.setStorm(false);
            world.setThundering(false);
            world.setWeatherDuration(0);

            p.sendMessage(ChatColor.DARK_RED + "" + killed + " Gegner getötet.");
            return true;
        }

        return true;
    }

}
