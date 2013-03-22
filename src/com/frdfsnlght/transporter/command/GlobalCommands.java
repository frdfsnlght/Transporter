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
package com.frdfsnlght.transporter.command;

import com.frdfsnlght.transporter.Chat;
import com.frdfsnlght.transporter.Config;
import com.frdfsnlght.transporter.Context;
import com.frdfsnlght.transporter.GateImpl;
import com.frdfsnlght.transporter.Gates;
import com.frdfsnlght.transporter.Global;
import com.frdfsnlght.transporter.Permissions;
import com.frdfsnlght.transporter.Players;
import com.frdfsnlght.transporter.RemotePlayerImpl;
import com.frdfsnlght.transporter.ReservationImpl;
import com.frdfsnlght.transporter.Server;
import com.frdfsnlght.transporter.api.ReservationException;
import com.frdfsnlght.transporter.api.TransporterException;
import com.frdfsnlght.transporter.api.event.LocalPlayerPMEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class GlobalCommands extends TrpCommandProcessor {

    @Override
    public boolean matches(Context ctx, Command cmd, List<String> args) {
        return super.matches(ctx, cmd, args) && (
               ("list".startsWith(args.get(0).toLowerCase())) ||
               ("get".startsWith(args.get(0).toLowerCase())) ||
               ("set".startsWith(args.get(0).toLowerCase())) ||
               ("go".startsWith(args.get(0).toLowerCase())) ||
               ("send".startsWith(args.get(0).toLowerCase())) ||
               ("pm".startsWith(args.get(0).toLowerCase()))
            );

    }

    @Override
    public List<String> getUsage(Context ctx) {
        List<String> cmds = new ArrayList<String>();
        cmds.add(getPrefix(ctx) + "list");
        cmds.add(getPrefix(ctx) + "get <option>|*");
        cmds.add(getPrefix(ctx) + "set <option> <value>");
        if (ctx.isPlayer())
            cmds.add(getPrefix(ctx) + "go [<gate>]");
        cmds.add(getPrefix(ctx) + "send <player> [<gate>]");
        cmds.add(getPrefix(ctx) + "pm <player> <message>");
        return cmds;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void process(Context ctx, Command cmd, List<String> args) throws TransporterException {
        if (args.isEmpty())
            throw new CommandException("do what?");
        String subCmd = args.remove(0).toLowerCase();

        if ("list".startsWith(subCmd)) {
            Permissions.require(ctx.getPlayer(), "trp.list");
            List<Player> localPlayers = new ArrayList<Player>();
            Collections.addAll(localPlayers, Global.plugin.getServer().getOnlinePlayers());
            List<RemotePlayerImpl> remotePlayers = new ArrayList(Global.plugin.getAPI().getRemotePlayers());

            if (localPlayers.isEmpty() && remotePlayers.isEmpty())
                ctx.send("there are no players");
            else {
                Collections.sort(localPlayers, new Comparator<Player>() {
                    @Override
                    public int compare(Player a, Player b) {
                        return a.getName().compareToIgnoreCase(b.getName());
                    }
                });
                Collections.sort(remotePlayers, new Comparator<RemotePlayerImpl>() {
                    @Override
                    public int compare(RemotePlayerImpl a, RemotePlayerImpl b) {
                        int res = a.getRemoteServer().getName().compareToIgnoreCase(b.getRemoteServer().getName());
                        if (res == 0)
                            res = a.getName().compareToIgnoreCase(b.getName());
                        return res;
                    }
                });

                ctx.send("%d local players:", localPlayers.size());
                for (Player p : localPlayers)
                    ctx.send("  %s (%s)", p.getDisplayName(), p.getWorld().getName());

                ctx.send("%d remote players:", remotePlayers.size());
                String lastServer = "*";
                for (RemotePlayerImpl p : remotePlayers) {
                    if (! lastServer.equals(p.getRemoteServer().getName())) {
                        ctx.send("  server: %s", p.getRemoteServer().getName());
                        lastServer = p.getRemoteServer().getName();
                    }
                    ctx.send("    %s (%s)", p.getDisplayName(), p.getRemoteWorld().getName());
                }
            }
            return;
        }

        if ("go".startsWith(subCmd)) {
            if (! ctx.isPlayer())
                throw new CommandException("this command can only be used by a player");
            GateImpl gate;
            if (! args.isEmpty()) {
                String name = args.remove(0);
                gate = Gates.find(ctx, name);
                if (gate == null)
                    throw new CommandException("unknown gate '%s'", name);
            } else
                gate = Gates.getSelectedGate(ctx.getPlayer());
            if (gate == null)
                throw new CommandException("gate name required");

            Permissions.require(ctx.getPlayer(), "trp.go." + gate.getFullName());
            try {
                ReservationImpl r = new ReservationImpl(ctx.getPlayer(), gate);
                r.depart();
            } catch (ReservationException e) {
                ctx.warnLog(e.getMessage());
            }
            return;
        }

        if ("send".startsWith(subCmd)) {
            if (args.isEmpty())
                throw new CommandException("player name required");
            Player player = Global.plugin.getServer().getPlayer(args.remove(0));
            if (player == null)
                throw new CommandException("unknown player");
            GateImpl gate;
            if (! args.isEmpty()) {
                String name = args.remove(0);
                gate = Gates.find(ctx, name);
                if (gate == null)
                    throw new CommandException("unknown gate '%s'", name);
            } else
                gate = Gates.getSelectedGate(ctx.getPlayer());
            if (gate == null)
                throw new CommandException("gate name required");

            Permissions.require(ctx.getPlayer(), "trp.send." + gate.getFullName());
            try {
                ReservationImpl res = new ReservationImpl(player, gate);
                ctx.send("sending player '%s' to '%s'", player.getName(), gate.getLocalName());
                res.depart();
            } catch (ReservationException re) {
                throw new CommandException(re.getMessage());
            }
            return;
        }

        if ("pm".startsWith(subCmd)) {
            if (args.isEmpty())
                throw new CommandException("player name required");
            String playerName = args.remove(0);
            if (args.isEmpty())
                throw new CommandException("message required");
            String message = "";
            for (String arg : args) {
                if (message.length() > 0) message += " ";
                message += arg;
            }
            Permissions.require(ctx.getPlayer(), "trp.pm");
            Player localPlayer = Players.findLocal(playerName);
            if (localPlayer != null) {
                LocalPlayerPMEvent event = new LocalPlayerPMEvent(ctx.getPlayer(), localPlayer, message);
                Global.plugin.getServer().getPluginManager().callEvent(event);
                if (event.isCancelled()) return;
                String format;
                if (ctx.isConsole()) {
                    format = Config.getConsolePMFormat();
                    if (format == null) return;
                } else {
                    if (ctx.getPlayer().getWorld() == localPlayer.getWorld())
                        format = Config.getLocalPMFormat();
                    else
                        format = Config.getWorldPMFormat();
                    if (format == null) return;
                    format = format.replaceAll("%fromPlayer%", ctx.getPlayer().getDisplayName());
                    format = format.replaceAll("%fromWorld%", ctx.getPlayer().getWorld().getName());
                    format = format.replaceAll("%toPlayer%", localPlayer.getDisplayName());
                    format = format.replaceAll("%toWorld%", localPlayer.getWorld().getName());
                }
                format = format.replaceAll("%message%", message);
                format = Chat.colorize(format);
                if (! format.isEmpty()) {
                    localPlayer.sendMessage(format);
                    ctx.send(ctx.getSender().getName() + " -> " + localPlayer.getName() + ": " + message);
                }
                return;
            }
            RemotePlayerImpl remotePlayer = Players.findRemote(playerName);
            if (remotePlayer != null) {
                ((Server)remotePlayer.getRemoteServer()).sendPrivateMessage(ctx.getPlayer(), remotePlayer, message);
                ctx.send(ctx.getSender().getName() + " -> " + remotePlayer.getRemoteServer().getName() + "@" + remotePlayer.getName() + ": " + message);
                return;
            }
            throw new CommandException("unknown or ambiguous player name");
        }

        if ("set".startsWith(subCmd)) {
            if (args.isEmpty())
                throw new CommandException("option name required");
            String option = args.remove(0);
            if (args.isEmpty())
                throw new CommandException("option value required");
            String value = args.remove(0);
            Config.setOption(ctx, option, value);
            return;
        }

        if ("get".startsWith(subCmd)) {
            if (args.isEmpty())
                throw new CommandException("option name required");
            String option = args.remove(0);
            Config.getOptions(ctx, option);
//            return;
        }

    }

}
