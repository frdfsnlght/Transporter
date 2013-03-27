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
package com.frdfsnlght.transporter;

import com.frdfsnlght.transporter.api.API;
import com.frdfsnlght.transporter.api.TransporterException;
import com.frdfsnlght.transporter.command.CommandException;
import com.frdfsnlght.transporter.command.CommandProcessor;
import com.frdfsnlght.transporter.net.Network;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.PluginClassLoader;


/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public class Transporter extends JavaPlugin {

    private ServerListenerImpl serverListener;
    private BlockListenerImpl blockListener;
    private PlayerListenerImpl playerListener;
    private VehicleListenerImpl vehicleListener;
    private WorldListenerImpl worldListener;
    private EntityListenerImpl entityListener;

    private API api = null;

    @Override
    public void onEnable() {
        Global.mainThread = Thread.currentThread();
        PluginDescriptionFile pdf = getDescription();
        Global.plugin = this;
        Global.pluginName = pdf.getName();
        Global.pluginVersion = pdf.getVersion();
        Global.started = false;

        /*
        if (! Compatibility.setup()) {
            Utils.severe("unable to find compatible server version, plugin is disabled");
            setEnabled(false);
            return;
        }
        */
        
        Global.enabled = true;

        final Context ctx = new Context();

        //ctx.sendLog("this is v%s", Global.pluginVersion);

        // install/update resources

        File dataFolder = Global.plugin.getDataFolder();
        if (! dataFolder.exists()) {
            ctx.sendLog("creating data folder");
            dataFolder.mkdirs();
        }
        Utils.copyFileFromJar("/resources/LICENSE.txt", dataFolder, true);
        Utils.copyFileFromJar("/resources/README.txt", dataFolder, true);
        Utils.copyFileFromJar("/resources/materials.txt", dataFolder, true);

        if (Utils.copyFileFromJar("/resources/config.yml", dataFolder, false))
            ctx.sendLog("installed default configuration");
        if (Utils.copyFileFromJar("/resources/permissions.properties", dataFolder, false))
            ctx.sendLog("installed default basic permissions");

        File designsFolder = new File(dataFolder, "designs");
        if (Utils.copyFilesFromJar("/resources/designs/manifest", designsFolder, false))
            ctx.sendLog("installed default designs");

        File overviewerFolder = new File(dataFolder, "overviewer");
        Utils.copyFilesFromJar("/resources/overviewer/manifest", overviewerFolder, true);
        Utils.copyFileFromJar("/resources/overviewer/transporterConfig.js", overviewerFolder, false);

        // Add all jars in the data folder to our class loader
        PluginClassLoader classLoader = (PluginClassLoader)getClass().getClassLoader();
        for (String fileName : dataFolder.list()) {
            if (fileName.toLowerCase().endsWith(".jar"))
                try {
                    classLoader.addURL((new File(dataFolder, fileName)).toURI().toURL());
                } catch (MalformedURLException mue) {}
        }

        Config.load(ctx);

        if (Config.getCheckVersion())
            Utils.checkVersion();

        serverListener = new ServerListenerImpl();
        blockListener = new BlockListenerImpl();
        playerListener = new PlayerListenerImpl();
        vehicleListener = new VehicleListenerImpl();
        worldListener = new WorldListenerImpl();
        entityListener = new EntityListenerImpl();

        Designs.load(ctx);
        Network.start(ctx);
        Realm.start(ctx);

        PluginManager pm = getServer().getPluginManager();

        pm.registerEvents(serverListener, this);
        pm.registerEvents(blockListener, this);
        pm.registerEvents(playerListener, this);
        pm.registerEvents(vehicleListener, this);
        pm.registerEvents(worldListener, this);
        pm.registerEvents(entityListener, this);

        Runnable loadWorlds = new Runnable() {
            @Override
            public void run() {
                Worlds.autoLoad(ctx);
                Markers.update();
            }
        };

        if (Config.getWorldLoadDelay() == 0)
            loadWorlds.run();
        else
            // Setup delayed start tasks
            // It would be better if bukkit someday offered an event that indicated
            // all the plugins were done loading and the server was started
            Utils.fireDelayed(loadWorlds, Config.getWorldLoadDelay());

        Global.started = true;

        if (Config.getUpdateMetrics())
            try {
                Metrics metrics = new Metrics(this);
                metrics.start();
            } catch (IOException e) {
                ctx.warn("unable to start metrics: %s", e.getMessage());
            }

        ctx.sendLog("ready on server '%s'", getServer().getServerName());

    }

    @Override
    public void onDisable() {
        if (! Global.enabled) return;
        Global.enabled = false;
        Context ctx = new Context();
        Realm.stop(ctx);
        Network.stop(ctx);
        Config.save(ctx);
        Gates.save(ctx);
        ctx.sendLog("disabled");
        Global.plugin = null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] rawArgs) {
        // Rebuild quoted arguments
        List<String> args = new ArrayList<String>();
        boolean inQuotes = false;
        StringBuilder argBuffer = null;
        for (String arg : rawArgs) {
            if (arg.isEmpty()) continue;
            if (inQuotes) {
                argBuffer.append(" ");
                argBuffer.append(arg);
                if (arg.endsWith("\"")) {
                    argBuffer.deleteCharAt(argBuffer.length() - 1);
                    inQuotes = false;
                    args.add(argBuffer.toString());
                    argBuffer = null;
                }
            } else if (arg.startsWith("\"")) {
                argBuffer = new StringBuilder(arg);
                argBuffer.deleteCharAt(0);
                if ((arg.length() > 1) && arg.endsWith("\"")) {
                    argBuffer.deleteCharAt(argBuffer.length() - 1);
                    args.add(argBuffer.toString());
                    argBuffer = null;
                } else
                    inQuotes = true;
            } else
                args.add(arg);
        }
        if (argBuffer != null)
            args.add(argBuffer.toString());

        Context ctx = new Context(sender);
        if (args.isEmpty()) {
            ctx.send("this is v%s", Global.pluginVersion);
            return true;
        }

        // Find the matching commands
        List<CommandProcessor> cps = new ArrayList<CommandProcessor>();
        for (CommandProcessor cp : Global.commands) {
            if (! cp.matches(ctx, cmd, args)) continue;
            cps.add(cp);
        }
        // Execute the matching command
        try {
            if (cps.isEmpty())
                throw new CommandException("huh? try %strp help", (ctx.isPlayer() ? "/" : ""));
            if (cps.size() > 1)
                throw new CommandException("ambiguous command; try %strp help", (ctx.isPlayer() ? "/" : ""));
            cps.get(0).process(ctx, cmd, args);
            return true;
        } catch (TransporterException te) {
            ctx.warn(te.getMessage());
            return true;
        }
    }

    public API getAPI() {
        if (api == null)
            api = new API();
        return api;
    }

}
