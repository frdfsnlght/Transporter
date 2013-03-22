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
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired when a private message is sent to a local player from a remote player.
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class RemotePlayerPMEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    /**
     * Returns the list of event handlers for this event.
     *
     * @return the list of event handlers for this event
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }

    private RemotePlayer fromPlayer;
    private Player toPlayer;
    private String message;
    private boolean cancelled = false;

    /**
     * Creates the event.
     *
     * @param fromPlayer    the player sending the message
     * @param toPlayer      the player receiving the message
     * @param message       the message
     */
    public RemotePlayerPMEvent(RemotePlayer fromPlayer, Player toPlayer, String message) {
        this.fromPlayer = fromPlayer;
        this.toPlayer = toPlayer;
        this.message = message;
    }

    /**
     * Returns the {@link RemotePlayer} object of the player that sent the message.
     *
     * @return the player
     */
    public RemotePlayer getFromPlayer() {
        return fromPlayer;
    }

    /**
     * Returns the <code>Player</code> object of the player that will receive the message.
     *
     * @return the player
     */
    public Player getToPlayer() {
        return toPlayer;
    }

    /**
     * Returns the message being sent.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets a flag to indicate the player should not receive the message.
     *
     * @param b     true to cancel the event
     */
    @Override
    public void setCancelled(boolean b) {
        cancelled = b;
    }

    /**
     * Returns whether or not the event was canceled.
     *
     * @return true if the event is canceled
     */
    @Override
    public boolean isCancelled() {
        return cancelled;
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
