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

import com.nijiko.permissions.PermissionHandler;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;

/**
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class Permissions {

//    private static final String OPS_FILE = "ops.txt";
//    private static final String BANNEDPLAYERS_FILE = "banned-players.txt";
//    private static final String WHITELIST_FILE = "white-list.txt";
//    private static final String SERVERPROPERTIES_FILE = "server.properties";
    private static final String PERMISSIONS_FILE = "permissions.properties";

    private static final File permissionsFile =
            new File(Global.plugin.getDataFolder(), PERMISSIONS_FILE);

//    private static Map<String,ListFile> listFiles = new HashMap<String,ListFile>();
    private static Map<String,PropertiesFile> propertiesFiles = new HashMap<String,PropertiesFile>();

    private static boolean basicPermsInitted = false;
    private static net.milkbowl.vault.permission.Permission vaultPlugin = null;
    private static PermissionHandler permissionsPlugin = null;
    private static PermissionManager permissionsExPlugin = null;

    public static boolean basicPermsAvailable() {
        if (basicPermsInitted) return true;
        basicPermsInitted = true;
        Utils.info("Initialized Basic for Permissions");
        return true;
    }

    public static boolean vaultAvailable() {
        if (! Config.getUseVaultPermissions()) return false;
        Plugin p = Global.plugin.getServer().getPluginManager().getPlugin("Vault");
        if (p == null) {
            Utils.warning("Vault is not installed!");
            return false;
        }
        if (! p.isEnabled()) {
            Utils.warning("Vault is not enabled!");
            return false;
        }
        if (vaultPlugin != null) return true;
        RegisteredServiceProvider<net.milkbowl.vault.permission.Permission> rsp =
                Global.plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (rsp == null) {
            Utils.warning("Vault didn't return a service provider!");
            return false;
        }
        vaultPlugin = rsp.getProvider();
        if (vaultPlugin == null) {
            Utils.warning("Vault didn't return a permissions provider!");
            return false;
        }
        Utils.info("Initialized Vault for Permissions");
        return true;
    }

    public static boolean permissionsAvailable() {
        if (! Config.getUsePermissions()) return false;
        Plugin p = Global.plugin.getServer().getPluginManager().getPlugin("Permissions");
        if (p == null) {
            Utils.warning("Permissions is not installed!");
            return false;
        }
        if (! p.isEnabled()) {
            Utils.warning("Permissions is not enabled!");
            return false;
        }
        if (permissionsPlugin != null) return true;
        permissionsPlugin = ((com.nijikokun.bukkit.Permissions.Permissions)p).getHandler();
        if (permissionsPlugin == null) {
            Utils.warning("Permissions didn't return a handler!");
            return false;
        }
        Utils.info("Initialized Permissions for Permissions");
        return true;
    }

    public static boolean permissionsExAvailable() {
        if (! Config.getUsePermissionsEx()) return false;
        Plugin p = Global.plugin.getServer().getPluginManager().getPlugin("PermissionsEx");
        if (p == null) {
            Utils.warning("PermissionsEx is not installed!");
            return false;
        }
        if (! p.isEnabled()) {
            Utils.warning("PermissionsEx is not enabled!");
            return false;
        }
        if (permissionsExPlugin != null) return true;
        permissionsExPlugin = PermissionsEx.getPermissionManager();
        if (permissionsExPlugin == null) {
            Utils.warning("PermissionsEx didn't return a manager!");
            return false;
        }
        Utils.info("Initialized PermissionsEx for Permissions");
        return true;
    }

    public static boolean hasBasic(Player player, String perm) {
        return hasBasic(player.getName(), perm);
    }

    public static boolean hasBasic(String name, String perm) {
        Properties permissions = getProperties(permissionsFile);
        Utils.debug("basic permissions check '%s' for %s", perm, name);
        for (;;) {
            String prop = permissions.getProperty(perm);
            if (prop != null)
                Utils.debug("found basic permissions node for '%s': %s", perm, prop);
            else {
                prop = permissions.getProperty(perm + ".*");
                if (prop != null)
                    Utils.debug("found basic permissions node for '%s.*': %s", perm, prop);
            }
            if (prop != null) {
                String[] players = prop.split("\\s*,\\s*");
                boolean grant = false;
                boolean found = false;
                for (String player : players) {
                    if (player.equals("*") || player.equals("+*")) {
                        grant = true;
                        found = true;
                    } else if (player.equals("-*")) {
                        grant = false;
                        found = true;
                    } else if (player.equals(name) || player.equals("+" + name)) {
                        grant = true;
                        found = true;
                    } else if (player.equals("-" + name)) {
                        grant = false;
                        found = true;
                    }
                }
                if (found) {
                    Utils.debug("basic permission %s granted", grant ? "is" : "is not");
                    return grant;
                }
            } else
                Utils.debug("basic permissions node '%s' not found", perm);
            int pos = perm.lastIndexOf(".");
            if (pos == -1) {
                Utils.debug("basic permission is not granted");
                return false;
            }
            perm = perm.substring(0, pos);
        }
    }

    public static boolean has(Player player, String perm) {
        if (player == null) return true;
        try {
            require(player.getWorld().getName(), player.getName(), true, perm);
            return true;
        } catch (PermissionsException e) {
            return false;
        }
    }

    public static void require(Player player, String perm) throws PermissionsException {
        if (player == null) return;
        require(player.getWorld().getName(), player.getName(), true, perm);
    }

    public static void require(Player player, boolean requireAll, String ... perms) throws PermissionsException {
        if (player == null) return;
        require(player.getWorld().getName(), player.getName(), requireAll, perms);
    }

    public static void require(String worldName, String playerName, String perm) throws PermissionsException {
        require(worldName, playerName, true, perm);
    }

    private static void require(String worldName, String playerName, boolean requireAll, String ... perms) throws PermissionsException {
        if (isOp(playerName)) {
            Utils.debug("player '%s' is op", playerName);
            return;
        }

        if (vaultAvailable()) {
            for (String perm : perms) {
                if (requireAll) {
                    if (! vaultPlugin.has(worldName, playerName, perm))
                        throw new PermissionsException("not permitted");
                } else {
                    if (vaultPlugin.has(worldName, playerName, perm)) return;
                }
            }
            if ((! requireAll) && (perms.length > 0))
                throw new PermissionsException("not permitted");
            return;
        }

        if (permissionsAvailable()) {
            for (String perm : perms) {
                if (requireAll) {
                    if (! permissionsPlugin.permission(worldName, playerName, perm))
                        throw new PermissionsException("not permitted");
                } else {
                    if (permissionsPlugin.permission(worldName, playerName, perm)) return;
                }
            }
            if ((! requireAll) && (perms.length > 0))
                throw new PermissionsException("not permitted");
            return;
        }

        if (permissionsExAvailable()) {
            for (String perm : perms) {
                if (requireAll) {
                    if (! permissionsExPlugin.has(playerName, perm, worldName))
                        throw new PermissionsException("not permitted");
                } else {
                    if (permissionsExPlugin.has(playerName, perm, worldName)) return;
                }
            }
            if ((! requireAll) && (perms.length > 0))
                throw new PermissionsException("not permitted");
            return;
        }

        if (basicPermsAvailable()) {
            for (String perm : perms) {
                if (requireAll) {
                    if (! hasBasic(playerName, perm))
                        throw new PermissionsException("not permitted");
                } else {
                    if (hasBasic(playerName, perm)) return;
                }
            }
            if ((! requireAll) && (perms.length > 0))
                throw new PermissionsException("not permitted");
            return;
        }

        // should never get here!
        throw new PermissionsException("not permitted because no permissions system is available?");

    }

    // can't check player's IP because it might not be what it is on the sending side due to NAT
    public static void connect(String playerName) throws PermissionsException {
        if (Global.plugin.getServer().getOnlinePlayers().length >= Global.plugin.getServer().getMaxPlayers())
            throw new PermissionsException("maximim players already connected");
        for (OfflinePlayer p : Global.plugin.getServer().getWhitelistedPlayers())
            if (p.getName().equalsIgnoreCase(playerName)) return;
        for (OfflinePlayer p : Global.plugin.getServer().getBannedPlayers())
            if (p.getName().equalsIgnoreCase(playerName))
                throw new PermissionsException("player is banned");
        /*
        if (getProperties(new File(SERVERPROPERTIES_FILE)).getProperty("white-list", "false").equalsIgnoreCase("true"))
            if (! getList(new File(WHITELIST_FILE), true).contains(playerName.toLowerCase()))
                throw new PermissionsException("player is not white-listed");
        if (getList(new File(BANNEDPLAYERS_FILE), false).contains(playerName))
            throw new PermissionsException("player is banned");
         */
    }

    public static boolean isOp(Player player) {
        if (player == null) return true;
        return player.isOp();
//        return isOp(player.getName());
    }

    public static boolean isOp(String playerName) {
        Set<OfflinePlayer> ops = Global.plugin.getServer().getOperators();
        for (OfflinePlayer p : Global.plugin.getServer().getOperators())
            if (p.getName().equalsIgnoreCase(playerName)) return true;
        return false;
//        return getList(new File(OPS_FILE), true).contains(playerName);
    }

    /*
    private static Set<String> getList(File file, boolean forceLower) {
        ListFile listFile = listFiles.get(file.getAbsolutePath());
        if (listFile == null) {
            listFile = new ListFile();
            listFiles.put(file.getAbsolutePath(), listFile);
        }
        if ((listFile.data == null) || (listFile.lastRead < file.lastModified())) {
            listFile.data = new HashSet<String>();
            try {
                BufferedReader r = new BufferedReader(new FileReader(file));
                String line;
                while ((line = r.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    if (forceLower) line = line.toLowerCase();
                    listFile.data.add(line);
                }
                r.close();
                listFile.lastRead = System.currentTimeMillis();
            } catch (IOException ioe) {
                Utils.warning("unable to read %s: %s", file.getAbsolutePath(), ioe.getMessage());
            }
        }
        return listFile.data;
    }
     */

    private static Properties getProperties(File file) {
        PropertiesFile propsFile = propertiesFiles.get(file.getAbsolutePath());
        if (propsFile == null) {
            propsFile = new PropertiesFile();
            propertiesFiles.put(file.getAbsolutePath(), propsFile);
        }
        if ((propsFile.data == null) || (propsFile.lastRead < file.lastModified())) {
            propsFile.data = new Properties();
            try {
                propsFile.data.load(new FileInputStream(file));
                propsFile.lastRead = System.currentTimeMillis();
            } catch (IOException ioe) {
                Utils.warning("unable to read %s: %s", file.getAbsolutePath(), ioe.getMessage());
            }
        }
        return propsFile.data;
    }

    /*
    private static class ListFile {
        Set<String> data = null;
        long lastRead = 0;
    }
*/

    private static class PropertiesFile {
        Properties data = null;
        long lastRead = 0;
    }

}
