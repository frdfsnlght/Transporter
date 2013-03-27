/*
 * Copyright 2013 frdfsnlght <frdfsnlght@gmail.com>.
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

import com.frdfsnlght.transporter.api.RemotePlayer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.mcsg.double0negative.tabapi.TabAPI;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class TabList {

    private static final Set<String> OPTIONS = new HashSet<String>();
    private static final Options options;

    static {
        OPTIONS.add("priority");
        OPTIONS.add("showServerList");
        OPTIONS.add("serverListHeader");
        OPTIONS.add("connectedServerFormat");
        OPTIONS.add("disconnectedServerFormat");
        OPTIONS.add("showPlayerList");
        OPTIONS.add("playerListHeader");

        options = new Options(TabList.class, OPTIONS, "trp.tablist", new OptionsListener() {
            @Override
            public void onOptionSet(Context ctx, String name, String value) {
                ctx.sendLog("tab list option '%s' set to '%s'", name, value);
            }
            @Override
            public String getOptionPermission(Context ctx, String name) {
                return name;
            }
        });

    }

    private static TabAPI tabAPIPlugin = null;


    // can be called from any thread
    public static void updateAll() {
        if (! tabAPIAvailable()) return;
        if (Utils.isMainThread())
            updateAllNow();
        else
            Utils.fire(new Runnable() {
                @Override
                public void run() {
                    updateAllNow();
                }
            });
    }

    // can be called from any thread
    public static void updatePlayer(final Player player) {
        if (! tabAPIAvailable()) return;
        if (Utils.isMainThread())
            updatePlayerNow(player);
        else
            Utils.fire(new Runnable() {
                @Override
                public void run() {
                    updatePlayerNow(player);
                }
            });
    }

    // called from main thread
    public static void startPlayer(final Player player) {
        if (! tabAPIAvailable()) return;
        Utils.fireDelayed(new Runnable() {
            @Override
            public void run() {
                TabAPI.setPriority(Global.plugin, player, getPriority());
                updateAllNow();
            }
        }, 500);
    }

    // called from main thread
    public static void stopPlayer(Player player) {
        if (! tabAPIAvailable()) return;
        TabAPI.setPriority(Global.plugin, player, -2);
        Utils.fireDelayed(new Runnable() {
            @Override
            public void run() {
                updateAllNow();
            }
        }, 500);
    }

    private static boolean tabAPIAvailable() {
        if (! Config.getUseTabAPI()) return false;
        Plugin p = Global.plugin.getServer().getPluginManager().getPlugin("TabAPI");
        if ((p == null) || (! p.isEnabled())) return false;
        if (tabAPIPlugin != null) return true;
        tabAPIPlugin = (TabAPI)p;
        Utils.info("Initialized TabAPI");
        return true;
    }

    private static void updateAllNow() {
        List<TabListCell> screen = generateScreen();
        for (Player player : Global.plugin.getServer().getOnlinePlayers())
            sendScreenToPlayer(screen, player);
    }

    private static void updatePlayerNow(Player player) {
        List<TabListCell> screen = generateScreen();
        sendScreenToPlayer(screen, player);
    }

    /* Begin options */

    public static int getPriority() {
        return Config.getIntDirect("tablist.priority", 0);
    }

    public static void setPriority(int i) {
        if (i < -1) i = -1;
        Config.setPropertyDirect("tablist.priority", i);
    }

    public static boolean getShowServerList() {
        return Config.getBooleanDirect("tablist.showServerList", true);
    }

    public static void setShowServerList(boolean b) {
        Config.setPropertyDirect("tablist.showServerList", b);
    }

    public static String getServerListHeader() {
        return Config.getStringDirect("tablist.serverListHeader", "%BLUE%%BOLD%----------|%BLUE%%BOLD%ServerList|%BLUE%%BOLD%----------");
    }

    public static void setServerListHeader(String s) {
        if (s != null) {
            if (s.equals("-")) s = "";
            else if (s.equals("*")) s = null;
        }
        Config.setPropertyDirect("tablist.serverListHeader", s);
    }

    public static String getConnectedServerFormat() {
        return Config.getStringDirect("tablist.connectedServerFormat", "%BLUE%%server%[%players%]");
    }

    public static void setConnectedServerFormat(String s) {
        if (s != null) {
            if (s.equals("-")) s = "";
            else if (s.equals("*")) s = null;
        }
        Config.setPropertyDirect("tablist.connectedServerFormat", s);
    }

    public static String getDisconnectedServerFormat() {
        return Config.getStringDirect("tablist.disconnectedServerFormat", "%GRAY%%server%");
    }

    public static void setDisconnectedServerFormat(String s) {
        if (s != null) {
            if (s.equals("-")) s = "";
            else if (s.equals("*")) s = null;
        }
        Config.setPropertyDirect("tablist.disconnectedServerFormat", s);
    }

    public static boolean getShowPlayerList() {
        return Config.getBooleanDirect("tablist.showPlayerList", true);
    }

    public static void setShowPlayerList(boolean b) {
        Config.setPropertyDirect("tablist.showPlayerList", b);
    }

    public static String getPlayerListHeader() {
        return Config.getStringDirect("tablist.playerListHeader", "%DARK_GREEN%%BOLD%----------|%GREEN%%BOLD%PlayerList|%DARK_GREEN%%BOLD%----------");
    }

    public static void setPlayerListHeader(String s) {
        if (s != null) {
            if (s.equals("-")) s = "";
            else if (s.equals("*")) s = null;
        }
        Config.setPropertyDirect("tablist.playerListHeader", s);
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



    private static List<TabListCell> generateScreen() {
        List<TabListCell> screen = new ArrayList<TabListCell>();
        int pos = 0;

		if (getShowServerList()) {
            String format = getServerListHeader();
            if (! format.isEmpty()) {
                pos = setCells(screen, pos, format, 9999);
                pos = newLine(pos, false);
            }

            pos = setServer(screen, pos, "Local", Global.plugin.getServer().getOnlinePlayers().length, true);
            for (Server server : Servers.getAll())
                pos = setServer(screen, pos, server.getName(), server.getRemotePlayers().size(), server.isConnected());
            pos = newLine(pos, false);
        }

		if (getShowPlayerList()) {
            String format = getPlayerListHeader();
            if (! format.isEmpty()) {
                pos = setCells(screen, pos, format, 9999);
                pos = newLine(pos, false);
            }

			for (Player p : Global.plugin.getServer().getOnlinePlayers())
                // TODO: get player's actual ping time if Bukkit ever implements it
                pos = setLocalPlayer(screen, pos, p.getPlayerListName(), 0);
            for (Server server : Servers.getAll()) {
                for (RemotePlayer remotePlayer : server.getRemotePlayers())
                    pos = setRemotePlayer(screen, pos, (RemotePlayerImpl)remotePlayer, server);
            }
		}
        // make each cell unique
        Map<String,Integer> cells = new HashMap<String,Integer>();
        for (TabListCell cell : screen) {
            if (cells.containsKey(cell.content))
                cell.content = cell.content + TabAPI.nextNull();
            cells.put(cell.content, 1);
            Utils.debug("tablist: %d,%d,%s", cell.x, cell.y, cell.content);
        }
        return screen;
	}

    private static void sendScreenToPlayer(List<TabListCell> screen, Player player) {
        for (TabListCell cell : screen) {
            if (cell.y < TabAPI.getVertSize())
                TabAPI.setTabString(Global.plugin, player, cell.y, cell.x, cell.content, cell.ping);
        }
		TabAPI.updatePlayer(player);
    }

    private static int setCells(List<TabListCell> screen, int pos, String format, int ping) {
        if (format == null) return pos;
        format = Chat.colorize(format);
        for (StringTokenizer lineTokens = new StringTokenizer(format, "$"); lineTokens.hasMoreTokens(); ) {
            String line = lineTokens.nextToken();
            for (StringTokenizer columnTokens = new StringTokenizer(line, "|"); columnTokens.hasMoreTokens(); ) {
                String column = columnTokens.nextToken();
                if (! column.isEmpty()) {
                    int x = pos % TabAPI.getHorizSize();
                    int y = pos / TabAPI.getHorizSize();
                    screen.add(new TabListCell(x, y, column, ping));
                }
                pos++;
            }
            if (lineTokens.hasMoreTokens()) pos = newLine(pos, true);
        }
        Utils.debug("pos is now %d", pos);
        return pos;
    }

    private static int newLine(int pos, boolean force) {
        boolean inLine = (pos % TabAPI.getHorizSize()) > 0;
        if (inLine)
            pos += (TabAPI.getHorizSize() - (pos % TabAPI.getHorizSize()));
        else if (force)
            pos += TabAPI.getHorizSize();
        return pos;
    }

    private static int setServer(List<TabListCell> screen, int pos, String name, int players, boolean connected) {
        String baseFormat;
        if (connected)
            baseFormat = getConnectedServerFormat();
        else
            baseFormat = getDisconnectedServerFormat();

        if (baseFormat.isEmpty()) return pos;

        String format;
        String n = name;
        for (;;) {
            format = baseFormat;
            format = format.replace("%server%", n);
            if (connected)
                format = format.replace("%players%", players + "");
            format = Chat.colorize(format);
            if (format.length() <= 13)
                return setCells(screen, pos, format, connected ? 0 : -1);
            n = n.substring(0, n.length() - 4) + "...";
        }
    }

    private static int setLocalPlayer(List<TabListCell> screen, int pos, String name, int ping) {
        return setCells(screen, pos, name, ping);
    }

    private static int setRemotePlayer(List<TabListCell> screen, int pos, RemotePlayerImpl remotePlayer, Server server) {
        String format = server.formatPlayerListName(remotePlayer);
        if (format.isEmpty()) return pos;
        return setCells(screen, pos, format, 0);
    }

    private static class TabListCell {
        int x, y;
        String content;
        int ping;
        TabListCell(int x, int y, String content, int ping) {
            this.x = x;
            this.y = y;
            this.content = content;
            this.ping = ping;
        }
    }

}
