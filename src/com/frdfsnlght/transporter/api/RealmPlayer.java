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
package com.frdfsnlght.transporter.api;

import java.util.Calendar;
import org.bukkit.GameMode;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

/**
 * This class represents a player record in the realm database.
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public interface RealmPlayer {

    public String getName();
    public String getDisplayName();
    public String getAddress();
    public ItemStack[] getInventory();
    public ItemStack[] getArmor();
    public int getHeldItemSlot();
    public int getHealth();
    public int getRemainingAir();
    public int getFireTicks();
    public int getFoodLevel();
    public float getExhaustion();
    public float getSaturation();
    public GameMode getGameMode();
    public int getLevel();
    public float getExp();
    public int getTotalExperience();
    public PotionEffect[] getPotionEffects();

    public String getLastServer();
    public String getLastWorld();
    public String getLastLocation();
    public String getHome();
    public Calendar getLastJoin();
    public Calendar getLastQuit();
    public Calendar getLastKick();
    public Calendar getLastDeath();
    public int getDeaths();
    public String getLastDeathMessage();
    public Calendar getLastPlayerKill();
    public int getPlayerKills();
    public String getLastPlayerKilled();
    public Calendar getLastMobKill();
    public int getMobKills();
    public String getLastMobKilled();
    public Calendar getLastUpdated();

}
