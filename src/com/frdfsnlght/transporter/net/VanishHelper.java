package com.frdfsnlght.transporter.net;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.kitteh.vanish.event.VanishStatusChangeEvent;
import org.kitteh.vanish.staticaccess.VanishNoPacket;
import org.kitteh.vanish.staticaccess.VanishNotLoadedException;

import com.frdfsnlght.transporter.Global;
import com.frdfsnlght.transporter.TabList;

public class VanishHelper implements Listener {

    public static boolean isVanished(String playerName) {
        try {
            return VanishNoPacket.isVanished(playerName);
        } catch (VanishNotLoadedException e) {
            return false;
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onVanishStatusChange(VanishStatusChangeEvent event) {
        Global.plugin.getServer().getScheduler().runTaskLater(Global.plugin, new Thread() {
            @Override
            public void run() {
                TabList.updateAll();
            }
        }, 1L);
    }
}
