package de.jakob48;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class restartcountdown extends JavaPlugin {

    private BukkitRunnable countdownTask = null; // Referenz auf aktuellen Countdown

    @Override
    public void onEnable() {
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
            sender.sendMessage("§cVerwendung: /restartserver <Zeit> oder /restartserver stop");
            return false;
        }

        if (args[0].equalsIgnoreCase("stop")) {
            if (countdownTask != null) {
                countdownTask.cancel();
                countdownTask = null;
                Bukkit.broadcastMessage("§cCountdown wurde gestoppt!");
            } else {
                sender.sendMessage("§cEs läuft kein Countdown!");
            }
            return true;
        }

        long seconds = parseTime(args[0]);
        if (seconds <= 0) {
            sender.sendMessage("§cUngültige Zeitangabe!");
            return false;
        }

        sender.sendMessage("§aServerneustart in " + formatTime(seconds) + " gestartet!");
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

    private void startCountdown(long seconds) {
        if (countdownTask != null) countdownTask.cancel();

        countdownTask = new BukkitRunnable() {
            long timeLeft = seconds;

            @Override
            public void run() {
                if (timeLeft <= 0) {
                    Bukkit.broadcastMessage("§cServer wird jetzt neu gestartet!");
                    Bukkit.shutdown();
                    cancel();
                    countdownTask = null;
                    return;
                }

                if (timeLeft == seconds) {
                    Bukkit.broadcastMessage("§eServerneustart in " + formatTime(timeLeft) + "!");
                }
                else if (timeLeft > 600 && timeLeft % 600 == 0) {
                    Bukkit.broadcastMessage("§eServerneustart in " + formatTime(timeLeft) + "!");
                }
                else if (timeLeft <= 600 && timeLeft >= 60 && timeLeft % 60 == 0) {
                    Bukkit.broadcastMessage("§eServerneustart in " + formatTime(timeLeft) + "!");
                }
                else if (timeLeft == 20) {
                    Bukkit.broadcastMessage("§cServerneustart in 20 Sekunden!");
                }
                else if (timeLeft <= 10) {
                    Bukkit.broadcastMessage("§cServerneustart in " + timeLeft + " Sekunden!");
                }

                timeLeft--;
            }
        };

        countdownTask.runTaskTimer(this, 0, 20); // 20 Ticks = 1 Sekunde
    }

    private String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        StringBuilder sb = new StringBuilder();
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (secs > 0) sb.append(secs).append("s");
        return sb.toString().trim();
    }
}
