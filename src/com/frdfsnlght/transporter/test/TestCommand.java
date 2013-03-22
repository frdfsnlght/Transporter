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
package com.frdfsnlght.transporter.test;

import java.util.ArrayList;
import java.util.List;
import com.frdfsnlght.transporter.Context;
import com.frdfsnlght.transporter.Global;
import com.frdfsnlght.transporter.api.API;
import com.frdfsnlght.transporter.api.Callback;
import com.frdfsnlght.transporter.api.RemoteException;
import com.frdfsnlght.transporter.api.RemotePlayer;
import com.frdfsnlght.transporter.api.RemoteServer;
import com.frdfsnlght.transporter.api.RemoteWorld;
import com.frdfsnlght.transporter.api.TransporterException;
import com.frdfsnlght.transporter.command.CommandException;
import com.frdfsnlght.transporter.command.TrpCommandProcessor;
import org.bukkit.command.Command;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public class TestCommand extends TrpCommandProcessor {

    private static final String GROUP = "test ";

    @Override
    public boolean matches(Context ctx, Command cmd, List<String> args) {
        return super.matches(ctx, cmd, args) &&
               GROUP.startsWith(args.get(0).toLowerCase());
    }

    @Override
    public List<String> getUsage(Context ctx) {
        List<String> cmds = new ArrayList<String>();
        cmds.add(getPrefix(ctx) + GROUP + "die");
        cmds.add(getPrefix(ctx) + GROUP + "potion");
        cmds.add(getPrefix(ctx) + GROUP + "api");
        return cmds;
    }

    @Override
    public void process(final Context ctx, Command cmd, List<String> args)  throws TransporterException {
        args.remove(0);
        if (args.isEmpty())
            throw new CommandException("test what?");
        String subCmd = args.remove(0).toLowerCase();

        if ("die".startsWith(subCmd)) {
            if (! ctx.isPlayer())
                throw new CommandException("this command is only available to players");
            ctx.getPlayer().damage(ctx.getPlayer().getHealth());
            return;
        }

        if ("potion".startsWith(subCmd)) {
            if (! ctx.isPlayer())
                throw new CommandException("this command is only available to players");
            if (args.isEmpty())
                throw new CommandException("type required");
            String typeStr = args.remove(0);
            if ("clear".startsWith(typeStr)) {
                for (PotionEffectType pet : PotionEffectType.values()) {
                    if (pet == null) continue;
                    if (ctx.getPlayer().hasPotionEffect(pet)) {
                        ctx.getPlayer().removePotionEffect(pet);
                        ctx.send("removed %s", pet.getName());
                    }
                }
                return;
            }
            if ("list".startsWith(typeStr)) {
                for (PotionEffectType pet : PotionEffectType.values()) {
                    if (pet == null) continue;
                    if (ctx.getPlayer().hasPotionEffect(pet))
                        ctx.send("has %s", pet.getName());
                }
                return;
            }
            PotionEffectType type = PotionEffectType.getByName(typeStr);
            int duration = 3000;
            int amplifier = 1;
            if (! args.isEmpty())
                duration = Integer.parseInt(args.remove(0));
            if (! args.isEmpty())
                amplifier = Integer.parseInt(args.remove(0));
            PotionEffect pe = type.createEffect(duration, amplifier);
            ctx.getPlayer().addPotionEffect(pe, true);
            ctx.send("added potion %s %s %s", type.getName(), duration, amplifier);
            return;
        }

        if ("api".startsWith(subCmd)) {
            API api = Global.plugin.getAPI();
            for (RemoteServer server : api.getRemoteServers()) {
                final RemoteServer s = server;
                ctx.send("requesting version from %s", server.getName());
                server.getVersion(new Callback<String>() {
                    @Override
                    public void onSuccess(String version) {
                        ctx.send("%s version: %s", s.getName(), version);
                    }
                    @Override
                    public void onFailure(RemoteException re) {
                        ctx.send("%s failed: %s", s.getName(), re.getMessage());
                    }
                });
                for (RemoteWorld world : server.getRemoteWorlds()) {
                    ctx.send("requesting time from %s.%s", server.getName(), world.getName());
                    final RemoteWorld w = world;
                    world.getTime(new Callback<Long>() {
                        @Override
                        public void onSuccess(Long time) {
                            ctx.send("%s.%s time: %s", s.getName(), w.getName(), time);
                        }
                        @Override
                        public void onFailure(RemoteException re) {
                            ctx.send("%s.%s failed: %s", s.getName(), w.getName(), re.getMessage());
                        }
                    });
                }
                for (RemotePlayer player : server.getRemotePlayers()) {
                    ctx.send("sending message to %s.%s", server.getName(), player.getName());
                    final RemotePlayer p = player;
                    player.sendMessage(null, "hello there");

                }
            }
            return;
        }

        throw new CommandException("test what?");
    }

}
