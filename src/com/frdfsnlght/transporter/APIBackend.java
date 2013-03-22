/*
 * Copyright 2012 frdfsnlght <frdfsnlght@gmail.com>.
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
package com.frdfsnlght.transporter;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import com.frdfsnlght.transporter.api.RemoteException;
import com.frdfsnlght.transporter.api.TransporterException;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class APIBackend {

    private static final Set<String> OPTIONS = new HashSet<String>();
    private static final Options options;

    static {
        OPTIONS.add("debug");
        OPTIONS.add("timeout");

        options = new Options(APIBackend.class, OPTIONS, "trp.api", new OptionsListener() {
            @Override
            public void onOptionSet(Context ctx, String name, String value) {
                ctx.sendLog("API option '%s' set to '%s'", name, value);
            }
            @Override
            public String getOptionPermission(Context ctx, String name) {
                return name;
            }
        });
    }

    public static void onConfigLoad(Context ctx) {}

    public static void onConfigSave() {}

    /* Begin options */

    public static boolean getDebug() {
        return Config.getBooleanDirect("api.debug", false);
    }

    public static void setDebug(boolean b) {
        Config.setPropertyDirect("api.debug", b);
    }

    public static int getTimeout() {
        return Config.getIntDirect("api.timeout", 5000);
    }

    public static void setTimeout(int i) {
        if (i < 1000)
            throw new IllegalArgumentException("timeout must be at least 1000");
        Config.setPropertyDirect("api.timeout", i);
    }


    public static void getOptions(Context ctx, String name) throws OptionsException, PermissionsException {
        options.getOptions(ctx, name);
    }

    public static String getOption(Context ctx, String name) throws OptionsException, PermissionsException {
        return options.getOption(ctx, name);
    }

    public static void setOption(Context ctx, String name, String value) throws OptionsException, PermissionsException {
        options.setOption(ctx, name, value);
    }

    /* End options */

    public static void debug(String msg, Object ... args) {
        if (! getDebug()) return;
        if (args.length > 0)
            msg = String.format(msg, args);
        msg = ChatColor.stripColor(msg);
        if (msg.isEmpty()) return;
        Utils.logger.log(Level.INFO, String.format("[%s] (API-DEBUG) %s", Global.pluginName, msg));
    }

    public static void invoke(String target, String method, TypeMap args, TypeMap out) throws TransporterException {
        debug("invoke %s.%s: %s", target, method, args);
        if (target.equals("server"))
            invokeServerMethod(method, args, out);
        else if (target.equals("world"))
            invokeWorldMethod(method, args, out);
        else if (target.equals("player"))
            invokePlayerMethod(method, args, out);

        else
            throw new RemoteException("unknown API target '%s'", target);
    }

    private static void invokeServerMethod(String method, TypeMap args, TypeMap out) throws TransporterException {
        org.bukkit.Server server = Global.plugin.getServer();
        if (method.equals("broadcast"))
            out.put("result", server.broadcast(args.getString("message"), args.getString("permission")));
        else if (method.equals("broadcastMessage"))
            out.put("result", server.broadcastMessage(args.getString("message")));
        else if (method.equals("dispatchCommand")) {
            String senderStr = args.getString("sender");
            CommandSender sender = null;
            if ("console".equals(senderStr))
                sender = server.getConsoleSender();
            else if ("player".equals(senderStr)) {
                sender = server.getPlayer(args.getString("name"));
                if (sender == null)
                    sender = server.getConsoleSender();
            }
            out.put("result", server.dispatchCommand(sender, args.getString("commandLine")));
        } else if (method.equals("getDefaultGameMode"))
            out.put("result", server.getDefaultGameMode().toString());
        else if (method.equals("getName"))
            out.put("result", server.getName());
        else if (method.equals("getServerId"))
            out.put("result", server.getServerId());
        else if (method.equals("getVersion"))
            out.put("result", server.getVersion());
        else
            throw new RemoteException("unknown server method '%s'", method);
    }

    private static void invokeWorldMethod(String method, TypeMap args, TypeMap out) throws TransporterException {
        String worldName = args.getString("world");
        if (worldName == null)
            throw new RemoteException("world is required");
        World world = Global.plugin.getServer().getWorld(worldName);
        if (world == null)
            throw new RemoteException("world '%s' is unknown", worldName);

        if (method.equals("getDifficulty"))
            out.put("result", world.getDifficulty().toString());
        else if (method.equals("getEnvironment"))
            out.put("result", world.getEnvironment().toString());
        else if (method.equals("getFullTime"))
            out.put("result", world.getFullTime());
        else if (method.equals("getSeed"))
            out.put("result", world.getSeed());
        else if (method.equals("getTime"))
            out.put("result", world.getTime());
        else
            throw new RemoteException("unknown world method '%s'", method);
    }

    private static void invokePlayerMethod(String method, TypeMap args, TypeMap out) throws TransporterException {
        String playerName = args.getString("player");
        if (playerName == null)
            throw new RemoteException("player is required");
        Player player = Global.plugin.getServer().getPlayer(playerName);
        if (player == null)
            throw new ServerException("player '%s' is unknown", playerName);

        if (method.equals("getLocation")) {
            TypeMap locMsg = new TypeMap();
            Location loc = player.getLocation();
            locMsg.put("world", loc.getWorld().getName());
            locMsg.put("x", loc.getX());
            locMsg.put("y", loc.getY());
            locMsg.put("z", loc.getZ());
            out.put("result", locMsg);
        } else if (method.equals("sendMessage")) {
            player.sendMessage(args.getString("message"));
        } else if (method.equals("sendRawMessage")) {
            player.sendRawMessage(args.getString("message"));
        } else
            throw new ServerException("unknown player method '%s'", method);
    }

}
