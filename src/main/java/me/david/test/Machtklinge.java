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

public final class Machtklinge implements Listener, CommandExecutor {

    private JavaPlugin plugin;
    private NamespacedKey key;

    public void register(JavaPlugin plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(this.plugin, "weapon_id");

        this.plugin.getCommand("machtklinge").setExecutor(this);

        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);

        rezepteRegistrieren();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Nur im Spiel nutzbar!");
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("machtklinge")) {
            ItemStack item = erstelleMachtklinge();
            p.getInventory().addItem(item);
            p.sendMessage(
                    ChatColor.GRAY + "Du hast die " + ChatColor.RED + "Machtklinge" + ChatColor.GRAY + " erhalten.");
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

        if (id.equals("machtklinge")) {
            e.getPlayer().sendMessage("KILLLL!");
            if (e.getAction() == Action.LEFT_CLICK_BLOCK && e.getClickedBlock() != null) {
                e.setCancelled(true);
                e.getClickedBlock().breakNaturally(new ItemStack(Material.NETHERITE_PICKAXE));
            }
        }
    }

    public void rezepteRegistrieren() {
        NamespacedKey rezept_2_key = new NamespacedKey(this.plugin, "machtklinge");
        Bukkit.removeRecipe(rezept_2_key); // doppelte Registrierung vermeiden (Reload)
        ItemStack rezept_2_item = erstelleMachtklinge();
        ShapedRecipe rezept_2 = new ShapedRecipe(rezept_2_key, rezept_2_item);
        rezept_2.shape("XXX", "XYX", "XXX");
        rezept_2.setIngredient('X', Material.DIAMOND);
        rezept_2.setIngredient('Y', Material.NETHERITE_SWORD);
        Bukkit.addRecipe(rezept_2);
    }

    public ItemStack erstelleMachtklinge() {
        ItemStack item = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Machtklinge");

        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "machtklinge");

        meta.addAttributeModifier(Attribute.ATTACK_DAMAGE, new AttributeModifier(
                UUID.randomUUID(),
                "dmg",
                20.0,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlotGroup.MAINHAND));

        meta.addAttributeModifier(Attribute.ATTACK_SPEED, new AttributeModifier(
                UUID.randomUUID(),
                "spd",
                1.0,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlotGroup.MAINHAND));

        meta.setUnbreakable(true);

        meta.setCustomModelData(1001); // resource pack

        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);

        item.setItemMeta(meta);

        return item;
    }

}
