package me.david.test;

// Pakete importeren

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Warden;
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
import org.bukkit.util.Vector;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Shulker;
import org.bukkit.entity.Vindicator;
import org.bukkit.entity.Zombie;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

// Warnungen ausblenden
@SuppressWarnings({"deprecation", "removal", "DataFlowIssue"})
@ParametersAreNonnullByDefault

// Klasse
public final class Test extends JavaPlugin implements Listener, CommandExecutor {

    // Variablen

    // Waffen
    private NamespacedKey key;

    // Beim Starten
    @Override
    public void onEnable() {

        // Kommandos registrieren
        key = new NamespacedKey(this, "weapon_id");
        getCommand("machtklinge").setExecutor(this);
        getCommand("donneraxt").setExecutor(this);
        getCommand("hirsch").setExecutor(this);

        // Events registrieren
        getServer().getPluginManager().registerEvents(this, this);

        // Rezepte registrieren
        rezepteRegistrieren();

    }

    // Beim Eingeben der Befehle
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player p)) {
            sender.sendMessage("Nur im Spiel nutzbar!");
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("machtklinge")) {
            ItemStack item = erstelleMachtklinge();
            p.getInventory().addItem(item);
            p.sendMessage(ChatColor.GRAY + "Du hast die " + ChatColor.RED + "Machtklinge" + ChatColor.GRAY + " erhalten.");
        }

        if (cmd.getName().equalsIgnoreCase("donneraxt")) {
            ItemStack item = erstelleDonneraxt();
            p.getInventory().addItem(item);
            p.sendMessage(ChatColor.GRAY + "Du hast die " + ChatColor.AQUA + "Donneraxt" + ChatColor.GRAY + " erhalten.");
        }

        if (cmd.getName().equalsIgnoreCase("hirsch")) {
            // Zielposition: 5 Blöcke (≈ Meter) in Blickrichtung
            Vector dir = p.getLocation().getDirection().normalize().multiply(5);
            var spawnLoc = p.getLocation().clone().add(dir);
            spawnLoc.setY(Math.floor(spawnLoc.getY())); // auf Blockhöhe

            var world = p.getWorld();
            var ent = world.spawnEntity(spawnLoc, EntityType.WARDEN);
            if (!(ent instanceof Warden warden)) {
                p.sendMessage("Konnte keinen Warden spawnen.");
                return true;
            }

            // Name + Sichtbarkeit
            warden.customName(Component.text("Hirsch", NamedTextColor.DARK_GREEN));
            warden.setCustomNameVisible(true);

            // Standard-Sounds NICHT unterdrücken
            warden.setSilent(false);

            // Attribute anpassen (Beispiele – tune nach Wunsch)
            warden.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(3.5);
            warden.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(15.0);
            warden.getAttribute(Attribute.FOLLOW_RANGE).setBaseValue(32.0);
            warden.getAttribute(Attribute.KNOCKBACK_RESISTANCE).setBaseValue(1.0);

            p.sendMessage(Component.text("HAHAHAHAHAHAHAHAHAHA! ICH WERDE DICH TÖTEN.", NamedTextColor.RED));
        }

        if (cmd.getName().equalsIgnoreCase("horde")) {
            World world = p.getWorld();
            Location base = p.getLocation().add(p.getLocation().getDirection().normalize().multiply(30));
            java.util.Random rnd = new java.util.Random();
            java.util.List<java.util.UUID> tracked = new java.util.ArrayList<>();

            // zufällige Boden-Position nahe der Basis
            java.util.function.Supplier<Location> randLoc = () -> {
                int x = base.getBlockX() + rnd.nextInt(17) - 8;
                int z = base.getBlockZ() + rnd.nextInt(17) - 8;
                int y = world.getHighestBlockYAt(x, z) + 1;
                return new Location(world, x + 0.5, y, z + 0.5);
            };

            // 10 Chicken Jockeys (nur den Zombie tracken)
            for (int i = 0; i < 10; i++) {
                Location loc = randLoc.get();
                Chicken chicken = (Chicken) world.spawnEntity(loc, EntityType.CHICKEN);
                Zombie zombie = (Zombie) world.spawnEntity(loc, EntityType.ZOMBIE);
                zombie.setBaby(true);
                zombie.setTarget(p);
                chicken.addPassenger(zombie);
                tracked.add(zombie.getUniqueId());
            }

            // 5 Vindicators
            for (int i = 0; i < 5; i++) {
                Location loc = randLoc.get();
                Vindicator v = (Vindicator) world.spawnEntity(loc, EntityType.VINDICATOR);
                v.setTarget(p);
                tracked.add(v.getUniqueId());
            }

            // 7 Shulker (bleiben eher stationär, schießen aber auf das Ziel)
            for (int i = 0; i < 7; i++) {
                Location loc = randLoc.get();
                Shulker s = (Shulker) world.spawnEntity(loc, EntityType.SHULKER);
                s.setTarget(p);
                tracked.add(s.getUniqueId());
            }

            p.sendMessage(ChatColor.DARK_RED + "Die Horde kommt!");

            // Grausame Musik bis alle tot sind
            new BukkitRunnable() {
                int tick = 0;
                @Override public void run() {
                    if (tick % 400 == 0) {
                        p.playSound(p.getLocation(), Sound.MUSIC_DISC_5, SoundCategory.RECORDS, 1.0f, 1.0f);
                    }
                    tick += 20;

                    int alive = 0;
                    for (java.util.UUID id : tracked) {
                        var e = Bukkit.getEntity(id);
                        if (e != null && !e.isDead()) alive++;
                    }
                    if (alive == 0) {
                        p.stopSound(Sound.MUSIC_DISC_5, SoundCategory.RECORDS);
                        p.sendMessage(ChatColor.GREEN + "Horde besiegt.");
                        cancel();
                    }
                }
            }.runTaskTimer(this, 0L, 20L);

            return true;
        }

        return true;
    }


    // Verhalten der Waffen
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

        // Machtklinge: Blöcke instant abbauen
        if (id.equals("machtklinge")) {
            e.getPlayer().sendMessage("KILLLL!");
            if (e.getAction() == Action.LEFT_CLICK_BLOCK && e.getClickedBlock() != null) {
                e.setCancelled(true);
                e.getClickedBlock().breakNaturally(new ItemStack(Material.NETHERITE_PICKAXE));
            }
        }

        // Donneraxt: Blitz beim Rechtsklick
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

    // Individuelle Funktionen
    private void rezepteRegistrieren() {
        NamespacedKey rezept_1_key = new NamespacedKey(this, "donneraxt");
        Bukkit.removeRecipe(rezept_1_key); // doppelte Registrierung vermeiden (Reload)
        ItemStack rezept_1_item = erstelleDonneraxt();
        ShapedRecipe rezept_1 = new ShapedRecipe(rezept_1_key, rezept_1_item);
        rezept_1.shape("XXX", "XYX", "XXX");
        rezept_1.setIngredient('X', Material.FLINT_AND_STEEL);
        rezept_1.setIngredient('Y', Material.DIAMOND_SWORD);
        Bukkit.addRecipe(rezept_1);

        NamespacedKey rezept_2_key = new NamespacedKey(this, "machtklinge");
        Bukkit.removeRecipe(rezept_2_key); // doppelte Registrierung vermeiden (Reload)
        ItemStack rezept_2_item = erstelleMachtklinge();
        ShapedRecipe rezept_2 = new ShapedRecipe(rezept_2_key, rezept_2_item);
        rezept_2.shape("XXX", "XYX", "XXX");
        rezept_2.setIngredient('X', Material.DIAMOND);
        rezept_2.setIngredient('Y', Material.NETHERITE_SWORD);
        Bukkit.addRecipe(rezept_2);
    }

    private ItemStack erstelleMachtklinge() {
        ItemStack item = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Machtklinge");

        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "machtklinge");

        meta.addAttributeModifier(Attribute.ATTACK_DAMAGE, new AttributeModifier(
                UUID.randomUUID(),
                "dmg",
                20.0,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlotGroup.MAINHAND
        ));

        meta.addAttributeModifier(Attribute.ATTACK_SPEED, new AttributeModifier(
                UUID.randomUUID(),
                "spd",
                1.0,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlotGroup.MAINHAND
        ));

        meta.setUnbreakable(true);

        meta.setCustomModelData(1001); // resource pack

        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);

        item.setItemMeta(meta);

        return item;
    }

    private ItemStack erstelleDonneraxt() {
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

    // HIRSCH
    private void wardenTrySet(Warden w, Attribute attr, double value) {
        var mod = w.getAttribute(attr);
        if (mod != null) mod.setBaseValue(value);
    }


}
