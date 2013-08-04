/*
 * Copyright (C) 2013 Dabo Ross <http://www.daboross.net/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.daboross.bukkitdev.permslow;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.mcstats.MetricsLite;

/**
 *
 * @author daboross
 */
public class PermSlowPlugin extends JavaPlugin implements Listener {

    private final Map<String, BukkitTask> map = new HashMap<String, BukkitTask>();

    @Override
    public void onEnable() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(this, this);
        MetricsLite metrics = null;
        try {
            metrics = new MetricsLite(this);
        } catch (IOException ex) {
            getLogger().log(Level.WARNING, "Unable to create Metrics", ex);
        }
        if (metrics != null) {
            metrics.start();
        }
    }

    @Override
    public void onDisable() {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        sender.sendMessage("PermSlow doesn't know about the command /" + cmd.getName());
        return true;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent evt) {
        runIt(evt.getPlayer().getName());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent evt) {
        runIt(evt.getPlayer().getName());
    }

    private void runIt(final String username) {
        BukkitTask lastTask = map.get(username.toLowerCase());
        if (lastTask != null) {
            lastTask.cancel();
        }
        int duration = 20 * (int) TimeUnit.MINUTES.toSeconds(20);
        final PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, duration, 3);
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                Player p = Bukkit.getPlayer(username);
                if (p != null) {
                    p.addPotionEffect(effect, true);
                } else {
                    this.cancel();
                }
            }
        };
        map.put(username.toLowerCase(), runnable.runTaskTimer(this, 0, duration));
    }
}
