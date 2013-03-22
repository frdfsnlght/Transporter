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
import com.frdfsnlght.transporter.LocalWorldImpl;
import com.frdfsnlght.transporter.Permissions;
import com.frdfsnlght.transporter.Utils;
import com.frdfsnlght.transporter.Worlds;
import com.frdfsnlght.transporter.api.TransporterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public class WorldCommand extends TrpCommandProcessor {

    private static final String GROUP = "world ";

    @Override
    public boolean matches(Context ctx, Command cmd, List<String> args) {
        return super.matches(ctx, cmd, args) &&
               GROUP.startsWith(args.get(0).toLowerCase());
    }

    @Override
    public List<String> getUsage(Context ctx) {
        List<String> cmds = new ArrayList<String>();
        cmds.add(getPrefix(ctx) + GROUP + "list");
        // TODO: change to add, add remove
        cmds.add(getPrefix(ctx) + GROUP + "add <world> [<env>[/<gen>] [<seed>]]");
        cmds.add(getPrefix(ctx) + GROUP + "remove <world>");
        cmds.add(getPrefix(ctx) + GROUP + "load <world>");
        cmds.add(getPrefix(ctx) + GROUP + "unload <world>");
        if (ctx.isPlayer())
            cmds.add(getPrefix(ctx) + GROUP + "go [<coords>] [<world>]");
        cmds.add(getPrefix(ctx) + GROUP + "spawn [<coords>] [<world>]");
        cmds.add(getPrefix(ctx) + GROUP + "get <world> <option>|*");
        cmds.add(getPrefix(ctx) + GROUP + "set <world> <option> <value>");
        return cmds;
    }

    @Override
    public void process(final Context ctx, Command cmd, List<String> args) throws TransporterException {
        args.remove(0);
        if (args.isEmpty())
            throw new CommandException("do what with a world?");
        String subCmd = args.remove(0).toLowerCase();

        if ("list".startsWith(subCmd)) {
            Permissions.require(ctx.getPlayer(), "trp.world.list");
            List<LocalWorldImpl> worlds = Worlds.getAll();
            Collections.sort(worlds, new Comparator<LocalWorldImpl>() {
                @Override
                public int compare(LocalWorldImpl a, LocalWorldImpl b) {
                    return a.getName().compareToIgnoreCase(b.getName());
                }
            });
            ctx.send("%d worlds:", worlds.size());
            for (LocalWorldImpl world : worlds) {
                ctx.send("  %s (%s/%s) %s %s",
                        world.getName(),
                        world.getEnvironment(),
                        (world.getGenerator() == null) ? "-" : world.getGenerator(),
                        (world.getSeed() == null) ? "-" : world.getSeed(),
                        world.isLoaded() ? "loaded" : "not loaded");
                ctx.send("    autoLoad: %s", world.getAutoLoad());
            }
            return;
        }

        if ("add".startsWith(subCmd)) {
            if (args.isEmpty())
                throw new CommandException("world name required");
            String worldName = args.remove(0);
            String environment = "NORMAL";
            String generator = null;
            String seed = null;

            if (! args.isEmpty()) {
                environment = args.remove(0);
                int pos = environment.indexOf("/");
                if (pos != -1) {
                    generator = environment.substring(pos + 1);
                    environment = environment.substring(0, pos);
                }

                if (! args.isEmpty())
                    seed = args.remove(0);
            }

            Environment env;
            try {
                env = Utils.valueOf(World.Environment.class, environment);
            } catch (IllegalArgumentException e) {
                throw new CommandException("%s value for environment", e.getMessage());
            }

            Permissions.require(ctx.getPlayer(), "trp.world.add");

            LocalWorldImpl wp = new LocalWorldImpl(worldName, env, generator, seed);
            wp.load(ctx);
            ctx.sendLog("added world '%s'", worldName);
            Worlds.add(wp);
            return;
        }

        if ("remove".startsWith(subCmd)) {
            if (args.isEmpty())
                throw new CommandException("world name required");
            String name = args.remove(0);
            LocalWorldImpl world = Worlds.find(name);
            if (world == null)
                throw new CommandException("unknown or ambiguous world '%s'", name);
            Permissions.require(ctx.getPlayer(), "trp.world.remove");
            Worlds.remove(world);
            ctx.sendLog("removed world '%s'", name);
            return;
        }

        if ("load".startsWith(subCmd)) {
            if (args.isEmpty())
                throw new CommandException("world name required");
            final String name = args.remove(0);

            Permissions.require(ctx.getPlayer(), "trp.world.load");

            LocalWorldImpl world = Worlds.find(name);
            if (world == null)
                throw new CommandException("unknown or ambiguous world '%s'", name);
            if (world.isLoaded())
                throw new CommandException("world '%s' is already loaded", world.getName());
            ctx.send("loading world '%s'...", world.getName());
            world.load(ctx);
            ctx.send("loaded world '%s'", world.getName());
            return;
        }

        if ("unload".startsWith(subCmd)) {
            if (args.isEmpty())
                throw new CommandException("world name required");
            String name = args.remove(0);

            Permissions.require(ctx.getPlayer(), "trp.world.unload");

            LocalWorldImpl world = Worlds.find(name);
            if (world == null)
                throw new CommandException("unknown or ambiguous world '%s'", name);
            if (! world.isLoaded())
                throw new CommandException("world '%s' is not loaded", world.getName());
            ctx.send("unloading world '%s'...", world.getName());
            World w = world.unload();
            ctx.send("unloaded world '%s'", world.getName());
            return;
        }

        if ("go".startsWith(subCmd)) {
            if (! ctx.isPlayer())
                throw new CommandException("must be a player to use this command");

            World world = ctx.getPlayer().getWorld();
            Location location = world.getSpawnLocation();
            String locationString = null;

            if ((! args.isEmpty()) && (args.get(0).indexOf(',') != -1))
                locationString = args.remove(0);
            if (! args.isEmpty()) {
                String name = args.remove(0);
                LocalWorldImpl bworld = Worlds.find(name);
                if (bworld == null)
                    throw new CommandException("unknown or ambiguous world '%s'", name);
                if (! bworld.isLoaded()) {
                    ctx.send("loading world '%s'...", world.getName());
                    bworld.load(ctx);
                    ctx.send("loaded world '%s'", world.getName());
                }
                world = bworld.getWorld();
                location = world.getSpawnLocation();
                name = bworld.getName();
            }

            if (locationString != null) {
                String ordStrings[] = locationString.split(",");
                double ords[] = new double[ordStrings.length];
                for (int i = 0; i < ordStrings.length; i++)
                    try {
                        ords[i] = Double.parseDouble(ordStrings[i]);
                    } catch (NumberFormatException e) {
                        throw new CommandException("invalid ordinate '%s'", ordStrings[i]);
                    }
                if (ords.length == 2) {
                    // given x,z, so figure out sensible y
                    int y = world.getHighestBlockYAt((int)ords[0], (int)ords[1]) + 1;
                    while (y > 1) {
                        if ((world.getBlockTypeIdAt((int)ords[0], y, (int)ords[1]) == 0) &&
                            (world.getBlockTypeIdAt((int)ords[0], y, (int)ords[1]) == 0)) break;
                        y--;
                    }
                    if (y == 1)
                        throw new CommandException("unable to locate a space big enough for you");
                    location = new Location(world, ords[0], y, ords[1]);
                } else if (ords.length == 3)
                    location = new Location(world, ords[0], ords[1], ords[2]);
                else
                    throw new CommandException("expected 2 or 3 ordinates");
            }

            Permissions.require(ctx.getPlayer(), "trp.world.go");

            ctx.getPlayer().teleport(location);
            ctx.sendLog("teleported to world '%s'", world.getName());

            return;
        }

        if ("spawn".startsWith(subCmd)) {
            World world = ctx.isPlayer() ? ctx.getPlayer().getWorld() : null;
            Location location = ctx.isPlayer() ? ctx.getPlayer().getLocation() : null;
            String locationString = null;

            if ((! args.isEmpty()) && (args.get(0).indexOf(',') != -1))
                locationString = args.remove(0);
            if (! args.isEmpty()) {
                String name = args.remove(0);
                LocalWorldImpl bworld = Worlds.find(name);
                if (bworld == null)
                    throw new CommandException("unknown world '%s'", name);
                if (! bworld.isLoaded())
                    throw new CommandException("world '%s' is not loaded", bworld.getName());
                world = bworld.getWorld();
            }

            if ((world != null) && (locationString != null)) {
                String ordStrings[] = locationString.split(",");
                double ords[] = new double[ordStrings.length];
                for (int i = 0; i < ordStrings.length; i++)
                    try {
                        ords[i] = Double.parseDouble(ordStrings[i]);
                    } catch (NumberFormatException e) {
                        throw new CommandException("invalid ordinate '%s'", ordStrings[i]);
                    }
                if (ords.length == 2) {
                    // given x,z, so figure out sensible y
                    int y = world.getHighestBlockYAt((int)ords[0], (int)ords[1]) + 1;
                    while (y > 1) {
                        if ((world.getBlockTypeIdAt((int)ords[0], y, (int)ords[1]) == 0) &&
                            (world.getBlockTypeIdAt((int)ords[0], y, (int)ords[1]) == 0)) break;
                        y--;
                    }
                    if (y == 1)
                        throw new CommandException("unable to locate a space big enough for a player");
                    location = new Location(world, ords[0], y, ords[1]);
                } else if (ords.length == 3)
                    location = new Location(world, ords[0], ords[1], ords[2]);
                else
                    throw new CommandException("expected 2 or 3 ordinates");
            }
            if ((world != null) && (location == null))
                throw new CommandException("location required");
            if (world == null)
                throw new CommandException("world name required");

            Permissions.require(ctx.getPlayer(), "trp.world.spawn");

            world.setSpawnLocation(location.getBlockX(), location.getBlockY(), location.getBlockZ());
            ctx.sendLog("set spawn location for world '%s'", world.getName());

            return;
        }

        if ("set".startsWith(subCmd)) {
            if (args.isEmpty())
                throw new CommandException("world name required");
            String name = args.remove(0);
            LocalWorldImpl world = Worlds.find(name);
            if (world == null)
                throw new CommandException("unknown world '%s'", name);
            if (args.isEmpty())
                throw new CommandException("option name required");
            String option = args.remove(0);
            if (args.isEmpty())
                throw new CommandException("option value required");
            String value = args.remove(0);
            world.setOption(ctx, option, value);
            return;
        }

        if ("get".startsWith(subCmd)) {
            if (args.isEmpty())
                throw new CommandException("world name required");
            String name = args.remove(0);
            LocalWorldImpl world = Worlds.find(name);
            if (world == null)
                throw new CommandException("unknown world '%s'", name);
            if (args.isEmpty())
                throw new CommandException("option name required");
            String option = args.remove(0);
            world.getOptions(ctx, option);
            return;
        }

        throw new CommandException("do what with a world?");
    }

}
