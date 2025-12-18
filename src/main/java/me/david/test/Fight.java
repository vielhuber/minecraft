package me.david.test;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@SuppressWarnings({"deprecation", "DataFlowIssue"})
@ParametersAreNonnullByDefault

public final class Fight implements Listener, CommandExecutor {

    private JavaPlugin plugin;
    private Machtklinge machtklinge;
    private Donneraxt donneraxt;
    private Random random = new Random();

    // Scoring System
    private Map<UUID, Integer> scores = new HashMap<>();

    // Aktive Fight-Daten
    private Player activeHunter = null;
    private Player activeWeglaeufer = null;
    private BukkitRunnable activeTimer = null;
    private BukkitRunnable musicTask = null;
    private boolean fightActive = false;

    public void register(JavaPlugin plugin, Machtklinge machtklinge, Donneraxt donneraxt) {
        this.plugin = plugin;
        this.machtklinge = machtklinge;
        this.donneraxt = donneraxt;

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

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!fightActive) return;

        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        // Wegläufer wurde vom Hunter getötet
        if (victim.equals(activeWeglaeufer) && killer != null && killer.equals(activeHunter)) {
            addScore(activeHunter, 1);
            endFight(activeHunter.getName() + " hat den Wegläufer getötet! +1 Punkt");
        }
        // Hunter wurde vom Wegläufer getötet
        else if (victim.equals(activeHunter) && killer != null && killer.equals(activeWeglaeufer)) {
            addScore(activeWeglaeufer, 2);
            endFight(activeWeglaeufer.getName() + " hat den Hunter getötet! +2 Punkte");
        }
        // Einer der Spieler ist gestorben (anderer Grund)
        else if (victim.equals(activeWeglaeufer) || victim.equals(activeHunter)) {
            endFight("Der Fight wurde beendet (Spieler gestorben)");
        }
    }

    private void addScore(Player player, int points) {
        UUID uuid = player.getUniqueId();
        scores.put(uuid, scores.getOrDefault(uuid, 0) + points);
    }

    private void displayScores() {
        Bukkit.broadcastMessage(ChatColor.GOLD + "═══════════════════════════");
        Bukkit.broadcastMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "SCOREBOARD:");

        // Scores sortieren (höchste zuerst)
        List<Map.Entry<UUID, Integer>> sortedScores = new ArrayList<>(scores.entrySet());
        sortedScores.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        if (sortedScores.isEmpty()) {
            Bukkit.broadcastMessage(ChatColor.GRAY + "Noch keine Punkte vergeben");
        } else {
            for (Map.Entry<UUID, Integer> entry : sortedScores) {
                Player player = Bukkit.getPlayer(entry.getKey());
                String playerName = player != null ? player.getName() : "Unbekannt";
                Bukkit.broadcastMessage(ChatColor.YELLOW + playerName + ": " + ChatColor.WHITE + entry.getValue() + " Punkte");
            }
        }

        Bukkit.broadcastMessage(ChatColor.GOLD + "═══════════════════════════");
    }

    private void startMusic() {
        // Musik-Loop starten - Pigstep dauert 149 Sekunden (2980 Ticks)
        musicTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!fightActive) {
                    this.cancel();
                    return;
                }

                // Musik für alle Online-Spieler abspielen - direkt am Spieler, nicht an einer Location
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.playSound(player, Sound.MUSIC_DISC_PIGSTEP, SoundCategory.MASTER, 1.0f, 1.0f);
                }
            }
        };
        // Sofort starten, dann alle 2980 Ticks (149 Sekunden) wiederholen
        musicTask.runTaskTimer(plugin, 0L, 2980L);
    }

    private void stopMusic() {
        if (musicTask != null) {
            musicTask.cancel();
            musicTask = null;
        }

        // Musik für alle Spieler stoppen
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.stopSound(Sound.MUSIC_DISC_PIGSTEP, SoundCategory.MASTER);
        }
    }

    public void stopFight() {
        endFight("Fight wurde gestoppt");
    }

    private void endFight(String reason) {
        if (!fightActive) return;

        fightActive = false;

        // Timer stoppen
        if (activeTimer != null) {
            activeTimer.cancel();
            activeTimer = null;
        }

        // Musik stoppen
        stopMusic();

        // Alle Spieler zurück in Spectator setzen
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setGameMode(GameMode.SPECTATOR);
        }

        Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + reason);
        displayScores();

        activeHunter = null;
        activeWeglaeufer = null;
    }

    private ItemStack erstelleTrident() {
        ItemStack trident = new ItemStack(Material.TRIDENT);

        // Alle Trident-Verzauberungen hinzufügen
        trident.addEnchantment(Enchantment.IMPALING, 5);        // Aufspießen
        trident.addEnchantment(Enchantment.LOYALTY, 3);         // Treue
        trident.addEnchantment(Enchantment.CHANNELING, 1);      // Kanalisierung
        trident.addEnchantment(Enchantment.UNBREAKING, 3);      // Haltbarkeit
        trident.addEnchantment(Enchantment.MENDING, 1);         // Reparatur

        return trident;
    }

    private void startFight() {
        // Aktiven Fight beenden, falls vorhanden
        if (fightActive) {
            endFight("Neuer Fight wurde gestartet");
        }

        // Alle Online-Spieler holen
        List<Player> allPlayers = new ArrayList<>(Bukkit.getOnlinePlayers());

        if (allPlayers.isEmpty()) {
            Bukkit.broadcastMessage(ChatColor.RED + "Es muss mindestens 1 Spieler online sein!");
            return;
        }

        // Aktuelle Scores anzeigen
        displayScores();

        // Alle Spieler in Spectator Modus setzen
        for (Player player : allPlayers) {
            player.setGameMode(GameMode.SPECTATOR);
        }

        // Spieler auswählen
        Player weglaeufer = allPlayers.remove(random.nextInt(allPlayers.size()));
        Player hunter;

        if (allPlayers.isEmpty()) {
            // Nur 1 Spieler online - er bekommt eine zufällige Rolle
            hunter = weglaeufer;
            boolean isOnlyHunter = random.nextBoolean();
            if (isOnlyHunter) {
                weglaeufer = null;
            } else {
                hunter = null;
            }
        } else {
            hunter = allPlayers.remove(random.nextInt(allPlayers.size()));
        }

        // Spawn-Location festlegen
        World world = Bukkit.getWorlds().get(0);
        Location spawnLocation = world.getSpawnLocation();

        // Wegläufer ausstatten
        if (weglaeufer != null) {
            weglaeufer.setGameMode(GameMode.SURVIVAL);
            weglaeufer.getInventory().clear();
            weglaeufer.getInventory().setArmorContents(null);

            // Herzen und Essen auffüllen
            weglaeufer.setHealth(20.0);
            weglaeufer.setFoodLevel(20);
            weglaeufer.setSaturation(20.0f);

            weglaeufer.teleport(spawnLocation);

            // Wassereimer
            ItemStack waterBucket = new ItemStack(Material.WATER_BUCKET);
            weglaeufer.getInventory().addItem(waterBucket);

            // Stack Goldkarotten (64)
            ItemStack goldenCarrots = new ItemStack(Material.GOLDEN_CARROT, 64);
            weglaeufer.getInventory().addItem(goldenCarrots);

            // 3 Enderperlen
            ItemStack enderPearls = new ItemStack(Material.ENDER_PEARL, 3);
            weglaeufer.getInventory().addItem(enderPearls);

            // Spinnennetze
            ItemStack cobwebs = new ItemStack(Material.COBWEB, 64);
            weglaeufer.getInventory().addItem(cobwebs);

            // Diamant Schild mit Verzauberungen
            ItemStack shield = new ItemStack(Material.SHIELD);
            shield.addEnchantment(Enchantment.UNBREAKING, 3);
            weglaeufer.getInventory().setItemInOffHand(shield);

            // Diamant Rüstung mit Verzauberungen
            ItemStack diaHelmet = new ItemStack(Material.DIAMOND_HELMET);
            ItemStack diaChestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
            ItemStack diaLeggings = new ItemStack(Material.DIAMOND_LEGGINGS);
            ItemStack diaBoots = new ItemStack(Material.DIAMOND_BOOTS);

            diaHelmet.addEnchantment(Enchantment.PROTECTION, 4);
            diaHelmet.addEnchantment(Enchantment.UNBREAKING, 3);
            diaChestplate.addEnchantment(Enchantment.PROTECTION, 4);
            diaChestplate.addEnchantment(Enchantment.UNBREAKING, 3);
            diaLeggings.addEnchantment(Enchantment.PROTECTION, 4);
            diaLeggings.addEnchantment(Enchantment.UNBREAKING, 3);
            diaBoots.addEnchantment(Enchantment.PROTECTION, 4);
            diaBoots.addEnchantment(Enchantment.UNBREAKING, 3);
            diaBoots.addEnchantment(Enchantment.FEATHER_FALLING, 4);

            weglaeufer.getInventory().setHelmet(diaHelmet);
            weglaeufer.getInventory().setChestplate(diaChestplate);
            weglaeufer.getInventory().setLeggings(diaLeggings);
            weglaeufer.getInventory().setBoots(diaBoots);
        }

        // Hunter ausstatten
        if (hunter != null) {
            hunter.setGameMode(GameMode.SURVIVAL);
            hunter.getInventory().clear();
            hunter.getInventory().setArmorContents(null);

            // Herzen und Essen auffüllen
            hunter.setHealth(20.0);
            hunter.setFoodLevel(20);
            hunter.setSaturation(20.0f);

            // Etwas versetzt spawnen, wenn beide Spieler existieren
            Location hunterLocation = weglaeufer != null ? spawnLocation.clone().add(5, 0, 0) : spawnLocation;
            hunter.teleport(hunterLocation);

            // Trident voll verzaubert
            ItemStack trident = erstelleTrident();
            hunter.getInventory().addItem(trident);

            // Maze (Machtklinge) voll verzaubert
            ItemStack maze = machtklinge.erstelleMachtklinge();
            hunter.getInventory().addItem(maze);

            // Donneraxt voll verzaubert
            ItemStack donneraxtItem = donneraxt.erstelleDonneraxt();
            hunter.getInventory().addItem(donneraxtItem);

            // Stack Goldäpfel (64)
            ItemStack goldenApples = new ItemStack(Material.GOLDEN_APPLE, 64);
            hunter.getInventory().addItem(goldenApples);

            // Keine Rüstung für Hunter
        }

        // Fight-Daten setzen
        activeHunter = hunter;
        activeWeglaeufer = weglaeufer;
        fightActive = true;

        // Nachricht broadcasten
        Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Der Man-Hunt beginnt!");
        if (hunter != null && weglaeufer != null) {
            Bukkit.broadcastMessage(ChatColor.YELLOW + "Jäger: " + hunter.getName());
            Bukkit.broadcastMessage(ChatColor.YELLOW + "Gejagter: " + weglaeufer.getName());
        } else if (hunter != null) {
            Bukkit.broadcastMessage(ChatColor.YELLOW + "Jäger: " + hunter.getName() + ChatColor.GRAY + " (Alleine)");
        } else {
            Bukkit.broadcastMessage(ChatColor.YELLOW + "Gejagter: " + weglaeufer.getName() + ChatColor.GRAY + " (Alleine)");
        }
        Bukkit.broadcastMessage(ChatColor.AQUA + "Timer: 3:00 Minuten");

        // Musik starten
        startMusic();

        // 3 Minuten Timer starten (180 Sekunden = 3600 Ticks)
        final Player finalWeglaeufer = weglaeufer;
        activeTimer = new BukkitRunnable() {
            int secondsLeft = 180;

            @Override
            public void run() {
                secondsLeft--;

                // Countdown-Nachrichten bei bestimmten Zeiten
                if (secondsLeft == 120) {
                    Bukkit.broadcastMessage(ChatColor.GOLD + "⏰ Noch 2 Minuten!");
                } else if (secondsLeft == 60) {
                    Bukkit.broadcastMessage(ChatColor.RED + "⏰ Noch 1 Minute!");
                } else if (secondsLeft == 45) {
                    Bukkit.broadcastMessage(ChatColor.RED + "⏰ Noch 45 Sekunden!");
                } else if (secondsLeft == 30) {
                    Bukkit.broadcastMessage(ChatColor.RED + "⏰ Noch 30 Sekunden!");
                } else if (secondsLeft == 20) {
                    Bukkit.broadcastMessage(ChatColor.RED + "⏰ Noch 20 Sekunden!");
                } else if (secondsLeft == 10) {
                    Bukkit.broadcastMessage(ChatColor.RED + "⏰ Noch 10 Sekunden!");
                } else if (secondsLeft <= 5 && secondsLeft > 0) {
                    Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + secondsLeft + "...");
                } else if (secondsLeft > 60 && secondsLeft % 5 == 0) {
                    // Alle 5 Sekunden eine Anzeige (über 1 Minute)
                    int minutes = secondsLeft / 60;
                    int seconds = secondsLeft % 60;
                    Bukkit.broadcastMessage(ChatColor.GRAY + "⏰ " + minutes + ":" + String.format("%02d", seconds));
                }

                // Timer abgelaufen - Wegläufer hat überlebt
                if (secondsLeft <= 0) {
                    if (finalWeglaeufer != null && finalWeglaeufer.isOnline() && fightActive) {
                        addScore(finalWeglaeufer, 1);
                        endFight(finalWeglaeufer.getName() + " hat überlebt! +1 Punkt");
                    } else {
                        endFight("Zeit abgelaufen!");
                    }
                    this.cancel();
                }
            }
        };
        activeTimer.runTaskTimer(plugin, 0L, 20L); // Jede Sekunde (20 Ticks)
    }
}
