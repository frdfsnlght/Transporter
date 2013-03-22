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

import com.frdfsnlght.transporter.api.GateException;
import com.frdfsnlght.transporter.api.event.RemotePlayerChatEvent;
import com.frdfsnlght.transporter.api.event.RemotePlayerPMEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class Chat {

    private static net.milkbowl.vault.chat.Chat vaultPlugin = null;

    private static Pattern colorPattern = Pattern.compile("%(\\w+)%");

    public static boolean vaultAvailable() {
        if (! Config.getUseVaultChat()) return false;
        Plugin p = Global.plugin.getServer().getPluginManager().getPlugin("Vault");
        if (p == null) {
            Utils.warning("Vault is not installed!");
            return false;
        }
        if (! p.isEnabled()) {
            Utils.warning("Vault is not enabled!");
            return false;
        }
        if (vaultPlugin != null) return true;
        RegisteredServiceProvider<net.milkbowl.vault.chat.Chat> rsp =
                Global.plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
        if (rsp == null) {
            Utils.warning("Vault didn't return a service provider!");
            return false;
        }
        vaultPlugin = rsp.getProvider();
        if (vaultPlugin == null) {
            Utils.warning("Vault didn't return a chat provider!");
            return false;
        }
        Utils.info("Initialized Vault for Chat");
        return true;
    }

    public static String colorize(String msg) {
        if (msg == null) return null;
        Matcher matcher = colorPattern.matcher(msg);
        StringBuffer b = new StringBuffer();
        while (matcher.find()) {
            String name = matcher.group(1);
            try {
                ChatColor color = Utils.valueOf(ChatColor.class, name);
                matcher.appendReplacement(b, color.toString());
            } catch (IllegalArgumentException iae) {
                matcher.appendReplacement(b, matcher.group());
            }
        }
        matcher.appendTail(b);
        return b.toString();
    }

    public static void send(Player player, String message, String format) {
        Utils.debug("player '%s' sent message '%s' with format '%s'", player.getName(), message, format);

        Map<Server,Set<RemoteGateImpl>> servers = new HashMap<Server,Set<RemoteGateImpl>>();

        // add all servers that relay all chat
        for (Server server : Servers.getAll())
            if (server.canSendChat(message, format)) {
                servers.put(server, null);
                Utils.debug("can send chat message to server %s", server.getName());
            }

        Location loc = player.getLocation();
        RemoteGateImpl destGate;
        Server destServer;
        for (LocalGateImpl gate : Gates.getLocalGates()) {
            if (gate.isOpen() && gate.canSendChat(message, format) && gate.isInChatSendProximity(loc)) {
                try {
                    GateImpl dg = gate.getDestinationGate();
                    if (! (dg instanceof RemoteGateImpl)) continue;
                    destGate = (RemoteGateImpl)dg;
                    destServer = (Server)destGate.getRemoteServer();
                    if (servers.containsKey(destServer)) {
                        if (servers.get(destServer) == null) continue;
                    } else
                        servers.put(destServer, new HashSet<RemoteGateImpl>());
                    servers.get(destServer).add(destGate);
                    Utils.debug("can send chat message to server %s through gate %s", destServer.getName(), destGate.getFullName());
                } catch (GateException e) {}
            }
        }

        if (servers.isEmpty()) {
            Utils.debug("no servers to send chat message to");
            return;
        }
        for (Server server : servers.keySet()) {
            server.sendChat(player, message, servers.get(server));
        }
    }

    public static void receive(Server fromServer, RemotePlayerImpl player, String message, List<String> toGates) {
        Player[] players = Global.plugin.getServer().getOnlinePlayers();

        Set<Player> playersToReceive = new HashSet<Player>();
        if ((toGates == null) && fromServer.canReceiveChat(message))
            Collections.addAll(playersToReceive, players);
        else if ((toGates != null) && (! toGates.isEmpty())) {
            for (String gateName : toGates) {
                GateImpl g = Gates.get(gateName);
                if ((g == null) || (! (g instanceof LocalGateImpl))) continue;
                LocalGateImpl gate = (LocalGateImpl)g;
                if (! gate.canReceiveChat(message)) continue;
                for (Player p : players) {
                    if (gate.isInChatReceiveProximity(p.getLocation()))
                        playersToReceive.add(p);
                }
            }
        } else {
            Utils.debug("chat message ignored");
            return;
        }

        if (playersToReceive.isEmpty()) {
            Utils.debug("no players to send chat message to");
            return;
        }

        String format = fromServer.getChatFormat();
        if (format == null)
            format = Config.getServerChatFormat();

        RemotePlayerChatEvent event = new RemotePlayerChatEvent(player, message, format);
        Global.plugin.getServer().getPluginManager().callEvent(event);

        format = player.format(format);
        format = format.replace("%message%", message);
        format = colorize(format);
        for (Player p : playersToReceive)
            p.sendMessage(format);
    }

    public static void receivePrivateMessage(Server fromServer, RemotePlayerImpl remotePlayer, String localPlayerName, String message) {
        Player localPlayer = Global.plugin.getServer().getPlayer(localPlayerName);
        if (localPlayer == null) return;
        RemotePlayerPMEvent event = new RemotePlayerPMEvent(remotePlayer, localPlayer, message);
        Global.plugin.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        String format = fromServer.getPmFormat();
        if (format == null)
            format = Config.getServerPMFormat();
        if (format == null) return;
        format = format.replace("%fromPlayerPrefix%", (remotePlayer.getPrefix() == null) ? "" : remotePlayer.getPrefix());
        format = format.replace("%fromPlayerSuffix", (remotePlayer.getSuffix() == null) ? "" : remotePlayer.getSuffix());
        format = format.replace("%fromPlayer%", remotePlayer.getDisplayName());
        format = format.replace("%fromWorld%", remotePlayer.getRemoteWorld().getName());
        format = format.replace("%fromServer%", fromServer.getName());
        String prefix = getPrefix(localPlayer);
        if (prefix == null) prefix = "";
        String suffix = getSuffix(localPlayer);
        if (suffix == null) suffix = "";
        format = format.replace("%toPlayerPrefix%", prefix);
        format = format.replace("%toPlayerSuffix", suffix);
        format = format.replace("%toPlayer%", localPlayer.getDisplayName());
        format = format.replace("%toWorld%", localPlayer.getWorld().getName());
        format = format.replace("%message%", message);
        format = colorize(format);
        if (! format.isEmpty())
            localPlayer.sendMessage(format);
    }

    public static String getPrefix(Player player) {
        if (vaultAvailable())
            return vaultPlugin.getPlayerPrefix(player);
        return null;
    }

    public static String getSuffix(Player player) {
        if (vaultAvailable())
            return vaultPlugin.getPlayerSuffix(player);
        return null;
    }

}
