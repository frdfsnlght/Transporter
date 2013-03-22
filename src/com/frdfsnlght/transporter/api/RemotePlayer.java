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

import org.bukkit.entity.Player;

/**
 * Represents an online player on a remote server.
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public interface RemotePlayer {

    /**
     * Returns the player's name.
     *
     * @return the player's name
     */
    public String getName();

    /**
     * Returns the player's display name.
     *
     * @return the player's display name
     */
    public String getDisplayName();

    /**
     * Returns the world where the player is located.
     *
     * @return the world where the player is located
     */
    public RemoteWorld getRemoteWorld();

    /**
     * Returns the server where the player is located.
     *
     * @return the server where the player is located
     */
    public RemoteServer getRemoteServer();

    /**
     * Returns the player's prefix on the remote server.
     *
     * @return the player's prefix
     */
    public String getPrefix();

    /**
     * Returns the player's suffix on the remote server.
     *
     * @return the player's suffix
     */
    public String getSuffix();

    /**
     * Returns the location where the player is located.
     *
     * @param cb    the callback to use when the call completes
     */
    public void getRemoteLocation(Callback<RemoteLocation> cb);

    /**
     * Sends a message to the player.
     *
     * @param cb    the callback to use when the call completes
     * @param msg   the message to send
     */
    public void sendMessage(Callback<Void> cb, String msg);

    /**
     * Sends a raw message to the player.
     *
     * @param cb    the callback to use when the call completes
     * @param msg   the message to send
     */
    public void sendRawMessage(Callback<Void> cb, String msg);

    /**
     * Sends a private message to the player.
     *
     * @param fromPlayer    the player sending the message
     * @param message       the message to send
     */
    public void sendPM(Player fromPlayer, String message);

}
