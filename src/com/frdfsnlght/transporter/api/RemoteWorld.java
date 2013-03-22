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

import org.bukkit.Difficulty;
import org.bukkit.World.Environment;

/**
 * Represents a world on a remote server.
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public interface RemoteWorld {

    /**
     * Returns the server where the world is located.
     *
     * @return the server where the world is located
     */
    public RemoteServer getRemoteServer();

    /**
     * Returns the name of the world.
     *
     * @return the name of the world.
     */
    public String getName();

    /**
     * Returns the difficulty setting of the world.
     *
     * @param cb    the callback to use when the call completes
     */
    public void getDifficulty(Callback<Difficulty> cb);

    /**
     * Returns the environment setting of the world.
     *
     * @param cb    the callback to use when the call completes
     */
    public void getEnvironment(Callback<Environment> cb);

    /**
     * Returns the full time of the world.
     *
     * @param cb    the callback to use when the call completes
     */
    public void getFullTime(Callback<Long> cb);

    /**
     * Returns the seed setting of the world.
     *
     * @param cb    the callback to use when the call completes
     */
    public void getSeed(Callback<Long> cb);

    /**
     * Returns the time setting of the world.
     *
     * @param cb    the callback to use when the call completes
     */
    public void getTime(Callback<Long> cb);

}
