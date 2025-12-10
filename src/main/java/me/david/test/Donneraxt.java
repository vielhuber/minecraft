package me.david.test;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

@SuppressWarnings({"deprecation", "removal", "DataFlowIssue"})
@ParametersAreNonnullByDefault

public final class Donneraxt implements Listener, CommandExecutor {

    private JavaPlugin plugin;
    private NamespacedKey key;

    public void register(JavaPlugin plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(this.plugin, "weapon_id");

        this.plugin.getCommand("donneraxt").setExecutor(this);

        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);

        rezepteRegistrieren();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Nur im Spiel nutzbar!");
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("donneraxt")) {
            ItemStack item = erstelleDonneraxt();
            p.getInventory().addItem(item);
            p.sendMessage(ChatColor.GRAY + "Du hast die " + ChatColor.AQUA + "Donneraxt" + ChatColor.GRAY + " erhalten.");
        }

        return true;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        ItemStack item = e.getPlayer().getInventory().getItemInMainHand();

        if (item.getType() == Material.AIR || !item.hasItemMeta()) {
            return;
        }

        String id = item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (id == null) {
            return;
        }

        if (id.equals("donneraxt")) {
            e.getPlayer().sendMessage("BOOOOM!");
            if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) {
                Player p = e.getPlayer();
                World w = p.getWorld();
                int dist = 6; // „ein paar Meter“ (Blöcke)
                Location strike = p.getLocation().add(
                        p.getLocation().getDirection().normalize().multiply(dist)
                );
                w.strikeLightning(strike);        // echter Blitz
                // w.strikeLightningEffect(strike); // nur Effekt, kein Schaden
            }
        }
    }

    public void rezepteRegistrieren() {
        NamespacedKey rezept_1_key = new NamespacedKey(this.plugin, "donneraxt");
        Bukkit.removeRecipe(rezept_1_key); // doppelte Registrierung vermeiden (Reload)
        ItemStack rezept_1_item = erstelleDonneraxt();
        ShapedRecipe rezept_1 = new ShapedRecipe(rezept_1_key, rezept_1_item);
        rezept_1.shape("XXX", "XYX", "XXX");
        rezept_1.setIngredient('X', Material.FLINT_AND_STEEL);
        rezept_1.setIngredient('Y', Material.DIAMOND_SWORD);
        Bukkit.addRecipe(rezept_1);
    }

    public ItemStack erstelleDonneraxt() {
        ItemStack item = new ItemStack(Material.NETHERITE_AXE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Donneraxt");

        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "donneraxt");

        meta.addAttributeModifier(Attribute.ATTACK_DAMAGE, new AttributeModifier(
                UUID.randomUUID(),
                "dmg",
                15,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlotGroup.MAINHAND
        ));

        meta.setUnbreakable(true);

        meta.setCustomModelData(1002); // resource pack

        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);

        item.setItemMeta(meta);

        return item;
    }

}
