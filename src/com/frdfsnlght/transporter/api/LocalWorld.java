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

import org.bukkit.World;
import org.bukkit.World.Environment;

/**
 * Represents a world on the local server.
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public interface LocalWorld {

    /**
     * Returns the name of the world.
     *
     * @return the name of the world
     */
    public String getName();

    /**
     * Returns the <code>World</code> object for this world.
     *
     * @return the World object
     */
    public World getWorld();

    /**
     * Returns true if this world is currently loaded on this server.
     *
     * @return true if the world is loaded
     */
    public boolean isLoaded();

    /* Options */

    /**
     * Returns the value of the "environment" option.
     *
     * @return      the option value
     */
    public Environment getEnvironment();

    /**
     * Sets the value of the "environment" option.
     *
     * @param env   the option value
     */
    public void setEnvironment(Environment env);

    /**
     * Returns the value of the "generator" option.
     *
     * @return      the option value
     */
    public String getGenerator();

    /**
     * Sets the value of the "generator" option.
     *
     * @param generator     the option value
     */
    public void setGenerator(String generator);

    /**
     * Returns the value of the "seed" option.
     *
     * @return      the option value
     */
    public String getSeed();

    /**
     * Sets the value of the "seed" option.
     *
     * @param seed  the option value
     */
    public void setSeed(String seed);

    /**
     * Returns the value of the "autoLoad" option.
     *
     * @return      the option value
     */
    public boolean getAutoLoad();

    /**
     * Sets the value of the "autoLoad" option.
     *
     * @param b     the option value
     */
    public void setAutoLoad(boolean b);

    /* End Options */

}
