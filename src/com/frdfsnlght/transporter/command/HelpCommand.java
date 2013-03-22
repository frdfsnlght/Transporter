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
import com.frdfsnlght.transporter.Global;
import com.frdfsnlght.transporter.api.TransporterException;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public class HelpCommand extends TrpCommandProcessor {

    private static final String GROUP = "help ";
    private static final int linesPerPage = 19;

    @Override
    public boolean matches(Context ctx, Command cmd, List<String> args) {
        return super.matches(ctx, cmd, args) &&
               GROUP.startsWith(args.get(0).toLowerCase());
    }

    @Override
    public List<String> getUsage(Context ctx) {
        List<String> cmds = new ArrayList<String>();
        cmds.add(getPrefix(ctx) + GROUP + "[page]");
        return cmds;
    }

    @Override
    public void process(Context ctx, Command cmd, List<String> args)  throws TransporterException {
        args.remove(0);
        List<String> help = new ArrayList<String>();
        for (CommandProcessor cp : Global.commands) {
            List<String> usage = cp.getUsage(ctx);
            if ((usage == null) || usage.isEmpty()) continue;
            if (usage != null)
                help.addAll(usage);
        }

        if (ctx.isConsole()) {
            for (String line : help)
                ctx.send(line);
        } else {
            int page = 1;
            int pages = (int)Math.ceil((double)help.size() / (double)linesPerPage);
            if (! args.isEmpty()) {
                try {
                    page = Integer.parseInt(args.get(0));
                } catch (NumberFormatException nfe) {}
                if (page < 1) page = 1;
                if (page > pages) page = pages;
            }
            int min = (page - 1) * linesPerPage;
            int max = (page * linesPerPage) - 1;
            if (max > (help.size() - 1)) max = help.size() - 1;

            ctx.send("Help page " + page + " of " + pages);
            for (int i = min; i <= max; i++)
                ctx.send(help.get(i));
        }
    }

}
