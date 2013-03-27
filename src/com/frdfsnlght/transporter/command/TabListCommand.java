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
import com.frdfsnlght.transporter.Players;
import com.frdfsnlght.transporter.TabList;
import com.frdfsnlght.transporter.api.TransporterException;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public class TabListCommand  extends TrpCommandProcessor {

    private static final String GROUP = "tablist ";

    @Override
    public boolean matches(Context ctx, Command cmd, List<String> args) {
        return super.matches(ctx, cmd, args) &&
               GROUP.startsWith(args.get(0).toLowerCase());
    }

    @Override
    public List<String> getUsage(Context ctx) {
        List<String> cmds = new ArrayList<String>();
        cmds.add(getPrefix(ctx) + GROUP + "update [<player>|all]");
        cmds.add(getPrefix(ctx) + GROUP + "get <option>|*");
        cmds.add(getPrefix(ctx) + GROUP + "set <option> <value>");

        return cmds;
    }

    @Override
    public void process(Context ctx, Command cmd, List<String> args) throws TransporterException {
        args.remove(0);
        if (args.isEmpty())
            throw new CommandException("do what with tablist support?");
        String subCmd = args.get(0).toLowerCase();
        args.remove(0);

        if ("update".startsWith(subCmd)) {
            Permissions.require(ctx.getPlayer(), "trp.tablist.update");
            Player player;
            if (args.isEmpty()) {
                if (ctx.isConsole())
                    throw new CommandException("this command is only available to players");
                player = ctx.getPlayer();
            } else {
                String playerName = args.remove(0);
                if (playerName.equalsIgnoreCase("all")) {
                    TabList.updateAll();
                    ctx.send("all players' tablists updated");
                    return;
                }
                player = Players.findLocal(playerName);
                if (player == null)
                    throw new CommandException("unknown or ambiguous player");
            }
            TabList.updatePlayer(player);
            ctx.send("player's tablist updated");
            return;
        }

        if ("set".startsWith(subCmd)) {
            if (args.isEmpty())
                throw new CommandException("option name required");
            String option = args.remove(0);
            if (args.isEmpty())
                throw new CommandException("option value required");
            String value = args.remove(0);
            TabList.setOption(ctx, option, value);
            return;
        }

        if ("get".startsWith(subCmd)) {
            if (args.isEmpty())
                throw new CommandException("option name required");
            String option = args.remove(0);
            TabList.getOptions(ctx, option);
            return;
        }

        throw new CommandException("do what with tablist support?");
    }

}
