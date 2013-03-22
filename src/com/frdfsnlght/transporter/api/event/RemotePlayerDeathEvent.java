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
package com.frdfsnlght.transporter.api.event;

import com.frdfsnlght.transporter.api.RemotePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired when a remote player dies.
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class RemotePlayerDeathEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    /**
     * Returns the list of event handlers for this event.
     *
     * @return the list of event handlers for this event
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }

    private RemotePlayer player;

    /**
     * Creates the event.
     *
     * @param player the player
     */
    public RemotePlayerDeathEvent(RemotePlayer player) {
        this.player = player;
    }

    /**
     * Returns the {@link RemotePlayer} object of the player that died.
     *
     * @return the player
     */
    public RemotePlayer getRemotePlayer() {
        return player;
    }

    /**
     * Returns the list of event handlers for this event.
     *
     * @return the list of event handlers for this event
     */
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
