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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.scheduler.BukkitWorker;
import org.bukkit.util.Vector;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public class Utils {

    public static final File BukkitBaseFolder = new File(".");

    public static final Logger logger = Logger.getLogger("Minecraft");

    private static final String DEBUG_BOUNDARY = "*****";
    private static final int DEBUG_LOG_BYTES = 20 * 1024;

    private static Pattern tokenPattern = Pattern.compile("%(\\w+)%");

    public static void info(String msg, Object ... args) {
        if (args.length > 0)
            msg = String.format(msg, args);
        msg = ChatColor.stripColor(msg);
        if (msg.isEmpty()) return;
        logger.log(Level.INFO, String.format("[%s] %s", Global.pluginName, msg));
    }

    public static void warning(String msg, Object ... args) {
        if (args.length > 0)
            msg = String.format(msg, args);
        msg = ChatColor.stripColor(msg);
        if (msg.isEmpty()) return;
        logger.log(Level.WARNING, String.format("[%s] %s", Global.pluginName, msg));
    }

    public static void severe(String msg, Object ... args) {
        if (args.length > 0)
            msg = String.format(msg, args);
        msg = ChatColor.stripColor(msg);
        if (msg.isEmpty()) return;
        logger.log(Level.SEVERE, String.format("[%s] %s", Global.pluginName, msg));
    }

    public static void severe(Throwable t, String msg, Object ... args) {
        if (args.length > 0)
            msg = String.format(msg, args);
        msg = ChatColor.stripColor(msg);
        if (msg.isEmpty()) return;
        logger.log(Level.SEVERE, String.format("[%s] %s", Global.pluginName, msg), t);
    }

    public static void debug(String msg, Object ... args) {
        if (! Config.getDebug()) return;
        if (args.length > 0)
            msg = String.format(msg, args);
        msg = ChatColor.stripColor(msg);
        if (msg.isEmpty()) return;
        logger.log(Level.INFO, String.format("[%s] (DEBUG) %s", Global.pluginName, msg));
    }

    public static String expandFormat(String format, Map<String,String> tokens) {
        if (format == null) return null;
        Matcher matcher = tokenPattern.matcher(format);
        StringBuffer b = new StringBuffer();
        while (matcher.find()) {
            String name = matcher.group(1);
            if (tokens.containsKey(name))
                matcher.appendReplacement(b, tokens.get(name));
            else
                matcher.appendReplacement(b, matcher.group());
        }
        matcher.appendTail(b);
        return b.toString();
    }

    public static String block(Block b) {
        return String.format("Block[%s,%d]", b.getType(), b.getData());
    }

    public static String blockCoords(Location loc) {
        return String.format("%s@%d,%d,%d", loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public static String coords(Location loc) {
        return String.format("%4.2f,%4.2f,%4.2f", (float)loc.getX(), (float)loc.getY(), (float)loc.getZ());
    }

    public static String dir(Vector vec) {
        return String.format("%4.2f,%4.2f,%4.2f", (float)vec.getX(), (float)vec.getY(), (float)vec.getZ());
    }

    public static String itemStackArray(ItemStack[] isa) {
        if (isa == null) return "no items";
        StringBuilder sb = new StringBuilder();
        sb.append(isa.length).append(" items\n");
        for (int i = 0; i < isa.length; i++)
            sb.append(itemStack(isa[i])).append("\n");
        return sb.toString();
    }

    public static String itemStack(ItemStack is) {
        if (is == null) return "null";
        return is.getAmount() + " of " + is.getType().toString();
    }

    public static boolean copyFileFromJar(String resPath, File dstFile, boolean overwriteIfOlder) {
        if (dstFile.isDirectory()) {
            int pos = resPath.lastIndexOf('/');
            if (pos != -1)
                dstFile = new File(dstFile, resPath.substring(pos + 1));
            else
                dstFile = new File(dstFile, resPath);
        }
        if (dstFile.exists()) {
            if (! overwriteIfOlder) return false;
            try {
                File jarFile = new File(Utils.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
                if (jarFile.lastModified() <= dstFile.lastModified()) return false;
            } catch (URISyntaxException e) {}
        }
        File parentDir = dstFile.getParentFile();
        if (! parentDir.exists())
            parentDir.mkdirs();
        InputStream is = Utils.class.getResourceAsStream(resPath);
        try {
            OutputStream os = new FileOutputStream(dstFile);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) > 0)
                os.write(buffer, 0, len);
            os.close();
            is.close();
        } catch (IOException ioe) {}
        return true;
    }

    public static boolean copyFilesFromJar(String manifestPath, File dstFolder, boolean overwriteIfOlder) {
        boolean created = false;
        if (! dstFolder.exists()) {
            dstFolder.mkdirs();
            created = true;
        }
        BufferedReader r = new BufferedReader(new InputStreamReader(Utils.class.getResourceAsStream(manifestPath)));
        String line;
        try {
            while ((line = r.readLine()) != null) {
                line = line.replaceAll("^\\s+|\\s+$|\\s*#.*", "");
                if (line.length() == 0) continue;
                if (line.endsWith("/")) {
                    // recursive copy of directory
                    int pos = line.lastIndexOf("/", line.length() - 2);
                    String subFolderName;
                    if (pos == -1)
                        subFolderName = line.substring(0, line.length() - 1);
                    else
                        subFolderName = line.substring(pos + 1, line.length() - 1);
                    line += "manifest";
                    File subFolder = new File(dstFolder, subFolderName);
                    copyFilesFromJar(line, subFolder, overwriteIfOlder);
                } else
                    copyFileFromJar(line, dstFolder, overwriteIfOlder);
            }
        } catch (IOException ioe) {}
        return created;
    }

    public static File[] listYAMLFiles(File folder) {
        return folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".yml");
            }
        });
    }

    public static float directionToYaw(BlockFace direction) {
        if (direction == null) return 0;
        switch (direction) {
            case SOUTH: return 0;
            case SOUTH_SOUTH_WEST: return 22.5F;
            case SOUTH_WEST: return 45;
            case WEST_SOUTH_WEST: return 67.5F;
            case WEST: return 90;
            case WEST_NORTH_WEST: return 112.5F;
            case NORTH_WEST: return 135;
            case NORTH_NORTH_WEST: return 157.5F;
            case NORTH: return 180;
            case NORTH_NORTH_EAST: return -157.5F;
            case NORTH_EAST: return -135;
            case EAST_NORTH_EAST: return -112.5F;
            case EAST: return -90;
            case EAST_SOUTH_EAST: return -67.5F;
            case SOUTH_EAST: return -45;
            case SOUTH_SOUTH_EAST: return -22.5F;

//            case WEST: return 0;
//            case WEST_NORTH_WEST: return 22.5F;
//            case NORTH_WEST: return 45;
//            case NORTH_NORTH_WEST: return 67.5F;
//            case NORTH: return 90;
//            case NORTH_NORTH_EAST: return 112.5F;
//            case NORTH_EAST: return 135;
//            case EAST_NORTH_EAST: return 157.5F;
//            case EAST: return 180;
//
//            case WEST_SOUTH_WEST: return -22.5F;
//            case SOUTH_WEST: return -45;
//            case SOUTH_SOUTH_WEST: return -67.5F;
//            case SOUTH: return -90;
//            case SOUTH_SOUTH_EAST: return -112.5F;
//            case SOUTH_EAST: return -135;
//            case EAST_SOUTH_EAST: return -157.5F;
            default: return 0;
        }
    }

    public static BlockFace yawToDirection(float yaw) {
        while (yaw > 180) yaw -= 360;
        while (yaw <= -180) yaw += 360;

        if (yaw < -168.75) return BlockFace.NORTH;
        if (yaw < -146.25) return BlockFace.NORTH_NORTH_EAST;
        if (yaw < -123.75) return BlockFace.NORTH_EAST;
        if (yaw < -101.25) return BlockFace.EAST_NORTH_EAST;
        if (yaw < -78.75) return BlockFace.EAST;
        if (yaw < -56.25) return BlockFace.EAST_SOUTH_EAST;
        if (yaw < -33.75) return BlockFace.SOUTH_EAST;
        if (yaw < -11.25) return BlockFace.SOUTH_SOUTH_EAST;

        if (yaw < 11.25) return BlockFace.SOUTH;
        if (yaw < 33.75) return BlockFace.SOUTH_SOUTH_WEST;
        if (yaw < 56.25) return BlockFace.SOUTH_WEST;
        if (yaw < 78.75) return BlockFace.WEST_SOUTH_WEST;
        if (yaw < 101.25) return BlockFace.WEST;
        if (yaw < 123.75) return BlockFace.WEST_NORTH_WEST;
        if (yaw < 146.25) return BlockFace.NORTH_WEST;
        if (yaw < 168.75) return BlockFace.NORTH_NORTH_WEST;

//        if (yaw < -168.75) return BlockFace.EAST;
//        if (yaw < -146.25) return BlockFace.EAST_SOUTH_EAST;
//        if (yaw < -123.75) return BlockFace.SOUTH_EAST;
//        if (yaw < -101.25) return BlockFace.SOUTH_SOUTH_EAST;
//        if (yaw < -78.75) return BlockFace.SOUTH;
//        if (yaw < -56.25) return BlockFace.SOUTH_SOUTH_WEST;
//        if (yaw < -33.75) return BlockFace.SOUTH_WEST;
//        if (yaw < -11.25) return BlockFace.WEST_SOUTH_WEST;
//
//        if (yaw < 11.25) return BlockFace.WEST;
//        if (yaw < 33.75) return BlockFace.WEST_NORTH_WEST;
//        if (yaw < 56.25) return BlockFace.NORTH_WEST;
//        if (yaw < 78.75) return BlockFace.NORTH_NORTH_WEST;
//        if (yaw < 101.25) return BlockFace.NORTH;
//        if (yaw < 123.75) return BlockFace.NORTH_NORTH_EAST;
//        if (yaw < 146.25) return BlockFace.NORTH_EAST;
//        if (yaw < 168.75) return BlockFace.EAST_NORTH_EAST;

//        return BlockFace.EAST;
        return BlockFace.NORTH;
    }

    public static BlockFace yawToCourseDirection(float yaw) {
        while (yaw > 180) yaw -= 360;
        while (yaw <= -180) yaw += 360;
        if (yaw < -135) return BlockFace.NORTH;
        if (yaw < -45) return BlockFace.EAST;
        if (yaw < 45) return BlockFace.SOUTH;
        if (yaw < 135) return BlockFace.WEST;
        return BlockFace.NORTH;
    }

    public static BlockFace rotate(BlockFace from, BlockFace to) {
        if ((from == BlockFace.DOWN) ||
            (from == BlockFace.UP)) return from;
        float fromYaw = directionToYaw(from);
        float toYaw = directionToYaw(to);
        float result = fromYaw + toYaw - 180;
//        float result = fromYaw + toYaw - 90;

//        Utils.debug("fromYaw=%s", fromYaw);
//        Utils.debug("toYaw=%s", toYaw);
//        Utils.debug("result=%s", result);

        return yawToDirection(result);
    }

    public static Vector rotate(Vector velocity, BlockFace from, BlockFace to) {
        double angle = directionToYaw(from) - directionToYaw(to); // degrees
        angle = angle * Math.PI / 180.0; // radians
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double x = velocity.getX();
        double z = velocity.getZ();
        velocity.setX((cos * x) + (sin * z));
        velocity.setZ((-sin * x) + (cos * z));
        return velocity;
    }

    public static boolean isMainThread() {
        return Thread.currentThread() == Global.mainThread;
    }

    public static boolean isWorkerThread() {
        if (isMainThread()) return false;
        Thread t = Thread.currentThread();
        for (BukkitWorker worker : Global.plugin.getServer().getScheduler().getActiveWorkers())
            if (worker.getThread() == t) return true;
        return false;
    }

    public static int fire(Runnable run) {
        if (! Global.enabled) return -1;
        return Global.plugin.getServer().getScheduler().scheduleSyncDelayedTask(Global.plugin, run);
    }

    // delay is millis
    public static int fireDelayed(Runnable run, long delay) {
        if (! Global.enabled) return -1;
        long ticks = delay / 50;
        return Global.plugin.getServer().getScheduler().scheduleSyncDelayedTask(Global.plugin, run, ticks);
    }

    public static <T> Future<T> call(Callable<T> task) {
        if (! Global.enabled) return null;
        return Global.plugin.getServer().getScheduler().callSyncMethod(Global.plugin, task);
    }

    public static int worker(Runnable run) {
        if (! Global.enabled) return -1;
        return Global.plugin.getServer().getScheduler().scheduleAsyncDelayedTask(Global.plugin, run);
    }

    // delay is millis
    public static int workerDelayed(Runnable run, long delay) {
        if (! Global.enabled) return -1;
        long ticks = delay / 50;
        return Global.plugin.getServer().getScheduler().scheduleAsyncDelayedTask(Global.plugin, run, ticks);
    }

    public static void cancelTask(int taskId) {
        if (! Global.enabled) return;
        Global.plugin.getServer().getScheduler().cancelTask(taskId);
    }

    public static <T extends Enum<T>> T valueOf(Class<T> cls, String s) {
        if ((s == null) || s.isEmpty() || s.equals("*") || s.equals("-")) return null;
        try {
            return Enum.valueOf(cls, s);
        } catch (IllegalArgumentException e) {}
        s = s.toLowerCase();
        T theOne = null;
        for (T value : cls.getEnumConstants()) {
            if (value.name().toLowerCase().equals(s))
                return value;
            if (value.name().toLowerCase().startsWith(s)) {
                if (theOne == null)
                    theOne = value;
                else
                    throw new IllegalArgumentException("ambiguous");
            }
        }
        if (theOne == null)
            throw new IllegalArgumentException("invalid");
        return theOne;
    }

    public static boolean prepareChunk(Location loc) {
        World world = loc.getWorld();
        Chunk chunk = world.getChunkAt(loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
        if (world.isChunkLoaded(chunk)) return false;
        world.loadChunk(chunk);
        return true;
    }

    public static void schedulePlayerKick(final Player player, final String message) {
        fire(new Runnable() {
            @Override
            public void run() {
                debug("kicking player '%s' @%s: %s", player.getName(), player.getAddress().getAddress().getHostAddress(), message);
                player.kickPlayer(message);
            }
        });
    }

    public static void sendPlayerToBungeeServer(Player player, String serverName) {
        sendPlayerChannelMessage(player, "BungeeCord", toUTF("Connect", serverName));
        sendPlayerChannelMessage(player, "RubberBand", toUTF(serverName));
    }

    public static void sendPlayerChannelMessage(Player player, String channel, byte[] payload) {
        Messenger m = Global.plugin.getServer().getMessenger();
        if (! m.isOutgoingChannelRegistered(Global.plugin, channel)) {
            debug("registering for sending messages on the '%s' channel", channel);
            m.registerOutgoingPluginChannel(Global.plugin, channel);
        }
        debug("sending payload of %s bytes on the '%s' channel", payload.length, channel);
        player.sendPluginMessage(Global.plugin, channel, payload);
    }

    public static byte[] toUTF(String ... str) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        try {
            for (String s : str)
                out.writeUTF(s);
        } catch (IOException e) {}
        return b.toByteArray();
    }

    public static HttpURLConnection openURL(URL url) throws IOException {
        Proxy proxy = Proxy.NO_PROXY;
        String proxyHost = Config.getHttpProxyHost();
        if (proxyHost != null) {
            Proxy.Type proxyType = null;
            try {
                proxyType = Utils.valueOf(Proxy.Type.class, Config.getHttpProxyType());
            } catch (IllegalArgumentException iae) {}
            int proxyPort = Config.getHttpProxyPort();
            proxy = new Proxy(proxyType, new InetSocketAddress(proxyHost, proxyPort));
            debug("using proxy %s", proxy);
        }
        HttpURLConnection conn = (HttpURLConnection)url.openConnection(proxy);
        if (proxy != Proxy.NO_PROXY) {
            String proxyUser = Config.getHttpProxyUser();
            String proxyPassword = Config.getHttpProxyPassword();
            if ((proxyUser != null) && (proxyPassword != null)) {
                String enc = Base64.encode(proxyUser + ":" + proxyPassword, "UTF-8");
                conn.setRequestProperty("Proxy-Authorization", enc);
                debug("proxy authentication enabled for user %s", proxyUser);
            }
        }
        return conn;
    }

    // can be called from any thread, upload will happen in a worker thread
    public static void submitDebug(final String id) {
        if (! isWorkerThread()) {
            debug("scheduling debug submission");
            worker(new Runnable() {
                @Override
                public void run() {
                    submitDebug(id);
                }
            });
            return;
        }
        debug("starting debug submission");

        File zipFile;
        try {
            zipFile = File.createTempFile(Global.pluginName + "-", "-debug.zip");
        } catch (IOException e) {
            severe(e, "unable to create debug zip file");
            return;
        }

        File file;
        FileInputStream in;
        int length;
        byte[] buffer = new byte[1024];

        try {
            ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile));

            // add the config file
            file = Config.getConfigFile();
            in = new FileInputStream(file);
            zipOut.putNextEntry(new ZipEntry(file.getName()));
            while ((length = in.read(buffer)) > 0)
                zipOut.write(buffer, 0, length);
            in.close();
            zipOut.closeEntry();

            // add the last DEBUG_LOG_BYTES bytes of the server log
            file = new File(BukkitBaseFolder, "server.log");
            in = new FileInputStream(file);
            zipOut.putNextEntry(new ZipEntry(file.getName()));
            if (file.length() > DEBUG_LOG_BYTES) {
                in.skip(file.length() - DEBUG_LOG_BYTES);
                // read until the end of a line
                while (in.read() != 10) {}
            }
            while ((length = in.read(buffer)) > 0)
                zipOut.write(buffer, 0, length);
            in.close();
            zipOut.closeEntry();

            // add other debug info...
            zipOut.putNextEntry(new ZipEntry("debug.txt"));
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(zipOut));
            writer.println("Id: " + id);
            writer.println();

            // MC info
            writer.format("Bukkit version:      %s\n", Global.plugin.getServer().getVersion());
            writer.format("MC address:          %s:%s\n", Global.plugin.getServer().getIp(), Global.plugin.getServer().getPort());
            writer.format("Transporter version: %s\n", Global.pluginVersion);
            writer.println();

            // list of players...
            List<Player> players = Arrays.asList(Global.plugin.getServer().getOnlinePlayers());
            Collections.sort(players, new Comparator<Player>() {
                @Override
                public int compare(Player a, Player b) {
                    return a.getName().compareToIgnoreCase(b.getName());
                }
            });
            writer.format("%d players:\n", players.size());
            for (Player player : players)
                writer.format("  %s (%s)\n", player.getName(), player.getAddress());
            writer.println();

            // list of worlds...
            List<World> worlds = Global.plugin.getServer().getWorlds();
            Collections.sort(worlds, new Comparator<World>() {
                @Override
                public int compare(World a, World b) {
                    return a.getName().compareToIgnoreCase(b.getName());
                }
            });
            writer.format("%d worlds:\n", worlds.size());
            for (World world : worlds)
                writer.format("  %s (%s)\n", world.getName(), world.getEnvironment());
            writer.println();

            // list of servers...
            List<Server> servers = Servers.getAll();
            Collections.sort(servers, new Comparator<Server>() {
                @Override
                public int compare(Server a, Server b) {
                    return a.getName().compareToIgnoreCase(b.getName());
                }
            });
            writer.format("%d servers:\n", servers.size());
            for (Server server : servers) {
                writer.format("  %s: %s '%s' %s/%s\n",
                            server.getName(),
                            server.getPluginAddress(),
                            server.getKey(),
                            (server.isEnabled() ? "up" : "down"),
                            (! server.isConnectionConnected() ? "down" :
                                String.format("up %s %s v%s",
                                    server.isIncoming() ? "incoming" : "outgoing",
                                    server.getConnection().getName(),
                                    server.getRemoteVersion()))
                        );
                writer.format("    publicAddress:        %s (%s)\n",
                        server.getPublicAddress(),
                        server.getNormalizedPublicAddress()
                        );
                writer.format("    privateAddress:       %s\n",
                        server.getPrivateAddress().equals("-") ?
                            "-" :
                            String.format("%s (%s:%d)",
                                server.getPrivateAddress(),
                                server.getNormalizedPrivateAddress().getAddress().getHostAddress(),
                                server.getNormalizedPrivateAddress().getPort()));
                if (server.isConnectionConnected()) {
                    writer.format("    remotePublicAddress:  %s\n",
                            server.getRemotePublicAddress());
                    writer.format("    remotePrivateAddress: %s\n",
                            (server.getRemotePrivateAddress() == null) ?
                                "-" : server.getRemotePrivateAddress());
                    writer.format("    remoteCluster:        %s\n",
                            (server.getRemoteCluster() == null) ?
                                "-" : server.getRemoteCluster());

                }
            }
            writer.println();

            // local gates...
            List<LocalGateImpl> gates = new ArrayList<LocalGateImpl>(Gates.getLocalGates());
            Collections.sort(gates, new Comparator<LocalGateImpl>() {
                @Override
                public int compare(LocalGateImpl a, LocalGateImpl b) {
                    return a.getFullName().compareToIgnoreCase(b.getFullName());
                }
            });

            writer.format("%d gates:\n", gates.size());
            for (LocalGateImpl gate : gates) {
                writer.format("  %s\n", gate.getFullName());
                if (gate.isSameServer()) {
                    writer.format("    type: %s\n", gate.getType().toString());
                    if (gate.getLinkLocal())
                        writer.format("local cost: %s/%s\n",
                                gate.getSendLocalCost(),
                                gate.getReceiveLocalCost());
                    if (gate.getLinkWorld())
                        writer.format("world cost: %s/%s\n",
                                gate.getSendWorldCost(),
                                gate.getReceiveWorldCost());
                    if (gate.getLinkServer())
                        writer.format("server cost: %s/%s\n",
                                gate.getSendServerCost(),
                                gate.getReceiveServerCost());
                    List<String> links = gate.getLinks();
                    writer.format("    links: %d\n", links.size());
                    for (String link : links)
                        writer.format("     %s%s\n", link.equals(gate.getDestinationLink()) ? "*": "", link);
                }
            }
            writer.println();

            // list of interfaces/addresses
            writer.println("Network interfaces:");
            for (Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces(); e.hasMoreElements(); ) {
                NetworkInterface iface = e.nextElement();
                writer.format("%s (%s) %s\n", iface.getName(), iface.getDisplayName(), iface.isUp() ? "UP" : "DOWN");
                if (! iface.isUp()) continue;
                for (InterfaceAddress address : iface.getInterfaceAddresses()) {
                    InetAddress addr = address.getAddress();
                    if (! (addr instanceof Inet4Address)) continue;
                    writer.format("  %s/%s\n", addr.getHostAddress(), address.getNetworkPrefixLength());
                }
            }
            writer.println();

            writer.flush();
            zipOut.closeEntry();

            zipOut.close();
        } catch (IOException e) {
            severe(e, "unable to create debug zip file '%s':", zipFile.getAbsolutePath());
            zipFile.delete();
            return;
        }

        debug("debug data file '%s' created successfully", zipFile.getAbsolutePath());

        try {
            URL url = new URL("http://www.frdfsnlght.com/transporter-debug.php");
            debug("submitting to %s", url);
            HttpURLConnection conn = openURL(url);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setAllowUserInteraction(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + DEBUG_BOUNDARY);
            OutputStream out = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));

            writer.write("--" + DEBUG_BOUNDARY + "\r\n");
            writer.write("Content-Disposition: form-data; name=\"id\"\r\n");
            writer.write("\r\n");
            writer.write(URLEncoder.encode(id, "UTF-8"));
            writer.write("\r\n");

            writer.write("--" + DEBUG_BOUNDARY + "\r\n");
            writer.write("Content-Disposition: form-data; name=\"file\"; filename=\"content.zip\"\r\n");
            writer.write("\r\n");
            writer.flush();

            in = new FileInputStream(zipFile);
            while ((length = in.read(buffer)) > 0)
                out.write(buffer, 0, length);
            in.close();
            out.flush();

            writer.write("\r\n");
            writer.write("--" + DEBUG_BOUNDARY + "--\r\n");
            writer.flush();
            writer.close();

            int status = conn.getResponseCode();
            if ((status >= 200) && (status < 300))
                debug("debug data submitted successfully");
            else
                warning("unable to submit debug data: HTTP status " + status);
        } catch (IOException e) {
            severe(e, "unable to submit debug data:");
        } finally {
            if (Config.getDeleteDebugFile())
                zipFile.delete();
        }

    }

    public static String byteArrayToString(byte[] buffer, int pos, int len) {
        StringBuilder b = new StringBuilder();
        for (int i = pos; i < pos + len; i++) {
            if (i >= buffer.length) {
                b.append("??");
                break;
            }
            int v = unsignedByteToInt(buffer[i]);
            if (v < 16) b.append('0');
            b.append(Integer.toHexString(v));
            b.append(' ');
        }
        return b.toString();
    }

    /*
    public static byte intToUnsignedByte(int in, int byteNum) {
        return (byte)(0x00ff & (in >> (byteNum * 8)));
    }
    */

    public static int unsignedByteToInt(byte in) {
        int out = (int)in;
        if (out < 0) out += 256;
        return out;
    }

    public static void checkVersion() {
        try {
            String urlStr = Global.plugin.getDescription().getWebsite() + "pages/version";
            URL url = new URL(urlStr);
            HttpURLConnection http = (HttpURLConnection)url.openConnection();
            http.setInstanceFollowRedirects(true);
            int statusCode = http.getResponseCode();
            if (statusCode != 200) {
                debug("got status %s during plugin version check");
                http.disconnect();
                return;
            }
            InputStreamReader isr = new InputStreamReader(http.getInputStream());
            BufferedReader br = new BufferedReader(isr);
            String content;
            StringBuilder sb = new StringBuilder();
            while ((content = br.readLine()) != null)
                sb.append(content);
            br.close();

            int myVersion = versionToInt(Global.pluginVersion);
            boolean iAmBeta = isBetaVersion(Global.pluginVersion);

            Pattern pattern = Pattern.compile("RELEASE:(.+):RELEASE");
            Matcher matcher = pattern.matcher(sb);
            if (! matcher.find()) {
                debug("couldn't find release version string!");
                return;
            }
            String releaseVersionStr = matcher.group(1);
            int releaseVersion = versionToInt(releaseVersionStr);

            pattern = Pattern.compile("BETA:(.+):BETA");
            matcher = pattern.matcher(sb);
            String betaVersionStr = matcher.find() ? matcher.group(1) : null;
            int betaVersion = versionToInt(releaseVersionStr);

            if (releaseVersionStr.equals(Global.pluginVersion)) {
                debug("plugin is current");
                return;
            }

            String upgradeVersion;

            if (iAmBeta) {
                if (releaseVersion > myVersion)
                    upgradeVersion = releaseVersionStr;
                else if (betaVersion > myVersion)
                    upgradeVersion = betaVersionStr;
                else {
                    debug("beta version is current");
                    return;
                }
            } else {
                if (releaseVersion > myVersion)
                    upgradeVersion = releaseVersionStr;
                else {
                    debug("release version is current");
                    return;
                }
            }
            info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            info("!! This is an outdated version of %s.", Global.pluginName);
            info("!! Please upgrade to %s.", upgradeVersion);
            info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        } catch (UnsupportedEncodingException uee) {
        } catch (MalformedURLException mue) {
        } catch (IOException ioe) {
        }
    }

    private static boolean isBetaVersion(String version) {
        return version.contains("beta");
    }

    private static int versionToInt(String version) {
        if (version == null) return 0;
        Matcher matcher = Pattern.compile("v?(\\d+)\\.(\\d+)(?:beta)?(\\d+)?").matcher(version);
        if (! matcher.find()) return 0;

        int major = Integer.parseInt(matcher.group(1));
        int minor = Integer.parseInt(matcher.group(2));
        int beta = (matcher.group(3) != null) ? Integer.parseInt(matcher.group(3)) : 0;

        if (beta > 0) {
            minor--;
            if (minor < 0) {
                minor = 99;
                major--;
            }
        }
        int v = (major * 10000) + (minor * 100) + beta;
        return v;
    }

}
