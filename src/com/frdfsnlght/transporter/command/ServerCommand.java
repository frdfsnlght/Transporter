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

import com.frdfsnlght.transporter.Context;
import com.frdfsnlght.transporter.Permissions;
import com.frdfsnlght.transporter.Realm;
import com.frdfsnlght.transporter.Server;
import com.frdfsnlght.transporter.Servers;
import com.frdfsnlght.transporter.api.TransporterException;
import com.frdfsnlght.transporter.net.Network;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.bukkit.command.Command;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public class ServerCommand extends TrpCommandProcessor {

    private static final String GROUP = "server ";

    @Override
    public boolean matches(Context ctx, Command cmd, List<String> args) {
        return super.matches(ctx, cmd, args) &&
               GROUP.startsWith(args.get(0).toLowerCase());
    }

    @Override
    public List<String> getUsage(Context ctx) {
        List<String> cmds = new ArrayList<String>();
        cmds.add(getPrefix(ctx) + GROUP + "list");
        cmds.add(getPrefix(ctx) + GROUP + "add <name> <plgAddr> <key>");
        cmds.add(getPrefix(ctx) + GROUP + "connect <server>");
        cmds.add(getPrefix(ctx) + GROUP + "disconnect <server>");
        cmds.add(getPrefix(ctx) + GROUP + "enable <server>");
        cmds.add(getPrefix(ctx) + GROUP + "disable <server>");
        cmds.add(getPrefix(ctx) + GROUP + "ping <server>");
        cmds.add(getPrefix(ctx) + GROUP + "refresh <server>");
        cmds.add(getPrefix(ctx) + GROUP + "remove <server>");
        cmds.add(getPrefix(ctx) + GROUP + "exec <server> <cmd> [<args>]");
        cmds.add(getPrefix(ctx) + GROUP + "mexec <cmd> [<args>]");
        cmds.add(getPrefix(ctx) + GROUP + "get <server> <option>|*");
        cmds.add(getPrefix(ctx) + GROUP + "set <server> <option> <value>");
        return cmds;
    }

    @Override
    public void process(Context ctx, Command cmd, List<String> args) throws TransporterException {
        args.remove(0);
        if (args.isEmpty())
            throw new CommandException("do what with a server?");
        String subCmd = args.get(0).toLowerCase();
        args.remove(0);

        if ("list".startsWith(subCmd)) {
            Permissions.require(ctx.getPlayer(), "trp.server.list");
            if (Servers.getAll().isEmpty())
                ctx.send("there are no servers");
            else {
                List<Server> servers = Servers.getAll();
                Collections.sort(servers, new Comparator<Server>() {
                    @Override
                    public int compare(Server a, Server b) {
                        return a.getName().compareToIgnoreCase(b.getName());
                    }
                });
                ctx.send("%d servers:", servers.size());
                for (Server server : servers) {
                    ctx.send("  %s: %s '%s' %s/%s",
                                server.getName(),
                                server.getPluginAddress(),
                                server.getKey(),
                                (server.isEnabled() ? "up" : "down"),
                                (! server.isConnectionConnected() ? "down" :
                                    String.format("up %s %s v%s",
                                        server.isIncoming() ? "incoming" : "outgoing",
                                        server.getConnection().getName(),
                                        server.getRemoteVersion()))
                            );
                    ctx.send("    publicAddress:        %s (%s)",
                            server.getPublicAddress(),
                            server.getNormalizedPublicAddress()
                            );
                    ctx.send("    privateAddress:       %s",
                            server.getPrivateAddress().equals("-") ?
                                "-" :
                                String.format("%s (%s:%d)",
                                    server.getPrivateAddress(),
                                    server.getNormalizedPrivateAddress().getAddress().getHostAddress(),
                                    server.getNormalizedPrivateAddress().getPort()));
                    if (server.isConnectionConnected()) {
                        ctx.send("    remoteServerName:     %s",
                                server.getRemoteServer());
                        ctx.send("    remotePublicAddress:  %s",
                                server.getRemotePublicAddress());
                        ctx.send("    remotePrivateAddress: %s",
                                (server.getRemotePrivateAddress() == null) ?
                                    "-" : server.getRemotePrivateAddress());
                        ctx.send("    remoteRealm:          %s",
                                (server.getRemoteRealm() == null) ?
                                    "-" : (server.getRemoteRealm() + ((Realm.isStarted() && Realm.getName().equals(server.getRemoteRealm())) ? " (realm-mate)" : "")));
                        ctx.send("    remoteCluster:        %s",
                                (server.getRemoteCluster() == null) ?
                                    "-" : (server.getRemoteCluster() + (Network.getClusterName().equals(server.getRemoteCluster()) ? " (cluster-mate)" : "")));
                        ctx.send("    remoteBungeeServer:   %s",
                                (server.getRemoteBungeeServer() == null) ?
                                    "-" : server.getRemoteBungeeServer());
                    }
                }
            }
            return;
        }

        if ("set".startsWith(subCmd)) {
            if (args.isEmpty())
                throw new CommandException("server name required");
            String name = args.remove(0);
            Server server = Servers.find(name);
            if (server == null)
                throw new CommandException("unknown server '%s'", name);
            if (args.isEmpty())
                throw new CommandException("option name required");
            String option = args.remove(0);
            if (args.isEmpty())
                throw new CommandException("option value required");
            String value = args.remove(0);
            server.setOption(ctx, option, value);
            return;
        }

        if ("get".startsWith(subCmd)) {
            if (args.isEmpty())
                throw new CommandException("server name required");
            String name = args.remove(0);
            Server server = Servers.find(name);
            if (server == null)
                throw new CommandException("unknown server '%s'", name);
            if (args.isEmpty())
                throw new CommandException("option name required");
            String option = args.remove(0);
            server.getOptions(ctx, option);
            return;
        }

        if ("add".startsWith(subCmd)) {
            if (args.size() < 3)
                throw new CommandException("server name, address, and key required");
            Permissions.require(ctx.getPlayer(), "trp.server.add");
            String name = args.remove(0);
            String plgAddr = args.remove(0);
            String key = args.remove(0);
            Server server = new Server(name, plgAddr, key);
            Servers.add(server);
            ctx.sendLog("added server '%s'", server.getName());
            return;
        }

        if ("connect".startsWith(subCmd)) {
            if (args.isEmpty())
                throw new CommandException("server name required");
            Server server = Servers.find(args.get(0));
            if (server == null)
                throw new CommandException("unknown server '%s'", args.get(0));
            Permissions.require(ctx.getPlayer(), "trp.server.connect");
            if (server.isConnectionConnected())
                ctx.sendLog("server '%s' is already connected", server.getName());
            else {
                ctx.sendLog("requested server connect for '%s'", server.getName());
                server.connect();
            }
            return;
        }

        if ("disconnect".startsWith(subCmd)) {
            if (args.isEmpty())
                throw new CommandException("server name required");
            Server server = Servers.find(args.get(0));
            if (server == null)
                throw new CommandException("unknown server '%s'", args.get(0));
            Permissions.require(ctx.getPlayer(), "trp.server.disconnect");
            ctx.sendLog("requested server disconnect for '%s'", server.getName());
            server.disconnect(false);
            return;
        }

        if ("enable".startsWith(subCmd)) {
            if (args.isEmpty())
                throw new CommandException("server name required");
            Server server = Servers.find(args.get(0));
            if (server == null)
                throw new CommandException("unknown server '%s'", args.get(0));
            Permissions.require(ctx.getPlayer(), "trp.server.enable");
            server.setEnabled(true);
            ctx.sendLog("server '%s' enabled", server.getName());
            return;
        }

        if ("disable".startsWith(subCmd)) {
            if (args.isEmpty())
                throw new CommandException("server name required");
            Server server = Servers.find(args.get(0));
            if (server == null)
                throw new CommandException("unknown server '%s'", args.get(0));
            Permissions.require(ctx.getPlayer(), "trp.server.disable");
            server.setEnabled(false);
            ctx.sendLog("server '%s' disabled", server.getName());
            return;
        }

        if ("ping".startsWith(subCmd)) {
            if (args.isEmpty())
                throw new CommandException("server name required");
            Server server = Servers.find(args.get(0));
            if (server == null)
                throw new CommandException("unknown server '%s'", args.get(0));
            args.remove(0);
            Permissions.require(ctx.getPlayer(), "trp.server.ping");
            if (! server.isEnabled())
                throw new CommandException("server '%s' is not enabled", server.getName());
            if (! server.isConnectionConnected())
                throw new CommandException("server '%s' is not connected", server.getName());
            server.sendPing(ctx.getPlayer());
            ctx.send("pinging '%s'...", server.getName());
            return;
        }

        if ("refresh".startsWith(subCmd)) {
            if (args.isEmpty())
                throw new CommandException("server name required");
            Server server = Servers.find(args.get(0));
            if (server == null)
                throw new CommandException("unknown server '%s'", args.get(0));
            Permissions.require(ctx.getPlayer(), "trp.server.refresh");
            if (! server.isConnectionConnected())
                ctx.sendLog("server '%s' is not connected", server.getName());
            else {
                server.refresh();
                ctx.sendLog("requested server refresh for '%s'", server.getName());
            }
            return;
        }

        if ("remove".startsWith(subCmd)) {
            if (args.isEmpty())
                throw new CommandException("server name required");
            Server server = Servers.find(args.get(0));
            if (server == null)
                throw new CommandException("unknown server '%s'", args.get(0));
            Permissions.require(ctx.getPlayer(), "trp.server.remove");
            Servers.remove(server);
            ctx.sendLog("removed server '%s'", server.getName());
            return;
        }

        if ("exec".startsWith(subCmd)) {
            if (args.isEmpty())
                throw new CommandException("server name required");
            String serverName = args.remove(0);
            Server server = Servers.find(serverName);
            if (server == null)
                throw new CommandException("unknown server '%s'", serverName);
            if (args.isEmpty())
                throw new CommandException("remote command required");
            String remoteCmd = args.remove(0);
            Permissions.require(ctx.getPlayer(), "trp.server.exec." + remoteCmd);
            StringBuilder remoteArgs = new StringBuilder();
            for (String arg : args) {
                boolean quote = arg.contains(" ");
                remoteArgs.append(' ');
                if (quote) remoteArgs.append('"');
                remoteArgs.append(arg);
                if (quote) remoteArgs.append('"');
            }
            server.dispatchCommand(null, ctx.getSender(), remoteCmd + remoteArgs.toString());
            ctx.sendLog("sent remote command to server '%s'", server.getName());
            return;
        }

        if ("mexec".startsWith(subCmd)) {
            if (args.isEmpty())
                throw new CommandException("remote command required");
            String remoteCmd = args.remove(0);
            Permissions.require(ctx.getPlayer(), "trp.server.mexec." + remoteCmd);
            StringBuilder remoteArgs = new StringBuilder();
            for (String arg : args) {
                boolean quote = arg.contains(" ");
                remoteArgs.append(' ');
                if (quote) remoteArgs.append('"');
                remoteArgs.append(arg);
                if (quote) remoteArgs.append('"');
            }
            for (Server server : Servers.getAll()) {
                if (! server.getMExecTarget()) continue;
                server.dispatchCommand(null, ctx.getSender(), remoteCmd + remoteArgs.toString());
                ctx.sendLog("sent remote command to server '%s'", server.getName());
            }
            return;
        }

        throw new CommandException("do what with a server?");
    }

}
