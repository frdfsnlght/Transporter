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
import com.frdfsnlght.transporter.Gates;
import com.frdfsnlght.transporter.PlayerListenerImpl;
import com.frdfsnlght.transporter.Utils;
import com.frdfsnlght.transporter.api.TransporterException;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public class DebugCommand extends TrpCommandProcessor {

    private static final String GROUP = "debug ";

    @Override
    public boolean matches(Context ctx, Command cmd, List<String> args) {
        return super.matches(ctx, cmd, args) &&
               GROUP.startsWith(args.get(0).toLowerCase());
    }

    @Override
    public List<String> getUsage(Context ctx) {
        if (! ctx.isConsole()) return null;
        List<String> cmds = new ArrayList<String>();
        if (ctx.isConsole())
            cmds.add(getPrefix(ctx) + GROUP + "submit <id>");
        if (ctx.isPlayer())
            cmds.add(getPrefix(ctx) + GROUP + "interact");
        return cmds;
    }

    @Override
    public void process(final Context ctx, Command cmd, List<String> args)  throws TransporterException {
        args.remove(0);
        if (args.isEmpty())
            throw new CommandException("debug what?");
        String subCmd = args.remove(0).toLowerCase();

        if ("submit".startsWith(subCmd)) {
            if (! ctx.isConsole())
                throw new CommandException("this command is only available on the console");
            if (args.isEmpty())
                throw new CommandException("message identifier required, how about a player name?");
            String id = null;
            while (! args.isEmpty()) {
                if (id == null) id = "";
                else id = id + " " + args.remove(0);
            }
            ctx.send("requested submission of debug data");
            Utils.submitDebug(id);
            return;
        }

        if ("interact".startsWith(subCmd)) {
            if (! ctx.isPlayer())
                throw new CommandException("this command is only available to players");
            if (PlayerListenerImpl.testPlayer != ctx.getPlayer()) {
                PlayerListenerImpl.testPlayer = ctx.getPlayer();
                ctx.send("player interaction debug is on");
            } else {
                PlayerListenerImpl.testPlayer = null;
                ctx.send("player interaction debug is off");
            }
            return;
        }

        if ("gatemaps".startsWith(subCmd)) {
            Gates.dumpMaps();
            return;
        }

        throw new CommandException("debug what?");
    }

}
