package me.david.test;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings({"DataFlowIssue"})
@ParametersAreNonnullByDefault

public final class Mace implements Listener, CommandExecutor {

    private static final int COOLDOWN_SECONDS = 6;

    private JavaPlugin plugin;

    // stores the timestamp (ms) of the last mace hit per player
    private final Map<UUID, Long> lastHitTimestamp = new HashMap<>();

    public void register(JavaPlugin plugin) {
        this.plugin = plugin;
        this.plugin.getCommand("stampfer").setExecutor(this);
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "Du hast keine Berechtigung für diesen Befehl!");
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("stampfer")) {
            ItemStack mace = new ItemStack(Material.MACE);
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.getInventory().addItem(mace.clone());
                player.sendMessage(ChatColor.GRAY + "Du hast das " + ChatColor.GOLD + "Mace" + ChatColor.GRAY + " erhalten.");
            }
            sender.sendMessage(ChatColor.GREEN + "Mace wurde an alle Spieler verteilt.");
        }

        return true;
    }

    /**
     * Cancels enchanting a Mace at the enchanting table.
     */
    @EventHandler
    public void onEnchantItem(EnchantItemEvent event) {
        if (event.getItem().getType() != Material.MACE) {
            return;
        }
        event.setCancelled(true);
        event.getEnchanter().sendMessage(ChatColor.RED + "Das Mace kann nicht verzaubert werden!");
    }

    /**
     * Cancels all anvil operations involving a Mace as primary item.
     */
    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        ItemStack primary = event.getInventory().getItem(0);

        if (primary == null || primary.getType() != Material.MACE) {
            return;
        }

        event.setResult(null);
    }

    /**
     * Enforces a 6-second cooldown between mace attacks.
     */
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) {
            return;
        }

        ItemStack item = attacker.getInventory().getItemInMainHand();

        if (item.getType() != Material.MACE) {
            return;
        }

        long now = System.currentTimeMillis();
        long lastHit = lastHitTimestamp.getOrDefault(attacker.getUniqueId(), 0L);
        long elapsedMilliseconds = now - lastHit;
        long cooldownMilliseconds = COOLDOWN_SECONDS * 1000L;

        if (elapsedMilliseconds < cooldownMilliseconds) {
            event.setCancelled(true);
            long remainingSeconds = (cooldownMilliseconds - elapsedMilliseconds) / 1000 + 1;
            attacker.sendMessage(ChatColor.RED + "Das Mace ist noch " + remainingSeconds + "s im Cooldown!");
            return;
        }

        lastHitTimestamp.put(attacker.getUniqueId(), now);
    }
}
