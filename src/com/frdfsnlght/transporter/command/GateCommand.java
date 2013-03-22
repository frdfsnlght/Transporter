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
import com.frdfsnlght.transporter.Economy;
import com.frdfsnlght.transporter.GateImpl;
import com.frdfsnlght.transporter.Gates;
import com.frdfsnlght.transporter.Global;
import com.frdfsnlght.transporter.LocalAreaGateImpl;
import com.frdfsnlght.transporter.LocalGateImpl;
import com.frdfsnlght.transporter.Permissions;
import com.frdfsnlght.transporter.RemoteGateImpl;
import com.frdfsnlght.transporter.Server;
import com.frdfsnlght.transporter.Utils;
import com.frdfsnlght.transporter.api.ExpandDirection;
import com.frdfsnlght.transporter.api.GateType;
import com.frdfsnlght.transporter.api.TransporterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public class GateCommand extends TrpCommandProcessor {

    private static final String GROUP = "gate ";

    @Override
    public boolean matches(Context ctx, Command cmd, List<String> args) {
        return super.matches(ctx, cmd, args) &&
               GROUP.startsWith(args.get(0).toLowerCase());
    }

    @Override
    public List<String> getUsage(Context ctx) {
        List<String> cmds = new ArrayList<String>();
        cmds.add(getPrefix(ctx) + GROUP + "list");
        cmds.add(getPrefix(ctx) + GROUP + "select <gate>");
        cmds.add(getPrefix(ctx) + GROUP + "info [<gate>]");
        cmds.add(getPrefix(ctx) + GROUP + "open [<gate>]");
        cmds.add(getPrefix(ctx) + GROUP + "close [<gate>]");
        cmds.add(getPrefix(ctx) + GROUP + "rebuild [<gate>]");
        cmds.add(getPrefix(ctx) + GROUP + "destroy [<gate>] [unbuild]");
        cmds.add(getPrefix(ctx) + GROUP + "rename <newname> [<gate>]");
        cmds.add(getPrefix(ctx) + GROUP + "link add [<from>] <to> [rev]");
        cmds.add(getPrefix(ctx) + GROUP + "link remove [<from>] <to> [rev]");
        cmds.add(getPrefix(ctx) + GROUP + "link next [<gate>]");
        cmds.add(getPrefix(ctx) + GROUP + "pin add <pin> [<gate>]");
        cmds.add(getPrefix(ctx) + GROUP + "pin remove <pin>|* [<gate>]");

        cmds.add(getPrefix(ctx) + GROUP + "ban item list [<gate>]");
        cmds.add(getPrefix(ctx) + GROUP + "ban item add <item> [<gate>]");
        cmds.add(getPrefix(ctx) + GROUP + "ban item remove <item>|* [<gate>]");
        cmds.add(getPrefix(ctx) + GROUP + "allow item list [<gate>]");
        cmds.add(getPrefix(ctx) + GROUP + "allow item add <item> [<gate>]");
        cmds.add(getPrefix(ctx) + GROUP + "allow item remove <item>|* [<gate>]");
        cmds.add(getPrefix(ctx) + GROUP + "replace item list [<gate>]");
        cmds.add(getPrefix(ctx) + GROUP + "replace item add <old> <new> [<gate>]");
        cmds.add(getPrefix(ctx) + GROUP + "replace item remove <old>|* [<gate>]");

        cmds.add(getPrefix(ctx) + GROUP + "ban potion list [<gate>]");
        cmds.add(getPrefix(ctx) + GROUP + "ban potion add <potion> [<gate>]");
        cmds.add(getPrefix(ctx) + GROUP + "ban potion remove <potion>|* [<gate>]");
        cmds.add(getPrefix(ctx) + GROUP + "allow potion list [<gate>]");
        cmds.add(getPrefix(ctx) + GROUP + "allow potion add <potion> [<gate>]");
        cmds.add(getPrefix(ctx) + GROUP + "allow potion remove <potion>|* [<gate>]");
        cmds.add(getPrefix(ctx) + GROUP + "replace potion list [<gate>]");
        cmds.add(getPrefix(ctx) + GROUP + "replace potion add <old> <new> [<gate>]");
        cmds.add(getPrefix(ctx) + GROUP + "replace potion remove <old>|* [<gate>]");

        cmds.add(getPrefix(ctx) + GROUP + "resize <num>[,<direction>] [<gate>]");
        if (ctx.isPlayer()) {
            cmds.add(getPrefix(ctx) + GROUP + "corner 1|2 [pick] [<gate>]");
            cmds.add(getPrefix(ctx) + GROUP + "create <designname>|area|server <gatename> [<to> [rev]]");
        }

        cmds.add(getPrefix(ctx) + GROUP + "get <option>|* [<gate>]");
        cmds.add(getPrefix(ctx) + GROUP + "set <option> <value> [<gate>]");
        return cmds;
    }

    @Override
    public void process(Context ctx, Command cmd, List<String> args) throws TransporterException {
        args.remove(0);
        if (args.isEmpty())
            throw new CommandException("do what with a gate?");
        String subCmd = args.remove(0).toLowerCase();

        if ("list".startsWith(subCmd)) {
            Permissions.require(ctx.getPlayer(), "trp.gate.list");

            List<LocalGateImpl> localGates = new ArrayList<LocalGateImpl>(Gates.getLocalGates());
            if ((! ctx.isConsole()) && (! ctx.isOp()))
                for (Iterator<LocalGateImpl> i = localGates.iterator(); i.hasNext(); )
                    if (i.next().getHidden()) i.remove();
            if (localGates.isEmpty())
                ctx.send("there are no local gates");
            else {
                Collections.sort(localGates, new Comparator<LocalGateImpl>() {
                    @Override
                    public int compare(LocalGateImpl a, LocalGateImpl b) {
                        return a.getLocalName().compareToIgnoreCase(b.getLocalName());
                    }
                });
                ctx.send("%d local gates:", localGates.size());
                for (LocalGateImpl gate : localGates)
                    ctx.send("  %s", gate.getLocalName());
            }
            List<RemoteGateImpl> remoteGates = new ArrayList<RemoteGateImpl>(Gates.getRemoteGates());
            if ((! ctx.isConsole()) && (! ctx.isOp()))
                for (Iterator<RemoteGateImpl> i = remoteGates.iterator(); i.hasNext(); )
                    if (i.next().getHidden()) i.remove();
            if (remoteGates.isEmpty())
                ctx.send("there are no remote gates");
            else {
                Collections.sort(remoteGates, new Comparator<RemoteGateImpl>() {
                    @Override
                    public int compare(RemoteGateImpl a, RemoteGateImpl b) {
                        return a.getFullName().compareToIgnoreCase(b.getFullName());
                    }
                });
                ctx.send("%d remote gates:", remoteGates.size());
                for (RemoteGateImpl gate : remoteGates)
                    ctx.send("  %s", gate.getFullName());
            }
            return;
        }

        if ("select".startsWith(subCmd)) {
            LocalGateImpl gate = getGate(ctx, args);
            Permissions.require(ctx.getPlayer(), "trp.gate.select." + gate.getFullName());
            Gates.setSelectedGate(ctx.getPlayer(), gate);
            ctx.send("selected gate '%s'", gate.getFullName());
            return;
        }

        if ("info".startsWith(subCmd)) {
            LocalGateImpl gate = getGate(ctx, args);
            Permissions.require(ctx.getPlayer(), "trp.gate.info." + gate.getFullName());
            ctx.send("Full name: %s", gate.getFullName());
            ctx.send("Type: %s", gate.getType().toString());
            ctx.send("Creator: %s", gate.getCreatorName());
            if (Economy.isAvailable()) {
                if (gate.getLinkLocal())
                    ctx.send("On-world travel cost: %s/%s",
                            Economy.format(gate.getSendLocalCost()),
                            Economy.format(gate.getReceiveLocalCost()));
                if (gate.getLinkWorld())
                    ctx.send("Off-world travel cost: %s/%s",
                            Economy.format(gate.getSendWorldCost()),
                            Economy.format(gate.getReceiveWorldCost()));
                if (gate.getLinkServer())
                    ctx.send("Off-server travel cost: %s/%s",
                            Economy.format(gate.getSendServerCost()),
                            Economy.format(gate.getReceiveServerCost()));
            }
            List<String> links = gate.getLinks();
            ctx.send("Links: %d", links.size());
            for (String link : links)
                ctx.send(" %s%s", link.equals(gate.getDestinationLink()) ? "*": "", link);
            return;
        }

        if ("open".startsWith(subCmd)) {
            LocalGateImpl gate = getGate(ctx, args);
            if (gate.isOpen())
                ctx.warn("gate '%s' is already open", gate.getName(ctx));
            else {
                Permissions.require(ctx.getPlayer(), "trp.gate.open." + gate.getFullName());
                gate.open();
                ctx.sendLog("opened gate '%s'", gate.getName(ctx));
            }
            return;
        }

        if ("close".startsWith(subCmd)) {
            LocalGateImpl gate = getGate(ctx, args);
            if (gate.isOpen()) {
                Permissions.require(ctx.getPlayer(), "trp.gate.close." + gate.getFullName());
                gate.close();
                ctx.sendLog("closed gate '%s'", gate.getName(ctx));
            } else
                ctx.warn("gate '%s' is already closed", gate.getName(ctx));
            return;
        }

        if ("rebuild".startsWith(subCmd)) {
            LocalGateImpl gate = getGate(ctx, args);
            Permissions.require(ctx.getPlayer(), "trp.gate.rebuild." + gate.getFullName());
            gate.rebuild();
            ctx.sendLog("rebuilt gate '%s'", gate.getName(ctx));
            return;
        }

        if ("destroy".startsWith(subCmd)) {
            boolean unbuild = false;
            if ((args.size() > 0) && "unbuild".startsWith(args.get(args.size() - 1).toLowerCase())) {
                unbuild = true;
                args.remove(args.size() - 1);
            }
            LocalGateImpl gate = getGate(ctx, args);
            Permissions.require(ctx.getPlayer(), "trp.gate.destroy." + gate.getFullName());
            Gates.destroy(gate, unbuild);
            ctx.sendLog("destroyed gate '%s'", gate.getName(ctx));
            return;
        }

        if ("rename".startsWith(subCmd)) {
            if (args.isEmpty())
                throw new CommandException("new name required");
            String newName = args.remove(0);
            LocalGateImpl gate = getGate(ctx, args);
            String oldName = gate.getName(ctx);
            Permissions.require(ctx.getPlayer(), "trp.gate.rename");
            Gates.rename(gate, newName);
            ctx.sendLog("renamed gate '%s' to '%s'", oldName, gate.getName(ctx));
            return;
        }

        if ("link".startsWith(subCmd)) {
            if (args.isEmpty())
                throw new CommandException("do what with a link?");
            subCmd = args.remove(0).toLowerCase();

            if ("next".startsWith(subCmd)) {
                LocalGateImpl fromGate = getGate(ctx, args);
                fromGate.nextLink();
                return;
            }

            if (args.isEmpty())
                throw new CommandException("destination endpoint required");
            boolean reverse = false;
            if ("reverse".startsWith(args.get(args.size() - 1).toLowerCase())) {
                reverse = true;
                args.remove(args.size() - 1);
            }
            if (args.isEmpty())
                throw new CommandException("destination endpoint required");

            String toGateName = args.remove(args.size() - 1);
            LocalGateImpl fromGate = getGate(ctx, args);

            GateImpl toGate = Gates.find(ctx, toGateName);

            if ("add".startsWith(subCmd)) {
                fromGate.addLink(ctx, toGateName);
                if (reverse && (ctx.getSender() != null) && (toGate != null)) {
                    if (toGate.isSameServer())
                        Global.plugin.getServer().dispatchCommand(ctx.getSender(), "trp gate link add \"" + toGate.getFullName() + "\" \"" + fromGate.getFullName() + "\"");
                    else {
                        Server server = (Server)((RemoteGateImpl)toGate).getRemoteServer();
                        if (! server.isConnectionConnected())
                            ctx.send("unable to add reverse link from offline server");
                        else
                            server.sendLinkAdd(ctx.getPlayer(), (LocalGateImpl)fromGate, (RemoteGateImpl)toGate);
                    }
                }
                return;
            }

            if ("remove".startsWith(subCmd)) {
                fromGate.removeLink(ctx, toGateName);
                if (reverse && (ctx.getSender() != null) && (toGate != null)) {
                    if (toGate.isSameServer())
                        Global.plugin.getServer().dispatchCommand(ctx.getSender(), "trp gate link remove \"" + fromGate.getFullName() + "\" \"" + toGate.getFullName() + "\"");
                    else {
                        Server server = (Server)((RemoteGateImpl)toGate).getRemoteServer();
                        if (! server.isConnectionConnected())
                            ctx.send("unable to remove reverse link from offline server");
                        else
                            server.sendLinkRemove(ctx.getPlayer(), (LocalGateImpl)fromGate, (RemoteGateImpl)toGate);
                    }
                }
                return;
            }
            throw new CommandException("do what with a link?");
        }

        if ("pin".startsWith(subCmd)) {
            if (args.isEmpty())
                throw new CommandException("do what with a pin?");
            subCmd = args.remove(0).toLowerCase();
            if (args.isEmpty())
                throw new CommandException("pin required");
            String pin = args.remove(0);
            LocalGateImpl gate = getGate(ctx, args);

            if ("add".startsWith(subCmd)) {
                Permissions.require(ctx.getPlayer(), "trp.gate.pin.add." + gate.getFullName());
                if (gate.addPin(pin))
                    ctx.send("added pin to '%s'", gate.getName(ctx));
                else
                    throw new CommandException("pin is already added");
                return;
            }

            if ("remove".startsWith(subCmd)) {
                Permissions.require(ctx.getPlayer(), "trp.gate.pin.remove." + gate.getFullName());
                if (pin.equals("*")) {
                    gate.removeAllPins();
                    ctx.send("removed all pins from '%s'", gate.getName(ctx));
                } else if (gate.removePin(pin))
                    ctx.send("removed pin from '%s'", gate.getName(ctx));
                else
                    throw new CommandException("pin not found");
                return;
            }
            throw new CommandException("do what with a pin?");
        }

        if ("ban".startsWith(subCmd)) {
            if (args.isEmpty())
                throw new CommandException("what type of ban?");
            String type = args.remove(0).toLowerCase();
            if (args.isEmpty())
                throw new CommandException("do what with a ban?");
            subCmd = args.remove(0).toLowerCase();

            LocalGateImpl gate;

            if ("item".startsWith(type)) {
                if ("list".startsWith(subCmd)) {
                    gate = getGate(ctx, args);
                    Permissions.require(ctx.getPlayer(), "trp.gate.ban.item.list." + gate.getFullName());
                    List<String> items = new ArrayList<String>(gate.getBannedItems());
                    Collections.sort(items);
                    ctx.send("%s items", items.size());
                    for (String item : items)
                        ctx.send("  %s", item);
                    return;
                }
                if (args.isEmpty())
                    throw new CommandException("item required");
                String item = args.remove(0);
                gate = getGate(ctx, args);
                if ("add".startsWith(subCmd)) {
                    Permissions.require(ctx.getPlayer(), "trp.gate.ban.item.add." + gate.getFullName());
                    if (gate.addBannedItem(item))
                        ctx.send("added banned item to '%s'", gate.getName(ctx));
                    else
                        throw new CommandException("item is already banned");
                    return;
                }
                if ("remove".startsWith(subCmd)) {
                    Permissions.require(ctx.getPlayer(), "trp.gate.ban.item.remove." + gate.getFullName());
                    if (item.equals("*")) {
                        gate.removeAllBannedItems();
                        ctx.send("removed all banned items from '%s'", gate.getName(ctx));
                    } else if (gate.removeBannedItem(item))
                        ctx.send("removed banned item from '%s'", gate.getName(ctx));
                    else
                        throw new CommandException("banned item not found");
                    return;
                }
                throw new CommandException("do what with an item ban?");
            }

            if ("potion".startsWith(type)) {
                if ("list".startsWith(subCmd)) {
                    gate = getGate(ctx, args);
                    Permissions.require(ctx.getPlayer(), "trp.gate.ban.potion.list." + gate.getFullName());
                    List<String> potions = new ArrayList<String>(gate.getBannedPotions());
                    Collections.sort(potions);
                    ctx.send("%s potion effects", potions.size());
                    for (String potion : potions)
                        ctx.send("  %s", potion);
                    return;
                }
                if (args.isEmpty())
                    throw new CommandException("potion effect required");
                String potion = args.remove(0);
                gate = getGate(ctx, args);
                if ("add".startsWith(subCmd)) {
                    Permissions.require(ctx.getPlayer(), "trp.gate.ban.potion.add." + gate.getFullName());
                    if (gate.addBannedPotion(potion))
                        ctx.send("added banned potion effect to '%s'", gate.getName(ctx));
                    else
                        throw new CommandException("potion effect is already banned");
                    return;
                }
                if ("remove".startsWith(subCmd)) {
                    Permissions.require(ctx.getPlayer(), "trp.gate.ban.potion.remove." + gate.getFullName());
                    if (potion.equals("*")) {
                        gate.removeAllBannedPotions();
                        ctx.send("removed all banned potion effects from '%s'", gate.getName(ctx));
                    } else if (gate.removeBannedPotion(potion))
                        ctx.send("removed banned potion effect from '%s'", gate.getName(ctx));
                    else
                        throw new CommandException("banned potion effect not found");
                    return;
                }
                throw new CommandException("do what with a potion effect ban?");
            }

            throw new CommandException("do what with a ban?");
        }

        if ("allow".startsWith(subCmd)) {
            if (args.isEmpty())
                throw new CommandException("what type of allow?");
            String type = args.remove(0).toLowerCase();
            if (args.isEmpty())
                throw new CommandException("do what with an allow?");
            subCmd = args.remove(0).toLowerCase();

            LocalGateImpl gate;

            if ("item".startsWith(type)) {
                if ("list".startsWith(subCmd)) {
                    gate = getGate(ctx, args);
                    Permissions.require(ctx.getPlayer(), "trp.gate.allow.item.list." + gate.getFullName());
                    List<String> items = new ArrayList<String>(gate.getAllowedItems());
                    Collections.sort(items);
                    ctx.send("%s items", items.size());
                    for (String item : items)
                        ctx.send("  %s", item);
                    return;
                }
                if (args.isEmpty())
                    throw new CommandException("item required");
                String item = args.remove(0);
                gate = getGate(ctx, args);
                if ("add".startsWith(subCmd)) {
                    Permissions.require(ctx.getPlayer(), "trp.gate.allow.item.add." + gate.getFullName());
                    if (gate.addAllowedItem(item))
                        ctx.send("added allowed item to '%s'", gate.getName(ctx));
                    else
                        throw new CommandException("item is already allowed");
                    return;
                }
                if ("remove".startsWith(subCmd)) {
                    Permissions.require(ctx.getPlayer(), "trp.gate.allow.item.remove." + gate.getFullName());
                    if (item.equals("*")) {
                        gate.removeAllAllowedItems();
                        ctx.send("removed all allowed items from '%s'", gate.getName(ctx));
                    } else if (gate.removeAllowedItem(item))
                        ctx.send("removed allowed item from '%s'", gate.getName(ctx));
                    else
                        throw new CommandException("allowed item not found");
                    return;
                }
                throw new CommandException("do what with an item allow?");
            }
            if ("potion".startsWith(type)) {
                if ("list".startsWith(subCmd)) {
                    gate = getGate(ctx, args);
                    Permissions.require(ctx.getPlayer(), "trp.gate.allow.potion.list." + gate.getFullName());
                    List<String> potions = new ArrayList<String>(gate.getAllowedPotions());
                    Collections.sort(potions);
                    ctx.send("%s potion effects", potions.size());
                    for (String potion : potions)
                        ctx.send("  %s", potion);
                    return;
                }
                if (args.isEmpty())
                    throw new CommandException("potion effect required");
                String potion = args.remove(0);
                gate = getGate(ctx, args);
                if ("add".startsWith(subCmd)) {
                    Permissions.require(ctx.getPlayer(), "trp.gate.allow.potion.add." + gate.getFullName());
                    if (gate.addAllowedPotion(potion))
                        ctx.send("added allowed potion effect to '%s'", gate.getName(ctx));
                    else
                        throw new CommandException("potion effect is already allowed");
                    return;
                }
                if ("remove".startsWith(subCmd)) {
                    Permissions.require(ctx.getPlayer(), "trp.gate.allow.potion.remove." + gate.getFullName());
                    if (potion.equals("*")) {
                        gate.removeAllAllowedPotions();
                        ctx.send("removed all allowed potions from '%s'", gate.getName(ctx));
                    } else if (gate.removeAllowedPotion(potion))
                        ctx.send("removed allowed potion effect from '%s'", gate.getName(ctx));
                    else
                        throw new CommandException("allowed potion effect not found");
                    return;
                }
                throw new CommandException("do what with a potion effect allow?");
            }

            throw new CommandException("do what with an allow?");
        }

        if ("replace".startsWith(subCmd)) {
            if (args.isEmpty())
                throw new CommandException("what type of replace?");
            String type = args.remove(0).toLowerCase();
            if (args.isEmpty())
                throw new CommandException("do what with a replace?");
            subCmd = args.remove(0).toLowerCase();

            LocalGateImpl gate;

            if ("item".startsWith(type)) {
                if ("list".startsWith(subCmd)) {
                    gate = getGate(ctx, args);
                    Permissions.require(ctx.getPlayer(), "trp.gate.replace.item.list." + gate.getFullName());
                    Map<String,String> items = new HashMap<String,String>(gate.getReplaceItems());
                    List<String> keys = new ArrayList<String>(items.keySet());
                    Collections.sort(keys);
                    ctx.send("%s items", items.size());
                    for (String key : keys)
                        ctx.send("  %s => %s", key, items.get(key));
                    return;
                }
                if (args.isEmpty())
                    throw new CommandException("item required");
                String oldItem = args.remove(0);
                if ("add".startsWith(subCmd)) {
                    if (args.isEmpty())
                        throw new CommandException("new item required");
                    String newItem = args.remove(0);
                    gate = getGate(ctx, args);
                    Permissions.require(ctx.getPlayer(), "trp.gate.replace.item.add." + gate.getFullName());
                    if (gate.addReplaceItem(oldItem, newItem))
                        ctx.send("added replace item to '%s'", gate.getName(ctx));
                    else
                        throw new CommandException("item is already replaced");
                    return;
                }
                if ("remove".startsWith(subCmd)) {
                    gate = getGate(ctx, args);
                    Permissions.require(ctx.getPlayer(), "trp.gate.replace.item.remove." + gate.getFullName());
                    if (oldItem.equals("*")) {
                        gate.removeAllReplaceItems();
                        ctx.send("removed all replace items from '%s'", gate.getName(ctx));
                    } else if ( gate.removeReplaceItem(oldItem))
                        ctx.send("removed replace item from '%s'", gate.getName(ctx));
                    else
                        throw new CommandException("replace item not found");
                    return;
                }
                throw new CommandException("do what with an item replace?");
            }
            if ("potion".startsWith(type)) {
                if ("list".startsWith(subCmd)) {
                    gate = getGate(ctx, args);
                    Permissions.require(ctx.getPlayer(), "trp.gate.replace.potion.list." + gate.getFullName());
                    Map<String,String> potions = new HashMap<String,String>(gate.getReplacePotions());
                    List<String> keys = new ArrayList<String>(potions.keySet());
                    Collections.sort(keys);
                    ctx.send("%s potion effects", potions.size());
                    for (String key : keys)
                        ctx.send("  %s => %s", key, potions.get(key));
                    return;
                }
                if (args.isEmpty())
                    throw new CommandException("potion effect required");
                String oldPotion = args.remove(0);
                if ("add".startsWith(subCmd)) {
                    if (args.isEmpty())
                        throw new CommandException("new potion effect required");
                    String newPotion = args.remove(0);
                    gate = getGate(ctx, args);
                    Permissions.require(ctx.getPlayer(), "trp.gate.replace.potion.add." + gate.getFullName());
                    if (gate.addReplacePotion(oldPotion, newPotion))
                        ctx.send("added replace potion effect to '%s'", gate.getName(ctx));
                    else
                        throw new CommandException("potion effect is already replaced");
                    return;
                }
                if ("remove".startsWith(subCmd)) {
                    gate = getGate(ctx, args);
                    Permissions.require(ctx.getPlayer(), "trp.gate.replace.potion.remove." + gate.getFullName());
                    if (oldPotion.equals("*")) {
                        gate.removeAllReplacePotions();
                        ctx.send("removed all replace potion effects from '%s'", gate.getName(ctx));
                    } else if ( gate.removeReplacePotion(oldPotion))
                        ctx.send("removed replace potion effect from '%s'", gate.getName(ctx));
                    else
                        throw new CommandException("replace potion effect not found");
                    return;
                }
                throw new CommandException("do what with a potion replace?");
            }

            throw new CommandException("do what with a replace?");
        }

        if ("resize".startsWith(subCmd)) {
            if (args.isEmpty())
                throw new CommandException("number and direction required");
            String numDir = args.remove(0);
            String[] numDirParts = numDir.split(",");
            int num;
            ExpandDirection dir = ExpandDirection.ALL;
            try {
                num = Integer.parseInt(numDirParts[0]);
            } catch (NumberFormatException nfe) {
                throw new CommandException("invalid number");
            }
            if (numDirParts.length > 1)
                try {
                    dir = Utils.valueOf(ExpandDirection.class, numDirParts[1]);
                } catch (IllegalArgumentException iae) {
                    throw new CommandException(iae.getMessage() + " direction");
                }
            LocalGateImpl gate = getGate(ctx, args);
            if (gate.getType() != GateType.AREA)
                throw new CommandException("this command is only valid for %s gates", GateType.AREA);
            Permissions.require(ctx.getPlayer(), "trp.gate.resize." + gate.getFullName());
            ((LocalAreaGateImpl)gate).resize(num, dir);
            return;
        }

        if ("corner".startsWith(subCmd)) {
            if (! ctx.isPlayer())
                throw new CommandException("must be a player to use this command");
            if (args.isEmpty())
                throw new CommandException("corner number required");
            String numStr = args.remove(0);
            int num;
            boolean pick = false;
            if ((! args.isEmpty()) && ("pick".startsWith(args.get(0).toLowerCase()))) {
                pick = true;
                args.remove(0);
            }
            try {
                num = Integer.parseInt(numStr);
            } catch (NumberFormatException nfe) {
                throw new CommandException("invalid corner number");
            }
            if ((num < 1) || (num > 2))
                throw new CommandException("number must be 1 or 2");
            Location loc;

            if (pick) {
                Block block = ctx.getPlayer().getTargetBlock(null, 1000);
                if ((block == null) || (block.getType() == Material.AIR))
                    throw new CommandException("no block found");
                loc = block.getLocation();
            } else
                loc = ctx.getPlayer().getLocation().getBlock().getLocation();
            LocalGateImpl gate = getGate(ctx, args);
            if (gate.getType() != GateType.AREA)
                throw new CommandException("this command is only valid for %s gates", GateType.AREA);
            Permissions.require(ctx.getPlayer(), "trp.gate.corner." + gate.getFullName());
            if (num == 1)
                ((LocalAreaGateImpl)gate).setP1Location(loc);
            else if (num == 2)
                ((LocalAreaGateImpl)gate).setP2Location(loc);
            return;
        }

        if ("create".startsWith(subCmd)) {
            if (! ctx.isPlayer())
                throw new CommandException("must be a player to use this command");
            ctx.getPlayer().performCommand("trp design create " + rebuildCommandArgs(args));
            return;
        }

        if ("set".startsWith(subCmd)) {
            if (args.isEmpty())
                throw new CommandException("option name required");
            String option = args.remove(0);
            if (args.isEmpty())
                throw new CommandException("option value required");
            String value = args.remove(0);
            LocalGateImpl gate = getGate(ctx, args);
            gate.setOption(ctx, option, value);
            return;
        }

        if ("get".startsWith(subCmd)) {
            if (args.isEmpty())
                throw new CommandException("option name required");
            String option = args.remove(0);
            LocalGateImpl gate = getGate(ctx, args);
            gate.getOptions(ctx, option);
            return;
        }

        throw new CommandException("do what with a gate?");
    }

    private LocalGateImpl getGate(Context ctx, List<String> args) throws CommandException {
        GateImpl gate;
        if (! args.isEmpty()) {
            gate = Gates.find(ctx, args.get(0));
            if ((gate == null) || (! (gate instanceof LocalGateImpl)))
                throw new CommandException("unknown gate '%s'", args.get(0));
            args.remove(0);
        } else
            gate = Gates.getSelectedGate(ctx.getPlayer());
        if (gate == null)
            throw new CommandException("gate name required");
        if (! gate.isSameServer())
            throw new CommandException("this command cannot be used on a remote endpoint");
        return (LocalGateImpl)gate;
    }

}
