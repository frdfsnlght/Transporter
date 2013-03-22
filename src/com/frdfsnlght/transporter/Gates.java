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

import com.frdfsnlght.transporter.GateMap.Volume;
import com.frdfsnlght.transporter.api.GateException;
import com.frdfsnlght.transporter.api.TransporterException;
import com.frdfsnlght.transporter.api.event.LocalGateCreateEvent;
import com.frdfsnlght.transporter.api.event.LocalGateDestroyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Manages a collection of both local and remote gates.
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class Gates {

    // Gate build blocks that are protected
    private static final GateMap protectionMap = new GateMap();

    // Portal blocks for open, local gates
    private static final GateMap portalMap = new GateMap();

    // Gate screens for local gates
    private static final GateMap screenMap = new GateMap();

    // Gate switches for local gates
    public static final GateMap switchMap = new GateMap();

    // Gate triggers for local gates
    public static final GateMap triggerMap = new GateMap();

    // Indexed by full name
    private static final Map<String,GateImpl> gates = new HashMap<String,GateImpl>();

    private static Map<Integer,LocalGateImpl> selectedGates = new HashMap<Integer,LocalGateImpl>();

    public static void load(Context ctx) {
        clearLocalGates();
        for (World world : Global.plugin.getServer().getWorlds())
            loadGatesForWorld(ctx, world);
    }

    public static int loadGatesForWorld(Context ctx, World world) {
        File worldFolder = Worlds.worldPluginFolder(world);
        File gatesFolder = new File(worldFolder, "gates");
        if (! gatesFolder.exists()) {
            Utils.info("no gates found for world '%s'", world.getName());
            return 0;
        }
        int loadedCount = 0;
        for (File gateFile : Utils.listYAMLFiles(gatesFolder)) {
            try {
                LocalGateImpl gate = LocalGateImpl.load(world, gateFile);
                if (gates.containsKey(gate.getFullName())) continue;
                try {
                    add(gate, false);
                    ctx.sendLog("loaded gate '%s' for world '%s'", gate.getName(), world.getName());
                    loadedCount++;
                } catch (GateException ge) {
                    ctx.warnLog("unable to load gate '%s' for world '%s': %s", gate.getName(), world.getName(), ge.getMessage());
                }
            } catch (TransporterException te) {
                ctx.warnLog("'%s' contains an invalid gate file for world '%s': %s", gateFile.getPath(), world.getName(), te.getMessage());
            } catch (Throwable t) {
                Utils.severe(t, "there was a problem loading the gate file '%s' for world '%s':", gateFile.getPath(), world.getName());
            }
        }
        return loadedCount;
    }

    public static void save(Context ctx) {
        Markers.update();
        if (gates.isEmpty()) return;
        Set<LocalGateImpl> lgates = getLocalGates();
        for (LocalGateImpl gate : lgates) {
            gate.save(true);
            if ((ctx != null) && Config.getShowGatesSavedMessage())
                ctx.sendLog("saved '%s'", gate.getLocalName());
        }
        if ((ctx != null) && (! Config.getShowGatesSavedMessage()))
            ctx.sendLog("saved %s gates", lgates.size());
    }

    public static GateImpl find(Context ctx, String name) {
        int pos = name.indexOf('.');
        if (pos == -1) {
            // asking for a local gate in the player's current world
            if (! ctx.isPlayer()) return null;
            name = ctx.getPlayer().getWorld().getName() + "." + name;
        }
        return find(name);
    }

    public static GateImpl find(String name) {
        if (gates.containsKey(name)) return gates.get(name);
        String lname = name.toLowerCase();
        GateImpl gate = null;
        for (String key : gates.keySet()) {
            if (key.toLowerCase().startsWith(lname)) {
                if (gate == null) gate = gates.get(key);
                else return null;
            }
        }
        return gate;
    }

    public static GateImpl get(String name) {
        return gates.get(name);
    }

    public static void add(GateImpl gate, boolean created) throws GateException {
        if (gates.containsKey(gate.getFullName()))
            throw new GateException("a gate with the same name already exists here");
        gates.put(gate.getFullName(), gate);
        for (LocalGateImpl lg : getLocalGates())
            lg.onGateAdded(gate);
        if (gate instanceof LocalGateImpl) {
            LocalGateImpl lg = (LocalGateImpl)gate;
            LocalGateCreateEvent event = new LocalGateCreateEvent(lg);
            Global.plugin.getServer().getPluginManager().callEvent(event);
            for (Server server : Servers.getAll())
                server.sendGateAdded(lg);
            Markers.update();
            World world = lg.getWorld();
            if (Config.getAutoAddWorlds())
                try {
                    LocalWorldImpl wp = Worlds.add(world);
                    if (wp != null)
                        Utils.info("automatically added world '%s' for new gate '%s'", wp.getName(), gate.getName());
                } catch (WorldException we) {}
            else if (Worlds.get(world.getName()) == null)
                Utils.warning("Gate '%s' has been added to world '%s' but the world has not been added to the plugin's list of worlds!", gate.getName(), world.getName());
        }
    }

    public static void remove(GateImpl gate) throws GateException {
        if (! gates.containsKey(gate.getFullName()))
            throw new GateException("gate not found");
        for (LocalGateImpl lg : getLocalGates())
            lg.onGateRemoved(gate);
        gates.remove(gate.getFullName());
        if (gate instanceof LocalGateImpl) {
            LocalGateImpl lg = (LocalGateImpl)gate;
            deselectGate(lg);
            lg.save(false);
            for (Server server : Servers.getAll())
                server.sendGateRemoved(lg);
            Markers.update();
        }
    }

    public static void destroy(GateImpl gate, boolean unbuild) {
        gates.remove(gate.getFullName());
        for (LocalGateImpl lg : getLocalGates())
            lg.onGateDestroyed(gate);
        if (gate instanceof LocalGateImpl) {
            LocalGateImpl lg = (LocalGateImpl)gate;
            deselectGate(lg);
            LocalGateDestroyEvent event = new LocalGateDestroyEvent(lg);
            Global.plugin.getServer().getPluginManager().callEvent(event);
            lg.destroy(unbuild);
            for (Server server : Servers.getAll())
                server.sendGateDestroyed(lg);
            Markers.update();
        }
    }

    public static void rename(LocalGateImpl lg, String newName) throws GateException {
        String oldName = lg.getName();
        String oldFullName = lg.getFullName();
        lg.setName(newName);
        String newFullName = lg.getFullName();
        if (gates.containsKey(newFullName)) {
            lg.setName(oldName);
            throw new GateException("gate name already exists");
        }
        rename((GateImpl)lg, oldFullName);
    }

    public static void rename(GateImpl gate, String oldFullName) {
        gates.remove(oldFullName);
        gates.put(gate.getFullName(), gate);
        for (LocalGateImpl lg : getLocalGates())
            lg.onGateRenamed(gate, oldFullName);
        if (gate instanceof LocalGateImpl) {
            LocalGateImpl lg = (LocalGateImpl)gate;
            lg.onRenameComplete();
            for (Server server : Servers.getAll())
                server.sendGateRenamed(oldFullName, gate.getName());
            Markers.update();
        }
    }

    public static void removeGatesForWorld(World world) {
        for (LocalGateImpl lg : getLocalGates()) {
            if (lg.getWorld() == world)
                try {
                    remove(lg);
                } catch (GateException ee) {}
        }
    }

    public static void removeGatesForServer(Server server) {
        for (RemoteGateImpl rg : getRemoteGates())
            if (rg.getRemoteServer() == server)
                try {
                    remove(rg);
                } catch (GateException ee) {}
    }

    public static LocalGateImpl getLocalGate(String name) {
        GateImpl gate = gates.get(name);
        if ((gate == null) || (! (gate instanceof LocalGateImpl))) return null;
        return (LocalGateImpl)gate;
    }

    public static Set<LocalGateImpl> getLocalGates() {
        Set<LocalGateImpl> gs = new HashSet<LocalGateImpl>();
        for (GateImpl gate : gates.values())
            if (gate instanceof LocalGateImpl) gs.add((LocalGateImpl)gate);
        return gs;
    }

    public static Set<RemoteGateImpl> getRemoteGates() {
        Set<RemoteGateImpl> gs = new HashSet<RemoteGateImpl>();
        for (GateImpl gate : gates.values())
            if (gate instanceof RemoteGateImpl) gs.add((RemoteGateImpl)gate);
        return gs;
    }



    public static LocalGateImpl findGateForPortal(Location loc) {
        return portalMap.getGate(loc);
    }

    public static void addPortalVolume(Volume vol) {
        portalMap.put(vol);
    }

    public static void removePortalVolume(LocalGateImpl gate) {
        portalMap.removeGate(gate);
    }

    public static LocalGateImpl findGateForProtection(Location loc) {
        return protectionMap.getGate(loc);
    }

    public static void addProtectionVolume(Volume vol) {
        protectionMap.put(vol);
    }

    public static void removeProtectionVolume(LocalGateImpl gate) {
        protectionMap.removeGate(gate);
    }

    public static LocalGateImpl findGateForScreen(Location loc) {
        return screenMap.getGate(loc);
    }

    public static void addScreenVolume(Volume vol) {
        screenMap.put(vol);
    }

    public static void removeScreenVolume(LocalGateImpl gate) {
        screenMap.removeGate(gate);
    }

    public static LocalGateImpl findGateForSwitch(Location loc) {
        return switchMap.getGate(loc);
    }

    public static void addSwitchVolume(Volume vol) {
        switchMap.put(vol);
    }

    public static void removeSwitchVolume(LocalGateImpl gate) {
        switchMap.removeGate(gate);
    }

    public static LocalGateImpl findGateForTrigger(Location loc) {
        return triggerMap.getGate(loc);
    }

    public static void addTriggerVolume(Volume vol) {
        triggerMap.put(vol);
    }

    public static void removeTriggerVolume(LocalGateImpl gate) {
        triggerMap.removeGate(gate);
    }

    public static void dumpMaps() {
        Utils.debug("portalMap=%s", portalMap);
        Utils.debug("protectionMap=%s", protectionMap);
        Utils.debug("screenMap=%s", screenMap);
        Utils.debug("switchMap=%s", switchMap);
        Utils.debug("triggerMap=%s", triggerMap);
    }


    public static void setSelectedGate(Player player, LocalGateImpl gate) {
        selectedGates.put((player == null) ? Integer.MAX_VALUE : player.getEntityId(), gate);
    }

    public static LocalGateImpl getSelectedGate(Player player) {
        return selectedGates.get((player == null) ? Integer.MAX_VALUE : player.getEntityId());
    }

    public static void deselectGate(LocalGateImpl gate) {
        for (Integer playerId : new ArrayList<Integer>(selectedGates.keySet()))
            if (selectedGates.get(playerId) == gate)
                selectedGates.remove(playerId);
    }

    private static void clearLocalGates() {
        for (GateImpl gate : new HashSet<GateImpl>(gates.values()))
            if (gate instanceof GateImpl)
                gates.remove(gate.getFullName());
    }

}
