/*
 * Copyright 2011 frdfsnlght <frdfsnlght@gmail.com>.
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

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public class WorldListenerImpl implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldLoad(WorldLoadEvent event) {
        Utils.debug("world '%s' loaded", event.getWorld().getName());
        Gates.loadGatesForWorld(new Context(), event.getWorld());
        for (Server server : Servers.getAll())
            server.sendWorldLoad(event.getWorld());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldUnload(WorldUnloadEvent event) {
        Utils.debug("world '%s' unloaded", event.getWorld().getName());
        Gates.removeGatesForWorld(event.getWorld());
        for (Server server : Servers.getAll())
            server.sendWorldUnload(event.getWorld());
    }

}
