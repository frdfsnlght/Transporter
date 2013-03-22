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

import org.bukkit.Location;
import org.bukkit.Material;

/**
 * Represents a local gate of the AREA type.
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public interface LocalAreaGate extends LocalGate {

    /**
     * Resizes the gate's portal volume by the specified number of blocks in the
     * specified direction.
     * <p>
     * The number of blocks can be negative.
     *
     * @param num   the number of blocks to add
     * @param dir   the direction to add blocks
     */
    public void resize(int num, ExpandDirection dir);

    /**
     * Returns the value of the "p1" option.
     *
     * @return      the option value
     */
    public String getP1();

    /**
     * Sets the value of the "p1" option.
     *
     * @param s      the option value
     */
    public void setP1(String s);

    /**
     * Returns the value of the "p1" option.
     *
     * @return      the option value
     */
    public Location getP1Location();

    /**
     * Sets the value of the "p1" option.
     *
     * @param l      the option value
     */
    public void setP1Location(Location l);

    /**
     * Returns the value of the "p2" option.
     *
     * @return      the option value
     */
    public String getP2();

    /**
     * Sets the value of the "p2" option.
     *
     * @param s      the option value
     */
    public void setP2(String s);

    /**
     * Returns the value of the "p2" option.
     *
     * @return      the option value
     */
    public Location getP2Location();

    /**
     * Sets the value of the "p2" option.
     *
     * @param l      the option value
     */
    public void setP2Location(Location l);

    /**
     * Returns the value of the "spawnDirection" option.
     *
     * @return      the option value
     */
    public SpawnDirection getSpawnDirection();

    /**
     * Sets the value of the "spawnDirection" option.
     *
     * @param dir      the option value
     */
    public void setSpawnDirection(SpawnDirection dir);

    /**
     * Returns the value of the "spawnAir" option.
     *
     * @return      the option value
     */
    public boolean getSpawnAir();

    /**
     * Sets the value of the "spawnAir" option.
     *
     * @param b      the option value
     */
    public void setSpawnAir(boolean b);

    /**
     * Returns the value of the "spawnSolid" option.
     *
     * @return      the option value
     */
    public boolean getSpawnSolid();

    /**
     * Sets the value of the "spawnSolid" option.
     *
     * @param b      the option value
     */
    public void setSpawnSolid(boolean b);

    /**
     * Returns the value of the "spawnLiquid" option.
     *
     * @return      the option value
     */
    public boolean getSpawnLiquid();

    /**
     * Sets the value of the "spawnLiquid" option.
     *
     * @param b      the option value
     */
    public void setSpawnLiquid(boolean b);

    /**
     * Returns the value of the "spawnSearch" option.
     *
     * @return      the option value
     */
    public SpawnSearch getSpawnSearch();

    /**
     * Sets the value of the "spawnSearch" option.
     *
     * @param s      the option value
     */
    public void setSpawnSearch(SpawnSearch s);

    /**
     * Returns the value of the "box" option.
     *
     * @return      the option value
     */
    public boolean getBox();

    /**
     * Sets the value of the "box" option.
     *
     * @param b      the option value
     */
    public void setBox(boolean b);

    /**
     * Returns the value of the "boxMaterial" option.
     *
     * @return      the option value
     */
    public Material getBoxMaterial();

    /**
     * Sets the value of the "boxMaterial" option.
     *
     * @param m      the option value
     */
    public void setBoxMaterial(Material m);

}
