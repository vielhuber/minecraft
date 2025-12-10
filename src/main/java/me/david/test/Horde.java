package me.david.test;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Warden;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Shulker;
import org.bukkit.entity.Vindicator;
import org.bukkit.entity.Zombie;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.ParametersAreNonnullByDefault;

@SuppressWarnings({"deprecation", "DataFlowIssue"})
@ParametersAreNonnullByDefault

public final class Horde implements Listener, CommandExecutor {

    private JavaPlugin plugin;
    private Hirsch hirsch;
    private Donneraxt donneraxt;
    private Machtklinge machtklinge;

    public void register(JavaPlugin plugin, Hirsch hirsch, Donneraxt donneraxt, Machtklinge machtklinge) {
        this.plugin = plugin;
        this.hirsch = hirsch;
        this.donneraxt = donneraxt;
        this.machtklinge = machtklinge;

        this.plugin.getCommand("horde").setExecutor(this);

        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Nur im Spiel nutzbar!");
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("horde")) {
            World world = p.getWorld();
            Location base = p.getLocation().add(p.getLocation().getDirection().normalize().multiply(30));
            java.util.Random rnd = new java.util.Random();
            java.util.List<java.util.UUID> tracked = new java.util.ArrayList<>();

            // Team erstellen für Horde (verhindert Friendly Fire)
            org.bukkit.scoreboard.Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
            org.bukkit.scoreboard.Team hordeTeam = scoreboard.getTeam("horde");
            if (hordeTeam == null) {
                hordeTeam = scoreboard.registerNewTeam("horde");
            }
            hordeTeam.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE,
                    org.bukkit.scoreboard.Team.OptionStatus.NEVER);
            hordeTeam.setAllowFriendlyFire(false);

            // zufällige Boden-Position nahe der Basis
            java.util.function.Supplier<Location> randLoc = () -> {
                int x = base.getBlockX() + rnd.nextInt(11) - 5;
                int z = base.getBlockZ() + rnd.nextInt(11) - 5;
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
                hordeTeam.addEntry(zombie.getUniqueId().toString());
                hordeTeam.addEntry(chicken.getUniqueId().toString());
            }

            // 5 Vindicators
            for (int i = 0; i < 5; i++) {
                Location loc = randLoc.get();
                Vindicator v = (Vindicator) world.spawnEntity(loc, EntityType.VINDICATOR);
                v.setTarget(p);
                tracked.add(v.getUniqueId());
                hordeTeam.addEntry(v.getUniqueId().toString());
            }

            // 7 Shulker (bleiben eher stationär, schießen aber auf das Ziel)
            for (int i = 0; i < 7; i++) {
                Location loc = randLoc.get();
                Shulker s = (Shulker) world.spawnEntity(loc, EntityType.SHULKER);
                s.setTarget(p);
                tracked.add(s.getUniqueId());
                hordeTeam.addEntry(s.getUniqueId().toString());
            }

            // 5 Hirsche
            for (int i = 0; i < 3; i++) {
                Warden warden = this.hirsch.spawnHirsch(p);
                if (warden != null) {
                    tracked.add(warden.getUniqueId());
                    hordeTeam.addEntry(warden.getUniqueId().toString());
                }
            }

            // Uhrzeit auf Nacht setzen
            world.setTime(18000); // 18000 = Mitternacht
            world.setStorm(false);
            world.setThundering(false);
            world.setWeatherDuration(0);

            // Statt Bäume anzünden: Dramatische Effekte ohne Zerstörung
            Location playerLoc = p.getLocation();
            /*
            int radius = 30;
            java.util.List<Block> placedLights = new java.util.ArrayList<>();
            for (int i = 0; i < 50; i++) {
                int x = playerLoc.getBlockX() + rnd.nextInt(radius * 2) - radius;
                int z = playerLoc.getBlockZ() + rnd.nextInt(radius * 2) - radius;
                int y = world.getHighestBlockYAt(x, z) + 1;
                Block block = world.getBlockAt(x, y, z);
                if (block.getType() == Material.AIR) {
                    // Unsichtbarer Lichtblock (Level 15 = maximale Helligkeit)
                    block.setType(Material.LIGHT);
                    if (block.getBlockData() instanceof org.bukkit.block.data.type.Light lightData) {
                        lightData.setLevel(15);
                        block.setBlockData(lightData);
                    }
                    placedLights.add(block);
                }
            }
            */

            // Explosions-Sounds für Drama
            world.playSound(playerLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.5f);

            // KeepInventory aktivieren
            world.setGameRule(GameRule.KEEP_INVENTORY, true);

            // Allen Spielern Ausrüstung geben
            for (Player player : world.getPlayers()) {
                ausruestungGeben(player);
                // Unendliche Night Vision (Integer.MAX_VALUE = praktisch unendlich, infinite=true)
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, PotionEffect.INFINITE_DURATION,
                        0, false, false, true));
            }

            p.sendMessage(ChatColor.DARK_RED + "Die Horde kommt!");
            p.sendMessage(ChatColor.DARK_RED + "Die Horde kommt!");
            p.sendMessage(ChatColor.DARK_RED + "Die Horde kommt!");
            p.sendMessage(ChatColor.DARK_RED + "Die Horde kommt!");
            p.sendMessage(ChatColor.DARK_RED + "Die Horde kommt!");

            // Grausame Musik bis alle tot sind
            // Boss-Musik starten
            p.playSound(p.getLocation(), "custom.ambient", SoundCategory.RECORDS, 1.0f, 1.0f);
            new BukkitRunnable() {
                @Override
                public void run() {
                    // Warden-Darkness-Effekt entfernen und Night Vision aufrechterhalten
                    for (Player player : world.getPlayers()) {
                        player.removePotionEffect(PotionEffectType.DARKNESS);
                        if (!player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION,
                                    PotionEffect.INFINITE_DURATION, 0, false, false, true));
                        }
                    }

                    int alive = 0;
                    for (java.util.UUID id : tracked) {
                        var e = Bukkit.getEntity(id);
                        if (e != null && !e.isDead())
                            alive++;
                    }
                    if (alive == 0) {
                        p.stopAllSounds();
                        p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.MASTER, 1.0f,
                                1.0f);

                        // Uhrzeit auf Tag setzen
                        world.setTime(1000); // 1000 = Morgens, voller Tag
                        world.setStorm(false);
                        world.setThundering(false);
                        world.setWeatherDuration(0);

                        p.sendMessage(ChatColor.GREEN + "Horde besiegt.");

                        cancel();
                    }
                }
            }.runTaskTimer(this.plugin, 0L, 20L);

            return true;
        }

        return true;
    }

    public void ausruestungGeben(Player player) {
        // Inventar leeren
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);

        // Machtklinge
        player.getInventory().addItem(this.machtklinge.erstelleMachtklinge());

        // Donneraxt
        player.getInventory().addItem(this.donneraxt.erstelleDonneraxt());

        // Netherite Schwert (verzaubert)
        ItemStack schwert = new ItemStack(Material.NETHERITE_SWORD);
        schwert.addEnchantment(Enchantment.SHARPNESS, 5);
        schwert.addEnchantment(Enchantment.UNBREAKING, 3);
        schwert.addEnchantment(Enchantment.FIRE_ASPECT, 2);
        schwert.addEnchantment(Enchantment.LOOTING, 3);
        player.getInventory().addItem(schwert);

        // Bogen (verzaubert)
        ItemStack bogen = new ItemStack(Material.BOW);
        bogen.addEnchantment(Enchantment.POWER, 5);
        bogen.addEnchantment(Enchantment.INFINITY, 1);
        bogen.addEnchantment(Enchantment.FLAME, 1);
        bogen.addEnchantment(Enchantment.UNBREAKING, 3);
        player.getInventory().addItem(bogen);

        // Pfeile (mindestens 1 für Infinity)
        player.getInventory().addItem(new ItemStack(Material.ARROW, 64));

        // 64 Goldene Äpfel (Enchanted)
        player.getInventory().addItem(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 64));

        // Ein Totem
        player.getInventory().addItem(new ItemStack(Material.TOTEM_OF_UNDYING, 1));

        // Sehr gutes Schild - direkt in Off-Hand ausgerüstet
        ItemStack schild = new ItemStack(Material.SHIELD);
        schild.addEnchantment(Enchantment.UNBREAKING, 3);
        schild.addEnchantment(Enchantment.MENDING, 1);
        player.getInventory().setItemInOffHand(schild);

        // Totem ins Inventar (nicht Off-Hand, da Schild dort ist)
        player.getInventory().addItem(new ItemStack(Material.TOTEM_OF_UNDYING));

        // Netherite Rüstung (verzaubert)
        ItemStack helm = new ItemStack(Material.NETHERITE_HELMET);
        helm.addEnchantment(Enchantment.PROTECTION, 4);
        helm.addEnchantment(Enchantment.UNBREAKING, 3);
        player.getInventory().setHelmet(helm);

        ItemStack brustplatte = new ItemStack(Material.NETHERITE_CHESTPLATE);
        brustplatte.addEnchantment(Enchantment.PROTECTION, 4);
        brustplatte.addEnchantment(Enchantment.UNBREAKING, 3);
        player.getInventory().setChestplate(brustplatte);

        // Elytra ins Inventar (verzaubert) + Feuerwerksraketen
        ItemStack elytra = new ItemStack(Material.ELYTRA);
        elytra.addEnchantment(Enchantment.UNBREAKING, 3);
        elytra.addEnchantment(Enchantment.MENDING, 1);
        player.getInventory().addItem(elytra);
        player.getInventory().addItem(new ItemStack(Material.FIREWORK_ROCKET, 64));

        ItemStack hose = new ItemStack(Material.NETHERITE_LEGGINGS);
        hose.addEnchantment(Enchantment.PROTECTION, 4);
        hose.addEnchantment(Enchantment.UNBREAKING, 3);
        player.getInventory().setLeggings(hose);

        ItemStack stiefel = new ItemStack(Material.NETHERITE_BOOTS);
        stiefel.addEnchantment(Enchantment.PROTECTION, 4);
        stiefel.addEnchantment(Enchantment.UNBREAKING, 3);
        stiefel.addEnchantment(Enchantment.FEATHER_FALLING, 4);
        player.getInventory().setBoots(stiefel);
    }

}
