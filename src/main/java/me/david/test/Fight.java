package me.david.test;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SuppressWarnings({"deprecation", "DataFlowIssue"})
@ParametersAreNonnullByDefault

public final class Fight implements Listener, CommandExecutor {

    private JavaPlugin plugin;
    private Random random = new Random();

    public void register(JavaPlugin plugin) {
        this.plugin = plugin;

        this.plugin.getCommand("fight").setExecutor(this);

        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("fight")) {
            startFight();
        }

        return true;
    }

    private void startFight() {
        // Alle Online-Spieler holen
        List<Player> allPlayers = new ArrayList<>(Bukkit.getOnlinePlayers());

        if (allPlayers.size() < 2) {
            Bukkit.broadcastMessage(ChatColor.RED + "Es müssen mindestens 2 Spieler online sein!");
            return;
        }

        // Alle Spieler in Spectator Modus setzen
        for (Player player : allPlayers) {
            player.setGameMode(GameMode.SPECTATOR);
        }

        // 2 zufällige Spieler auswählen
        Player player1 = allPlayers.remove(random.nextInt(allPlayers.size()));
        Player player2 = allPlayers.remove(random.nextInt(allPlayers.size()));

        // Beide Spieler in Survival setzen
        player1.setGameMode(GameMode.SURVIVAL);
        player2.setGameMode(GameMode.SURVIVAL);

        // Inventare leeren
        player1.getInventory().clear();
        player1.getInventory().setArmorContents(null);
        player2.getInventory().clear();
        player2.getInventory().setArmorContents(null);

        // Spawn-Location festlegen (z.B. Spawn-Punkt der Welt)
        Location spawnLocation = player1.getWorld().getSpawnLocation();

        // Spieler 2 am Boden spawnen
        player2.teleport(spawnLocation);

        // Spieler 2 bekommt Netherite Schwert und Rüstung
        ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta swordMeta = sword.getItemMeta();
        swordMeta.addEnchant(Enchantment.SHARPNESS, 5, true);
        sword.setItemMeta(swordMeta);
        player2.getInventory().addItem(sword);

        // Netherite Rüstung
        ItemStack helmet = new ItemStack(Material.NETHERITE_HELMET);
        ItemStack chestplate = new ItemStack(Material.NETHERITE_CHESTPLATE);
        ItemStack leggings = new ItemStack(Material.NETHERITE_LEGGINGS);
        ItemStack boots = new ItemStack(Material.NETHERITE_BOOTS);

        helmet.addEnchantment(Enchantment.PROTECTION, 4);
        chestplate.addEnchantment(Enchantment.PROTECTION, 4);
        leggings.addEnchantment(Enchantment.PROTECTION, 4);
        boots.addEnchantment(Enchantment.PROTECTION, 4);

        player2.getInventory().setHelmet(helmet);
        player2.getInventory().setChestplate(chestplate);
        player2.getInventory().setLeggings(leggings);
        player2.getInventory().setBoots(boots);

        // Spieler 1 in die Luft spawnen (50 Blöcke höher)
        Location airLocation = spawnLocation.clone().add(0, 50, 0);
        player1.teleport(airLocation);

        // Spieler 1 bekommt Wassereimer
        ItemStack waterBucket = new ItemStack(Material.WATER_BUCKET);
        player1.getInventory().addItem(waterBucket);

        // Nachricht broadcasten
        Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Der Man-Hunt beginnt!");
        Bukkit.broadcastMessage(ChatColor.YELLOW + "Jäger: " + player2.getName());
        Bukkit.broadcastMessage(ChatColor.YELLOW + "Gejagter: " + player1.getName());
    }
}
