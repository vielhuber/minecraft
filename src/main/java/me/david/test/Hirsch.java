package me.david.test;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Warden;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import javax.annotation.ParametersAreNonnullByDefault;

@SuppressWarnings({"DataFlowIssue"})
@ParametersAreNonnullByDefault

public final class Hirsch implements Listener, CommandExecutor {

    private JavaPlugin plugin;

    public void register(JavaPlugin plugin) {
        this.plugin = plugin;

        this.plugin.getCommand("hirsch").setExecutor(this);

        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Nur im Spiel nutzbar!");
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("hirsch")) {
            spawnHirsch(p);
            p.sendMessage(Component.text("HAHAHAHAHAHAHAHAHAHA! ICH WERDE DICH TÖTEN.", NamedTextColor.RED));
        }

        return true;
    }

    public Warden spawnHirsch(Player p) {
        // Zielposition: 5 Blöcke (≈ Meter) in Blickrichtung
        Vector dir = p.getLocation().getDirection().normalize().multiply(5);
        var spawnLoc = p.getLocation().clone().add(dir);
        spawnLoc.setY(Math.floor(spawnLoc.getY())); // auf Blockhöhe

        var world = p.getWorld();
        var ent = world.spawnEntity(spawnLoc, EntityType.WARDEN);
        if (!(ent instanceof Warden warden)) {
            p.sendMessage("Konnte keinen Warden spawnen.");
            return null;
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

        return warden;
    }

}
