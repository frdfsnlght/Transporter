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
package com.frdfsnlght.transporter;

import com.frdfsnlght.inquisitor.Inquisitor;
import com.frdfsnlght.inquisitor.api.API;
import com.frdfsnlght.transporter.api.ReservationException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class Realm {

    private static final Set<String> OPTIONS = new HashSet<String>();

    private static final Options options;

    static {
        OPTIONS.add("name");
        OPTIONS.add("defaultServer");
        OPTIONS.add("defaultWorld");
        OPTIONS.add("defaultGate");
        OPTIONS.add("respawn");
        OPTIONS.add("respawnGate");
        OPTIONS.add("serverOfflineFormat");
        OPTIONS.add("restoreWhenServerOffline");
        OPTIONS.add("restoreWhenServerOfflineFormat");
        OPTIONS.add("kickWhenServerOffline");
        OPTIONS.add("kickWhenServerOfflineFormat");

        options = new Options(Realm.class, OPTIONS, "trp.realm", new OptionsListener() {
            @Override
            public void onOptionSet(Context ctx, String name, String value) {
                ctx.send("realm option '%s' set to '%s'", name, value);
            }
            @Override
            public String getOptionPermission(Context ctx, String name) {
                return name;
            }
        });
    }

    private static boolean started = false;
    private static Set<String> redirectedPlayers = new HashSet<String>();
    private static Set<String> respawningPlayers = new HashSet<String>();
    private static API inquisitor = null;

    public static boolean isStarted() {
        return started;
    }

    // called from main thread
    public static void start(Context ctx) {
        if (! getEnabled()) return;
        try {
            if (getName() == null)
                throw new RealmException("name is not set");

            redirectedPlayers.clear();

            started = true;
            ctx.send("realm support started");

            for (Server server : Servers.getAll())
                if (server.isConnected()) server.sendRefreshData();

        } catch (RealmException e) {
            ctx.warn("realm support cannot be started: %s", e.getMessage());
        }
    }

    // called from main thread
    public static void stop(Context ctx) {
        if (! started) return;
        inquisitor = null;
        started = false;
        respawningPlayers.clear();
        ctx.send("realm support stopped");
    }

    public static void onConfigLoad(Context ctx) {}

    public static void onConfigSave() {}

    // Player events

    public static void onTeleport(Player player, Location toLocation) {
        if (! started) return;
        if (redirectedPlayers.contains(player.getName())) return;
        if (! respawningPlayers.remove(player.getName())) return;
        Utils.debug("realm respawn '%s'", player.getName());
        if (! getRespawn()) return;
        GateImpl respawnGate = getRespawnGateImpl();
        if (respawnGate != null)
            sendPlayerToGate(player, respawnGate);
        else {
            if (! inquisitorAvailable()) return;
            com.frdfsnlght.inquisitor.api.Location bedLocation = inquisitor.getPlayerBedLocation(player.getName());
            if (bedLocation == null) return;
            sendPlayerToBed(player, bedLocation.getServer(), bedLocation.getWorld(), bedLocation.getCoords());
        }
    }

    public static boolean onJoin(Player player) {
        if (! started) return false;
        Utils.debug("realm join '%s'", player.getName());
        redirectedPlayers.remove(player.getName());
        if (! inquisitorAvailable()) return false;
        com.frdfsnlght.inquisitor.api.Location lastLocation = inquisitor.getPlayerLastLocation(player.getName());
        if (lastLocation != null) {
            if (! lastLocation.getServer().equals(Global.plugin.getServer().getServerName())) {
                if (sendPlayerToServer(player, lastLocation.getServer()))
                    return true;
            }
        } else {
            GateImpl defaultGate = getDefaultGateImpl();
            if (defaultGate != null) {
                if (sendPlayerToGate(player, defaultGate))
                    return true;
            }
            String toServer = getDefaultServer();
            if (toServer != null) {
                if (! toServer.equals(Global.plugin.getServer().getServerName())) {
                    if (sendPlayerToServer(player, toServer))
                        return true;
                }
            }
            String toWorld = getDefaultWorld();
            if (toWorld != null) {
                if (! toWorld.equals(player.getWorld().getName())) {
                    if (sendPlayerToWorld(player, toWorld))
                        return true;
                }
            }
        }
        return false;
    }

    public static void onRespawn(Player player) {
        if (! started) return;
        respawningPlayers.add(player.getName());
    }


    // End Player events

    private static boolean inquisitorAvailable() {
        if (inquisitor != null) return true;
        Plugin p = Global.plugin.getServer().getPluginManager().getPlugin("Inquisitor");
        if (p == null) {
            Utils.warning("Inquisitor plugin is not installed!");
            return false;
        }
        Utils.info("Inquisitor plugin found");
        if (! p.isEnabled()) {
            Utils.warning("Inquisitor plugin is not enabled!");
            return false;
        }
        inquisitor = ((Inquisitor)p).getAPI();
        if (! inquisitor.isPlayerStatsStarted()) {
            inquisitor = null;
            Utils.warning("Inquisitor player stats is not started!");
            return false;
        }
        Utils.info("Inquisitor plugin is ready to go");
        return true;
    }

    // toServer is either the name of a configured server, or the name of a connected remote server
    private static boolean sendPlayerToServer(Player player, String toServer) {
        Server server = Servers.findByRemoteName(toServer);
        if (server == null) {
            server = Servers.find(toServer);
            /*
            if (server == null) {
                Utils.warning("Unknown realm server '%s' for player '%s'", toServer, player.getName());
                return false;
            }
            */
        }
        if ((server == null) || (! server.isConnected())) {
            Utils.warning("Unknown or offline realm server '%s' for player '%s'", toServer, player.getName());

            if (getRestoreWhenServerOffline()) {
                Map<String,Object> playerData = inquisitor.getPlayerStats(player.getName());
                if (playerData == null)
                    Utils.warning("Realm player stats for '%s' not found", player.getName());
                else {
                    TypeMap data = new TypeMap(playerData);
                    Players.restore(player, data);
                    Utils.debug("restored '%s' from realm data", player.getName());
                    String msg = getRestoreWhenServerOfflineFormat();
                    if (msg != null)
                        msg = msg.replace("%server%", toServer);
                     Chat.colorize(msg);
                     if ((msg != null) && (! msg.isEmpty()))
                        player.sendMessage(msg);
                    return false;
                }
            }
            if (getKickWhenServerOffline()) {
                String msg = getKickWhenServerOfflineFormat();
                msg = msg.replace("%server%", toServer);
                inquisitor.ignorePlayerJoin(player.getName());
                Utils.schedulePlayerKick(player, msg);
                return true;
            }

            String msg = getServerOfflineFormat();
            if (msg != null)
                 msg = msg.replace("%server%", toServer);
            Chat.colorize(msg);
            if ((msg != null) && (! msg.isEmpty()))
                player.sendMessage(msg);

            return false;
        }
        switch (server.getTransferMethod()) {
            case ClientKick:
                String kickMessage = server.getKickMessage(player.getAddress());
                if (kickMessage == null) return false;
                Utils.schedulePlayerKick(player, kickMessage);
                break;
            case Bungee:
                Utils.sendPlayerToBungeeServer(player, server.getRemoteBungeeServer());
                break;
        }
        redirectedPlayers.add(player.getName());
        if (inquisitorAvailable())
            inquisitor.ignorePlayerJoin(player.getName());
        return true;
    }

    private static boolean sendPlayerToGate(Player player, GateImpl gate) {
        try {
            ReservationImpl res = new ReservationImpl(player, gate);
            res.depart();
            return true;
        } catch (ReservationException re) {
            Utils.warning("Reservation exception while sending player '%s' to gate '%s': %s", player.getName(), gate.getLocalName(), re.getMessage());
            return false;
        }
    }

    private static void sendPlayerToBed(Player player, String serverName, String worldName, double[] coords) {
        if (serverName.equals(Global.plugin.getServer().getServerName())) return;
        Utils.debug("sending realm player '%s' to respawn at %s/%s/%s", player.getName(), serverName, worldName, Arrays.toString(coords));
        Server server = Servers.findByRemoteName(serverName);
        /*
        if (server == null) {
            Utils.warning("Unknown realm home server '%s' for player '%s'", serverName, player.getName());
            return;
        }
        */
        if (! server.isConnected()) {
            Utils.warning("Unknown or offline realm home server '%s' for player '%s'", serverName, player.getName());

            if (getKickWhenServerOffline()) {
                String msg = getKickWhenServerOfflineFormat();
                msg = msg.replace("%server%", serverName);
                player.kickPlayer(msg);
                return;
            }

            String msg = getServerOfflineFormat();
            if (msg != null)
                msg = msg.replace("%server%", serverName);
            Chat.colorize(msg);
            if ((msg != null) && (! msg.isEmpty()))
                player.sendMessage(msg);
            return;
        }
        try {
            ReservationImpl res = new ReservationImpl(player, server, worldName, coords[0], coords[1], coords[2]);
            res.depart();
        } catch (ReservationException re) {
            Utils.warning("Reservation exception while sending player '%s' to respawn at %s/%s/%s: %s", player.getName(), serverName, worldName, Arrays.toString(coords), re.getMessage());
        }
    }

    private static boolean sendPlayerToWorld(Player player, String toWorld) {
        World world = Global.plugin.getServer().getWorld(toWorld);
        if (world == null) {
            Utils.warning("Unknown realm world '%s' for player '%s'", toWorld, player.getName());
            return false;
        }
        Location toLocation = world.getSpawnLocation();
        Utils.debug("teleporting player '%s' to %s", player.getName(), toLocation);
        return player.teleport(toLocation);
    }

    public static boolean getEnabled() {
        return Config.getBooleanDirect("realm.enabled", false);
    }

    public static void setEnabled(Context ctx, boolean b) {
        Config.setPropertyDirect("realm.enabled", b);
        stop(ctx);
        if (b) start(ctx);
    }

    /* Begin options */

    public static String getName() {
        return Config.getStringDirect("realm.name", null);
    }

    public static void setName(String s) {
        if ((s != null) && (s.equals("-") || s.equals("*"))) s = null;
        Config.setPropertyDirect("realm.name", s);
    }

    public static String getDefaultServer() {
        return Config.getStringDirect("realm.defaultServer", null);
    }

    public static void setDefaultServer(String s) {
        if ((s != null) && (s.equals("-") || s.equals("*"))) s = null;
        if (s != null) {
            Server server = Servers.find(s);
            if (server == null)
                throw new IllegalArgumentException("unknown server");
            s = server.getName();
        }
        Config.setPropertyDirect("realm.defaultServer", s);
    }

    public static String getDefaultWorld() {
        return Config.getStringDirect("realm.defaultWorld", null);
    }

    public static void setDefaultWorld(String s) {
        if ((s != null) && (s.equals("-") || s.equals("*"))) s = null;
        if (s != null) {
            LocalWorldImpl world = Worlds.get(s);
            if (world == null)
                throw new IllegalArgumentException("unknown world");
            s = world.getName();
        }
        Config.setPropertyDirect("realm.defaultWorld", s);
    }

    public static String getDefaultGate() {
        return Config.getStringDirect("realm.defaultGate", null);
    }

    public static void setDefaultGate(String s) {
        if ((s != null) && (s.equals("-") || s.equals("*"))) s = null;
        if (s != null) {
            GateImpl gate = Gates.find(s);
            if (gate == null)
                throw new IllegalArgumentException("unknown or offline gate");
            s = gate.getLocalName();
        }
        Config.setPropertyDirect("realm.defaultGate", s);
    }

    public static boolean getRespawn() {
        return Config.getBooleanDirect("realm.respawn", true);
    }

    public static void setRespawn(boolean b) {
        Config.setPropertyDirect("realm.respawn", b);
    }

    public static String getRespawnGate() {
        return Config.getStringDirect("realm.respawnGate", null);
    }

    public static void setRespawnGate(String s) {
        if ((s != null) && (s.equals("-") || s.equals("*"))) s = null;
        if (s != null) {
            GateImpl gate = Gates.find(s);
            if (gate == null)
                throw new IllegalArgumentException("unknown or offline gate");
            s = gate.getLocalName();
        }
        Config.setPropertyDirect("realm.respawnGate", s);
    }

    public static String getServerOfflineFormat() {
        return Config.getStringDirect("realm.serverOfflineFormat", "You're not where you belong because server '%server%' is offline.");
    }

    public static void setServerOfflineFormat(String s) {
        if (s != null) {
            if (s.equals("-")) s = "";
            else if (s.equals("*")) s = null;
        }
        Config.setPropertyDirect("realm.serverOfflineFormat", s);
    }

    public static boolean getRestoreWhenServerOffline() {
        return Config.getBooleanDirect("realm.restoreWhenServerOffline", true);
    }

    public static void setRestoreWhenServerOffline(boolean b) {
        Config.setPropertyDirect("realm.restoreWhenServerOffline", b);
    }

    public static String getRestoreWhenServerOfflineFormat() {
        return Config.getStringDirect("realm.restoreWhenServerOfflineFormat", "You're not where you belong because server '%server%' is offline.");
    }

    public static void setRestoreWhenServerOfflineFormat(String s) {
        if (s != null) {
            if (s.equals("-")) s = "";
            else if (s.equals("*")) s = null;
        }
        Config.setPropertyDirect("realm.restoreWhenServerOfflineFormat", s);
    }

    public static boolean getKickWhenServerOffline() {
        return Config.getBooleanDirect("realm.kickWhenServerOffline", true);
    }

    public static void setKickWhenServerOffline(boolean b) {
        Config.setPropertyDirect("realm.kickWhenServerOffline", b);
    }

    public static String getKickWhenServerOfflineFormat() {
        return Config.getStringDirect("realm.kickWhenServerOfflineFormat", "Server '%server%' is offline.");
    }

    public static void setKickWhenServerOfflineFormat(String s) {
        if (s != null) {
            if (s.equals("*"))
                s = null;
        }
        Config.setPropertyDirect("realm.kickWhenServerOfflineFormat", s);
    }

    public static void getOptions(Context ctx, String name) throws OptionsException, PermissionsException {
        options.getOptions(ctx, name);
    }

    public static String getOption(Context ctx, String name) throws OptionsException, PermissionsException {
        return options.getOption(ctx, name);
    }

    public static void setOption(Context ctx, String name, String value) throws OptionsException, PermissionsException {
        options.setOption(ctx, name, value);
    }

    /* End options */

    private static GateImpl getDefaultGateImpl() {
        String gName = getDefaultGate();
        if (gName == null) return null;
        GateImpl gate = Gates.get(gName);
        return gate;
    }

    private static GateImpl getRespawnGateImpl() {
        String gName = getRespawnGate();
        if (gName == null) return null;
        GateImpl gate = Gates.get(gName);
        return gate;
    }

}
