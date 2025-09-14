package de.jakob48;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public class restartcountdown extends JavaPlugin {

    private BukkitRunnable countdownTask = null;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getLogger().info("RestartCountdown Plugin aktiviert!");
    }

    @Override
    public void onDisable() {
        getLogger().info("RestartCountdown Plugin deaktiviert!");
        if (countdownTask != null) countdownTask.cancel();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("restartserver")) return false;

        if (args.length == 0) {
            sender.sendMessage(getConfig().getString("messages.usage", "§cVerwendung: /restartserver <Zeit> oder /restartserver stop"));
            return true;
        }

        // Countdown stoppen
        if (args[0].equalsIgnoreCase("stop")) {
            if (countdownTask != null) {
                countdownTask.cancel();
                countdownTask = null;
                Bukkit.broadcastMessage(getConfig().getString("messages.stop", "§cCountdown wurde gestoppt!"));
            } else {
                sender.sendMessage("§cEs läuft kein Countdown!");
            }
            return true;
        }

        // Zeit parsen
        long seconds = parseTime(args[0]);
        if (seconds <= 0) {
            sender.sendMessage(getConfig().getString("messages.invalid_time", "§cUngültige Zeitangabe!"));
            return true;
        }

        // Countdown starten
        startCountdown(seconds);
        return true;
    }

    private long parseTime(String input) {
        try {
            if (input.endsWith("s")) return Long.parseLong(input.replace("s", ""));
            if (input.endsWith("m")) return Long.parseLong(input.replace("m", "")) * 60;
            if (input.endsWith("h")) return Long.parseLong(input.replace("h", "")) * 3600;
        } catch (NumberFormatException e) {
            return -1;
        }
        return -1;
    }

    private void startCountdown(long totalSeconds) {
        if (countdownTask != null) countdownTask.cancel();

        // Halbzeit-/Viertel-/Achtel-Marken vorberechnen
        // Halbzeit-/Viertel-/Achtel-Marken vorberechnen
        Set<Long> halbzeitMarken = new HashSet<>();
        long check = totalSeconds / 2;
        while (check >= 60) { // nur solange >= 1 Minute
            halbzeitMarken.add(check);
            check /= 2;
        }


        // Feste Meldungen (z.B. 20, 10, 5 Minuten)
        Set<Long> festeMarken = new HashSet<>();
        festeMarken.add(1200L); // 20 Minuten
        festeMarken.add(600L);  // 10 Minuten
        festeMarken.add(300L);  // 5 Minuten

        // Bereits angekündigte Zeiten merken, um Doppelmeldungen zu verhindern
        Set<Long> announced = new HashSet<>();

        countdownTask = new BukkitRunnable() {
            long timeLeft = totalSeconds;

            @Override
            public void run() {
                if (timeLeft <= 0) {
                    Bukkit.broadcastMessage(getConfig().getString("messages.restart", "§cServer wird jetzt neu gestartet!"));
                    Bukkit.shutdown();
                    cancel();
                    countdownTask = null;
                    return;
                }

                String msg = null;

                // Startmeldung
                if (timeLeft == totalSeconds && !announced.contains(timeLeft)) {
                    msg = getConfig().getString("messages.start", "§eServerneustart in %time% gestartet!");
                    announced.add(timeLeft);
                }
                // Feste Meldungen
                else if (festeMarken.contains(timeLeft) && !announced.contains(timeLeft)) {
                    msg = getConfig().getString("messages.fixed_" + timeLeft, "§eServerneustart in %time%!");
                    announced.add(timeLeft);
                }
                // Halbzeit-/Viertel-/Achtel-Meldungen
                else if (halbzeitMarken.contains(timeLeft) && !announced.contains(timeLeft)) {
                    msg = getConfig().getString("messages.half", "§6Serverneustart in %time%! (Halbzeit)");
                    announced.add(timeLeft);
                }
                // Ab 10 Minuten jede volle Minute
                else if (timeLeft > 60 && timeLeft <= 600 && timeLeft % 60 == 0 && !announced.contains(timeLeft)) {
                    msg = getConfig().getString("messages.minute", "§eServerneustart in %time%!");
                    announced.add(timeLeft);
                }
                // Ab 1 Minute jede volle Minute
                else if (timeLeft > 20 && timeLeft <= 60 && timeLeft % 60 == 0 && !announced.contains(timeLeft)) {
                    msg = getConfig().getString("messages.minute", "§eServerneustart in %time%!");
                    announced.add(timeLeft);
                }
                // Bei genau 20 Sekunden
                else if (timeLeft == 20 && !announced.contains(timeLeft)) {
                    msg = getConfig().getString("messages.seconds_20", "§cAchtung! Der Server wird in 20 Sekunden neu gestartet!");
                    announced.add(timeLeft);
                }
                // Letzte 10 Sekunden, jede Sekunde
                else if (timeLeft <= 10 && !announced.contains(timeLeft)) {
                    msg = getConfig().getString("messages.seconds_final", "§4Serverneustart in %time% Sekunden!");
                    announced.add(timeLeft);
                }

                if (msg != null) {
                    Bukkit.broadcastMessage(msg.replace("%time%", formatTime(timeLeft)));
                }

                timeLeft--;
            }
        };

        countdownTask.runTaskTimer(this, 0, 20);
    }

    private String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        StringBuilder sb = new StringBuilder();
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (secs > 0 || sb.length() == 0) sb.append(secs).append("s");
        return sb.toString().trim();
    }
}
