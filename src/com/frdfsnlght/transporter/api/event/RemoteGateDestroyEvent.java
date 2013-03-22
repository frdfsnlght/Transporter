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

import com.frdfsnlght.transporter.api.RemoteGate;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired when a remote gate is destroyed.
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class RemoteGateDestroyEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    /**
     * Returns the list of event handlers for this event.
     *
     * @return the list of event handlers for this event
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }

    private RemoteGate gate;

    /**
     * Creates the event.
     *
     * @param gate  the gate that was destroyed
     */
    public RemoteGateDestroyEvent(RemoteGate gate) {
        this.gate = gate;
    }

    /**
     * Returns the gate that was destroyed.
     *
     * @return the gate that was destroyed
     */
    public RemoteGate getRemoteGate() {
        return gate;
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
