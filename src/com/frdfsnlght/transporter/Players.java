/*
 * Copyright 2012 frdfsnlght <frdfsnlght@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.frdfsnlght.transporter;

import com.frdfsnlght.transporter.api.RemotePlayer;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class Players {

    public static Player findLocal(String name) {
        Player player = Global.plugin.getServer().getPlayer(name);
        if (player != null) return player;
        name = name.toLowerCase();
        for (Player p : Global.plugin.getServer().getOnlinePlayers())
            if (p.getName().toLowerCase().startsWith(name)) {
                if (player != null) return null;
                player = p;
            }
        return player;
    }

    public static RemotePlayerImpl findRemote(String name) {
        Map<String,RemotePlayerImpl> players = new HashMap<String,RemotePlayerImpl>();
        for (Server server : Servers.getAll())
            for (RemotePlayer p : server.getRemotePlayers())
                players.put(p.getName().toLowerCase(), (RemotePlayerImpl)p);
        name = name.toLowerCase();
        if (players.containsKey(name)) return players.get(name);
        RemotePlayerImpl player = null;
        for (String pname : players.keySet())
            if (pname.startsWith(name)) {
                if (player != null) return null;
                player = players.get(pname);
            }
        return player;
    }

    public static void restore(Player player, TypeMap data) {
        player.setHealth(data.getInt("health", 20));
        player.setRemainingAir(data.getInt("remainingAir", 300));
        player.setFoodLevel(data.getInt("foodLevel", 20));
        player.setExhaustion(data.getFloat("exhaustion", 0));
        player.setSaturation(data.getFloat("saturation", 0));
        player.setFireTicks(data.getInt("fireTicks", 0));
        player.setLevel(data.getInt("level", 0));
        player.setExp(data.getFloat("exp", 0));
        ItemStack[] inventory = Inventory.decodeItemStackArray(data.getMapList("inventory"));
        if (inventory != null) {
            PlayerInventory inv = player.getInventory();
            for (int slot = 0; slot < inventory.length; slot++) {
                if (inventory[slot] == null)
                    inv.setItem(slot, new ItemStack(Material.AIR.getId()));
                else
                    inv.setItem(slot, inventory[slot]);
            }
        }
        ItemStack[] armor = Inventory.decodeItemStackArray(data.getMapList("armor"));
        if (armor != null) {
            PlayerInventory inv = player.getInventory();
            inv.setArmorContents(armor);
        }
        PotionEffect[] potionEffects = PotionEffects.decodePotionEffects(data.getMapList("potionEffects"));
        if (potionEffects != null) {
            for (PotionEffectType pet : PotionEffectType.values()) {
                if (pet == null) continue;
                if (player.hasPotionEffect(pet))
                    player.removePotionEffect(pet);
            }
            for (PotionEffect effect : potionEffects) {
                if (effect == null) continue;
                player.addPotionEffect(effect);
            }
        }
    }

}
