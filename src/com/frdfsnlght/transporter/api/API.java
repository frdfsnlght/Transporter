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

import java.util.HashSet;
import java.util.Set;
import com.frdfsnlght.transporter.Config;
import com.frdfsnlght.transporter.GateImpl;
import com.frdfsnlght.transporter.Gates;
import com.frdfsnlght.transporter.LocalGateImpl;
import com.frdfsnlght.transporter.Realm;
import com.frdfsnlght.transporter.ReservationImpl;
import com.frdfsnlght.transporter.Server;
import com.frdfsnlght.transporter.Servers;
import com.frdfsnlght.transporter.Worlds;
import org.bukkit.entity.Player;

/**
 *  This class provides the top level API for the Transporter plugin.
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class API {

    /**
     * Returns a set of all online players from connected remote servers.
     *
     * @return a set of {@link RemotePlayer} objects
     */
    public Set<RemotePlayer> getRemotePlayers() {
        Set<RemotePlayer> players = new HashSet<RemotePlayer>();
        for (Server server : Servers.getAll())
            players.addAll(server.getRemotePlayers());
        return players;
    }

    /**
     * Returns a set of all gates on the local server.
     *
     * @return a set of {@link LocalGate} objects
     */
    public Set<LocalGate> getLocalGates() {
        return new HashSet<LocalGate>(Gates.getLocalGates());
    }

    /**
     * Returns a set of all remote gates from connected remote servers.
     *
     * @return a set of {@link RemoteGate} objects
     */
    public Set<RemoteGate> getRemoteGates() {
        return new HashSet<RemoteGate>(Gates.getRemoteGates());
    }

    /**
     * Returns a set of all configured remote servers.
     *
     * @return a set of {@link RemoteServer} objects
     */
    public Set<RemoteServer> getRemoteServers() {
        return new HashSet<RemoteServer>(Servers.getAll());
    }

    /**
     * Returns a set of all worlds on the local server.
     *
     * @return a set of {@link LocalWorld} objects
     */
    public Set<LocalWorld> getLocalWorlds() {
        return new HashSet<LocalWorld>(Worlds.getAll());
    }

    /**
     * Saves all plugin configurations.
     * <p>
     * This method is the equivalent of calling <code>saveConfig</code>
     * and <code>saveGates</code>.
     */
    public void saveAll() {
        saveConfig();
        saveGates();
    }

    /**
     * Saves the main plugin configuration.
     */
    public void saveConfig() {
        Config.save(null);
    }

    /**
     * Saves all gate configurations.
     */
    public void saveGates() {
        Gates.save(null);
    }

    /**
     * Teleports the specified player as if they stepped into the specified gate.
     * <p>
     * This method will return before the teleportation is complete and may
     * not throw an exception even if the teleportation fails, under some
     * circumstances.
     *
     * @param player    the player to teleport
     * @param fromGate  the gate from which to teleport the player
     * @throws ReservationException if the teleportation cannot be completed
     */
    public void teleportPlayer(Player player, LocalGate fromGate) throws ReservationException {
        ReservationImpl res = new ReservationImpl(player, (LocalGateImpl)fromGate);
        res.depart();
    }

    /**
     * Teleports the specified player to the specified gate.
     * <p>
     * This method will return before the teleportation is complete and may
     * not throw an exception even if the teleportation fails, under some
     * circumstances.
     *
     * @param player    the player to teleport
     * @param toGate    the destination gate
     * @throws ReservationException if the teleportation cannot be completed
     */
    public void teleportPlayer(Player player, Gate toGate) throws ReservationException {
        ReservationImpl res = new ReservationImpl(player, (GateImpl)toGate);
        res.depart();
    }

    /**
     * Teleports the specified player to the spawn location in the default
     * world on the specified server.
     * <p>
     * This method will return before the teleportation is complete and may
     * not throw an exception even if the teleportation fails, under some
     * circumstances.
     *
     * @param player    the player to teleport
     * @param server    the destination server
     * @throws ReservationException if the teleportation cannot be completed
     */
    public void teleportPlayer(Player player, RemoteServer server) throws ReservationException {
        ReservationImpl res = new ReservationImpl(player, (Server)server);
        res.depart();
    }

    /**
     * Teleports the specified player to the spawn location in the specified
     * remote world.
     * <p>
     * This method will return before the teleportation is complete and may
     * not throw an exception even if the teleportation fails, under some
     * circumstances.
     *
     * @param player    the player to teleport
     * @param world     the destination world
     * @throws ReservationException if the teleportation cannot be completed
     */
    public void teleportPlayer(Player player, RemoteWorld world) throws ReservationException {
        ReservationImpl res = new ReservationImpl(player, (Server)world.getRemoteServer(), world.getName());
        res.depart();
    }

    /**
     * Teleports the specified player to the specified remote location.
     * <p>
     * WARNING: No checks will be performed as to whether the player can
     * safely occupy the specified location. It is completely possible to
     * teleport a player into solid rock.
     * <p>
     * This method will return before the teleportation is complete and may
     * not throw an exception even if the teleportation fails, under some
     * circumstances.
     *
     * @param player    the player to teleport
     * @param location  the destination location
     * @throws ReservationException if the teleportation cannot be completed
     */
    public void teleportPlayer(Player player, RemoteLocation location) throws ReservationException {
        ReservationImpl res = new ReservationImpl(player, (Server)location.getRemoteServer(), location.getRemoteWorld().getName(), location.getX(), location.getY(), location.getZ());
        res.depart();
    }

    /**
     * Gets the name of the realm this server belongs to.
     *
     * @return the name of the realm
     */
    public String getRealmName() {
        return Realm.getName();
    }

}
