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
import com.frdfsnlght.transporter.api.TransporterException;
import com.frdfsnlght.transporter.net.Network;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public class NetworkCommand  extends TrpCommandProcessor {

    private static final String GROUP = "network ";

    @Override
    public boolean matches(Context ctx, Command cmd, List<String> args) {
        return super.matches(ctx, cmd, args) &&
               GROUP.startsWith(args.get(0).toLowerCase());
    }

    @Override
    public List<String> getUsage(Context ctx) {
        List<String> cmds = new ArrayList<String>();
        cmds.add(getPrefix(ctx) + GROUP + "ban add <pattern>");
        cmds.add(getPrefix(ctx) + GROUP + "ban remove <pattern>|*");
        cmds.add(getPrefix(ctx) + GROUP + "ban list");
        cmds.add(getPrefix(ctx) + GROUP + "get <option>|*");
        cmds.add(getPrefix(ctx) + GROUP + "set <option> <value>");

        return cmds;
    }

    @Override
    public void process(Context ctx, Command cmd, List<String> args) throws TransporterException {
        args.remove(0);
        if (args.isEmpty())
            throw new CommandException("do what with the network?");
        String subCmd = args.get(0).toLowerCase();
        args.remove(0);

        if ("set".startsWith(subCmd)) {
            if (args.isEmpty())
                throw new CommandException("option name required");
            String option = args.remove(0);
            if (args.isEmpty())
                throw new CommandException("option value required");
            String value = args.remove(0);
            Network.setOption(ctx, option, value);
            return;
        }

        if ("get".startsWith(subCmd)) {
            if (args.isEmpty())
                throw new CommandException("option name required");
            String option = args.remove(0);
            Network.getOptions(ctx, option);
            return;
        }

        if ("ban".startsWith(subCmd)) {
            if (args.isEmpty())
                throw new CommandException("do what with bans?");
            subCmd = args.remove(0).toLowerCase();

            if ("list".startsWith(subCmd)) {
                Permissions.require(ctx.getPlayer(), "trp.network.ban.list");
                List<String> banned = Network.getBannedAddresses();
                if (banned.isEmpty())
                    ctx.send("there are no banned addresses");
                else {
                    ctx.send("%d banned addresses:", banned.size());
                    for (String pattern : banned)
                        ctx.send("  %s", pattern);
                }
                return;
            }

            if (args.isEmpty())
                throw new CommandException("address pattern required");
            String pattern = args.remove(0);

            if ("add".startsWith(subCmd)) {
                Permissions.require(ctx.getPlayer(), "trp.network.ban.add");
                if (Network.addBannedAddress(args.get(0)))
                    ctx.sendLog("added ban");
                else
                    throw new CommandException("'%s' is already banned");
                return;
            }

            if ("remove".startsWith(subCmd)) {
                Permissions.require(ctx.getPlayer(), "trp.network.ban.remove");
                if (pattern.equals("*")) {
                    Network.removeAllBannedAddresses();
                    ctx.sendLog("removed all bans");
                } else if (Network.removeBannedAddress(args.get(0)))
                    ctx.sendLog("removed ban");
                else
                    throw new CommandException("'%s' is not banned");
                return;
            }
            throw new CommandException("do what with a ban?");
        }

        throw new CommandException("do what with the network?");
    }

}
