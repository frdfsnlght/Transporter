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
import com.frdfsnlght.transporter.Design;
import com.frdfsnlght.transporter.Designs;
import com.frdfsnlght.transporter.Economy;
import com.frdfsnlght.transporter.EconomyException;
import com.frdfsnlght.transporter.Gates;
import com.frdfsnlght.transporter.Global;
import com.frdfsnlght.transporter.Inventory;
import com.frdfsnlght.transporter.LocalAreaGateImpl;
import com.frdfsnlght.transporter.LocalGateImpl;
import com.frdfsnlght.transporter.LocalServerGateImpl;
import com.frdfsnlght.transporter.Permissions;
import com.frdfsnlght.transporter.Utils;
import com.frdfsnlght.transporter.api.TransporterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public class DesignCommand extends TrpCommandProcessor {

    private static final String GROUP = "design ";

    @Override
    public boolean matches(Context ctx, Command cmd, List<String> args) {
        return super.matches(ctx, cmd, args) &&
               GROUP.startsWith(args.get(0).toLowerCase());
    }

    @Override
    public List<String> getUsage(Context ctx) {
        List<String> cmds = new ArrayList<String>();
        cmds.add(getPrefix(ctx) + GROUP + "list");
        if (ctx.isPlayer()) {
            cmds.add(getPrefix(ctx) + GROUP + "build <designname>|undo");
            cmds.add(getPrefix(ctx) + GROUP + "create <designname>|area|server <gatename> [<to> [rev]]");
        }
        return cmds;
    }

    @Override
    public void process(Context ctx, Command cmd, List<String> args) throws TransporterException {
        args.remove(0);
        if (args.isEmpty())
            throw new CommandException("do what with a design?");
        String subCmd = args.remove(0).toLowerCase();

        if ("list".startsWith(subCmd)) {
            Permissions.require(ctx.getPlayer(), "trp.design.list");
            if (Designs.getAll().isEmpty())
                ctx.send("there are no designs");
            else {
                List<Design> designs = Designs.getAll();
                Collections.sort(designs, new Comparator<Design>() {
                    @Override
                    public int compare(Design a, Design b) {
                        return a.getName().compareToIgnoreCase(b.getName());
                    }
                });
                ctx.send("%d designs:", designs.size());
                for (Design design : designs)
                    ctx.send("  %s", design.getName());
            }
            return;
        }

        if ("build".startsWith(subCmd)) {
            if (! ctx.isPlayer())
                throw new CommandException("this command can only be used by a player");

            if (! Config.getAllowBuild())
                throw new CommandException("building gates is not permitted");

            if (args.isEmpty())
                throw new CommandException("design name required");
            String designName = args.remove(0);
            Player player = ctx.getPlayer();

            if (designName.toLowerCase().equals("undo")) {
                Permissions.require(ctx.getPlayer(), "trp.design.build.undo");
                if (Designs.undoBuild(player.getName()))
                    ctx.sendLog("build undone");
                else
                    throw new CommandException("nothing to undo");
                return;
            }

            Design design = Designs.get(designName);
            if (design == null)
                throw new CommandException("unknown design '%s'", designName);
            if (! design.isBuildable())
                throw new CommandException("design '%s' is not buildable", design.getName());
            World world = player.getWorld();
            if (! design.isBuildableInWorld(world))
                throw new CommandException("gate type '%s' is not buildable in this world", design.getName());

            Permissions.require(ctx.getPlayer(), "trp.design.build." + design.getName());
            Economy.requireFunds(ctx.getPlayer(), design.getBuildCost());
            if (design.mustBuildFromInventory())
                Inventory.requireBlocks(ctx.getPlayer(), design.getInventoryBlocks());

            design.build(player.getLocation(), player.getName());

            try {
                if (Economy.deductFunds(ctx.getPlayer(), design.getBuildCost()))
                    ctx.sendLog("debited %s for gate construction", design.getBuildCost());
            } catch (EconomyException ee) {
                Utils.warning("unable to debit gate construction costs for %s: %s", ctx.getPlayer().getName(), ee.getMessage());
            }

            if (design.mustBuildFromInventory())
                if (Inventory.deductBlocks(ctx.getPlayer(), design.getInventoryBlocks()))
                    ctx.sendLog("debited inventory");

            return;
        }

        if ("create".startsWith(subCmd)) {
            if (! ctx.isPlayer())
                throw new CommandException("this command can only be used by a player");

            if (! Config.getAllowBuild())
                throw new CommandException("building gates is not permitted");

            if (args.isEmpty())
                throw new CommandException("design name, 'area', or 'server' required");
            String designName = args.remove(0);
            if (args.isEmpty())
                throw new CommandException("gate name required");
            String gateName = args.remove(0);
            String link = null;
            if (args.size() > 0)
                link = args.remove(0);
            boolean rev = (args.size() > 0) && ("reverse".startsWith(args.get(args.size() - 1).toLowerCase()));

            Player player = ctx.getPlayer();
            World world = player.getWorld();

            if ("area".startsWith(designName)) {
                Permissions.require(ctx.getPlayer(), "trp.create.area");
                LocalGateImpl gate = new LocalAreaGateImpl(ctx.getPlayer().getWorld(), gateName, ctx.getPlayer().getName(), Utils.yawToDirection(ctx.getPlayer().getLocation().getYaw()), ctx.getPlayer().getLocation());
                Gates.add(gate, true);
                ctx.sendLog("created gate '%s'", gate.getName());
                Gates.setSelectedGate(ctx.getPlayer(), gate);
            } else if ("server".startsWith(designName)) {
                Permissions.require(ctx.getPlayer(), "trp.create.server");
                LocalGateImpl gate = new LocalServerGateImpl(Global.plugin.getServer().getWorlds().get(0), gateName, ctx.getPlayer().getName());
                Gates.add(gate, true);
                ctx.sendLog("created gate '%s'", gate.getName());
                Gates.setSelectedGate(ctx.getPlayer(), gate);
            } else {
                Design design = Designs.get(designName);
                if (design == null)
                    throw new CommandException("unknown design '%s'", designName);
                if (! design.isBuildable())
                    throw new CommandException("design '%s' is not buildable", design.getName());
                if (! design.isBuildableInWorld(world))
                    throw new CommandException("gate type '%s' is not buildable in this world", design.getName());
                Permissions.require(ctx.getPlayer(), "trp.design.build." + design.getName());
                Permissions.require(ctx.getPlayer(), "trp.create." + design.getName());
                Economy.requireFunds(ctx.getPlayer(), design.getBuildCost() + design.getCreateCost());
                if (design.mustBuildFromInventory())
                    Inventory.requireBlocks(ctx.getPlayer(), design.getInventoryBlocks());

                LocalGateImpl gate = design.create(player.getLocation(), player.getName(), gateName);
                Gates.add(gate, true);
                ctx.sendLog("created gate '%s'", gate.getName());
                Gates.setSelectedGate(ctx.getPlayer(), gate);

                try {
                    if (Economy.deductFunds(ctx.getPlayer(), design.getBuildCost()))
                        ctx.sendLog("debited %s for gate construction", Economy.format(design.getBuildCost()));
                } catch (EconomyException ee) {
                    Utils.warning("unable to debit gate construction costs for %s: %s", ctx.getPlayer().getName(), ee.getMessage());
                }
                try {
                    if (Economy.deductFunds(ctx.getPlayer(), design.getCreateCost()))
                        ctx.sendLog("debited %s for gate creation", Economy.format(design.getCreateCost()));
                } catch (EconomyException ee) {
                    Utils.warning("unable to debit gate creation costs for %s: %s", ctx.getPlayer().getName(), ee.getMessage());
                }

                if (design.mustBuildFromInventory())
                    if (Inventory.deductBlocks(ctx.getPlayer(), design.getInventoryBlocks()))
                        ctx.sendLog("debited inventory");
            }

            if (link == null) return;
            ctx.getPlayer().performCommand("trp gate link add \"" + link + "\"" + (rev ? " rev" : ""));

            return;
        }

        throw new CommandException("do what with a design?");
    }

}
