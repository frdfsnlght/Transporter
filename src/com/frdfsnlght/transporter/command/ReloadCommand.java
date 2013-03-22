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

import com.frdfsnlght.transporter.Config;
import com.frdfsnlght.transporter.Context;
import com.frdfsnlght.transporter.Designs;
import com.frdfsnlght.transporter.Gates;
import com.frdfsnlght.transporter.Permissions;
import com.frdfsnlght.transporter.api.TransporterException;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public class ReloadCommand extends TrpCommandProcessor {

    private static final String GROUP = "reload ";

    @Override
    public boolean matches(Context ctx, Command cmd, List<String> args) {
        return super.matches(ctx, cmd, args) &&
               GROUP.startsWith(args.get(0).toLowerCase());
    }

    @Override
    public List<String> getUsage(Context ctx) {
        List<String> cmds = new ArrayList<String>();
        cmds.add(getPrefix(ctx) + GROUP + "[config|designs|gates]");
        return cmds;
    }

    @Override
    public void process(Context ctx, Command cmd, List<String> args) throws TransporterException {
        args.remove(0);
        List<String> what = new ArrayList<String>();
        if (args.isEmpty()) {
            if (Permissions.has(ctx.getPlayer(), "trp.reload.config"))
                what.add("config");
            if (Permissions.has(ctx.getPlayer(), "trp.reload.designs"))
                what.add("designs");
            if (Permissions.has(ctx.getPlayer(), "trp.reload.gates"))
                what.add("gates");
        } else {
            for (String arg : args) {
                arg = arg.toLowerCase();
                if ("config".startsWith(arg)) arg = "config";
                else if ("designs".startsWith(arg)) arg = "designs";
                else if ("gates".startsWith(arg)) arg = "gates";
                else
                    throw new CommandException("reload what?");
                Permissions.require(ctx.getPlayer(), "trp.reload." + arg);
                what.add(arg);
            }
        }
        for (String arg : what) {
            if (arg.equals("config"))
                Config.load(ctx);
            else if (arg.equals("designs"))
                Designs.load(ctx);
            else if (arg.equals("gates"))
                Gates.load(ctx);
        }
    }

}
