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
package com.frdfsnlght.transporter.net;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import com.frdfsnlght.transporter.Config;
import com.frdfsnlght.transporter.Context;
import com.frdfsnlght.transporter.Global;
import com.frdfsnlght.transporter.Options;
import com.frdfsnlght.transporter.OptionsException;
import com.frdfsnlght.transporter.OptionsListener;
import com.frdfsnlght.transporter.PermissionsException;
import com.frdfsnlght.transporter.Server;
import com.frdfsnlght.transporter.Servers;
import com.frdfsnlght.transporter.ThreadState;
import com.frdfsnlght.transporter.Utils;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class Network {

    private static final Set<String> OPTIONS = new HashSet<String>();
    private static final Set<String> RESTART_OPTIONS = new HashSet<String>();
    private static final Options options;

    static {
        OPTIONS.add("readBufferSize");
        OPTIONS.add("selectInterval");
        OPTIONS.add("usePrivateAddress");
        OPTIONS.add("sendPrivateAddress");
        OPTIONS.add("clusterName");
        OPTIONS.add("reconnectInterval");
        OPTIONS.add("reconnectSkew");
        OPTIONS.add("listenAddress");
        OPTIONS.add("key");
        OPTIONS.add("suppressConnectionAttempts");
        OPTIONS.add("bungeeServer");

        RESTART_OPTIONS.add("readBufferSize");
        RESTART_OPTIONS.add("selectInterval");
        RESTART_OPTIONS.add("clusterName");
        RESTART_OPTIONS.add("listenAddress");
        RESTART_OPTIONS.add("key");

        options = new Options(Network.class, OPTIONS, "trp.network", new OptionsListener() {
            @Override
            public void onOptionSet(Context ctx, String name, String value) {
                ctx.send("network option '%s' set to '%s'", name, value);
                if (RESTART_OPTIONS.contains(name)) {
                    Config.save(ctx);
                    restart(ctx);
                }
            }
            @Override
            public String getOptionPermission(Context ctx, String name) {
                return name;
            }
        });
    }


    public static InetSocketAddress makeInetSocketAddress(String addrStr, String defAddr, int defPort, boolean allowWildcard) throws IllegalArgumentException {
        String addrPart = defAddr;
        String portPart = defPort + "";
        if (addrStr != null) {
            String[] parts = addrStr.split(":");
            if (parts[0].matches("^\\d+$")) {
                addrPart = defAddr;
                portPart = parts[0];
            } else {
                addrPart = (parts[0].length() > 0) ? parts[0] : defAddr;
                if (addrPart.equals("*")) addrPart = allowWildcard ? "0.0.0.0" : defAddr;
                portPart = (parts.length > 1) ? parts[1] : (defPort + "");
            }
        }

        if (addrPart == null)
            throw new IllegalArgumentException("missing address");
        if (portPart == null)
            throw new IllegalArgumentException("missing port");

        InetAddress address = null;
        if (addrPart.equals("0.0.0.0")) {
            if (! allowWildcard)
                throw new IllegalArgumentException("wildcard address not allowed");
        } else {

            // try to find a matching interface name
            try {
                for (Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces(); e.hasMoreElements(); ) {
                    NetworkInterface iface = e.nextElement();
                    if (iface.getName().equals(addrPart)) {
                        address = getInterfaceAddress(iface);
                        if (address != null) break;
                    }
                }
            } catch (SocketException e) {
                throw new IllegalArgumentException("unable to get local interfaces");
            }
            // try to find matching hostname
            try {
                address = InetAddress.getByName(addrPart);
            } catch (UnknownHostException uhe) {
                throw new IllegalArgumentException("unknown host address '" + addrPart + "'");
            }
        }

        int port;
        try {
            port = Integer.parseInt(portPart);
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("invalid port '" + portPart + "'");
        }
        if ((port < 1) || (port > 65535))
            throw new IllegalArgumentException("invalid port '" + port + "'");
        return new InetSocketAddress(address, port);
    }

    public static InetAddress getInterfaceAddress() {
        try {
            for (Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces(); e.hasMoreElements(); ) {
                NetworkInterface iface = e.nextElement();
                if (! iface.isUp()) continue;
                if (iface.isLoopback()) continue;
                InetAddress addr = getInterfaceAddress(iface);
                if (addr == null) continue;
                return addr;
            }
            return null;
        } catch (SocketException e) {
            return null;
        }
    }

    public static InetAddress getInterfaceAddress(NetworkInterface iface) {
        if (iface == null) return null;
        for (Enumeration<InetAddress> e = iface.getInetAddresses(); e.hasMoreElements(); ) {
            InetAddress addr = e.nextElement();
            if (addr.isLoopbackAddress()) continue;
            if (addr instanceof Inet4Address) return addr;
        }
        return null;
    }

    private static Thread networkThread;
    private static ThreadState state = ThreadState.STOPPED;
    private static InetSocketAddress listenAddress = null;
    private static String key;
    private static int selectInterval;
    private static int readBufferSize;
    private static Selector selector = null;
    private static final Set<Pattern> banned = new HashSet<Pattern>();
    private static final Map<SocketChannel,Connection> channels = new HashMap<SocketChannel,Connection>();
    private static final Set<Connection> opening = new HashSet<Connection>();
    private static final Set<Connection> closing = new HashSet<Connection>();

    // called from main thread
    public static void start(Context ctx) {
        try {
            if (listenAddress == null)
                throw new NetworkException("listenAddress is not set");
            if (key == null)
                throw new NetworkException("key is not set");
        } catch (Exception e) {
            ctx.warn("network manager cannot be started (server-to-server is disabled): %s", e.getMessage());
            return;
        }

        networkThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Network.run();
            }
        });
        ctx.send("starting network manager...");
        networkThread.start();
    }

    public static void restart(Context ctx) {
        stop(ctx);
        onConfigLoad(ctx);
        start(ctx);
        Servers.connectAll();
    }

    // called from main thread
    public static void stop(Context ctx) {
        if ((networkThread == null) ||
            (! networkThread.isAlive()) ||
            (state != ThreadState.RUNNING)) return;
        ctx.send("stopping network manager...");
        Servers.disconnectAll();
        state = ThreadState.STOP;
        selector.wakeup();
        while (networkThread.isAlive()) {
            try {
                networkThread.join();
            } catch (InterruptedException ie) {}
        }
        networkThread = null;
        ctx.send("network manager stopped");
    }

    public static void onConfigLoad(Context ctx) {
        //boolean restart = state == State.RUNNING;
        //if (restart) Network.stop(ctx);
        try {
            listenAddress = makeInetSocketAddress(getListenAddress(), "0.0.0.0", Global.DEFAULT_PLUGIN_PORT, true);
        } catch (IllegalArgumentException e) {
            ctx.warn("listenAddress: %s", e.getMessage());
        }
        key = getKey();
        selectInterval = getSelectInterval();
        readBufferSize = getReadBufferSize();

        banned.clear();
        List<String> addresses = Config.getStringList("network.bannedAddresses");
        if (addresses != null)
            for (String addressPattern : addresses) {
                try {
                    Pattern pattern = Pattern.compile(addressPattern);
                    banned.add(pattern);
                } catch (PatternSyntaxException pse) {
                    ctx.warn("ignored invalid bannedAddress pattern '%s': %s", addressPattern, pse.getMessage());
                }
            }
        //if (restart) Network.start(ctx);
    }

    public static void onConfigSave() {
        synchronized (banned) {
            List<String> bannedAddresses = new ArrayList<String>(banned.size());
            for (Pattern p : banned)
                bannedAddresses.add(p.pattern());
            Config.setPropertyDirect("network.bannedAddresses", bannedAddresses);
        }
    }

    /* Begin options */

    public static int getReadBufferSize() {
        return Config.getIntDirect("network.readBufferSize", 4096);
    }

    public static void setReadBufferSize(int i) {
        if (i < 1024)
            throw new IllegalArgumentException("readBufferSize must be at least 1024");
        Config.setPropertyDirect("network.readBufferSize", i);
    }

    public static int getSelectInterval() {
        return Config.getIntDirect("network.selectInterval", 30000);
    }

    public static void setSelectInterval(int i) {
        if (i < 1000)
            throw new IllegalArgumentException("selectInterval must be at least 1000");
        Config.setPropertyDirect("network.selectInterval", i);
    }

    public static boolean getUsePrivateAddress() {
        return Config.getBooleanDirect("network.usePrivateAddress", true);
    }

    public static void setUsePrivateAddress(boolean b) {
        Config.setPropertyDirect("network.usePrivateAddress", b);
    }

    public static boolean getSendPrivateAddress() {
        return Config.getBooleanDirect("network.sendPrivateAddress", true);
    }

    public static void setSendPrivateAddress(boolean b) {
        Config.setPropertyDirect("network.sendPrivateAddress", b);
    }

    public static String getClusterName() {
        return Config.getStringDirect("network.clusterName", null);
    }

    public static void setClusterName(String s) {
        if ((s != null) && (s.equals("-") || s.equals("*"))) s = null;
        Config.setPropertyDirect("network.clusterName", s);
    }

    public static String getBungeeServer() {
        return Config.getStringDirect("network.bungeeServer", null);
    }

    public static void setBungeeServer(String s) {
        if ((s != null) && (s.equals("-") || s.equals("*"))) s = null;
        Config.setPropertyDirect("network.bungeeServer", s);
    }

    public static int getReconnectInterval() {
        return Config.getIntDirect("network.reconnectInterval", 60000);
    }

    public static void setReconnectInterval(int i) {
        if (i < 10000)
            throw new IllegalArgumentException("reconnectInterval must be at least 10000");
        Config.setPropertyDirect("network.reconnectInterval", i);
    }

    public static int getReconnectSkew() {
        return Config.getIntDirect("network.reconnectSkew", 10000);
    }

    public static void setReconnectSkew(int i) {
        if (i < 0)
            throw new IllegalArgumentException("reconnectSkew must be greater than 0");
        Config.setPropertyDirect("network.reconnectSkew", i);
    }

    public static String getListenAddress() {
        return Config.getStringDirect("network.listenAddress", null);
    }

    public static void setListenAddress(String s) {
        Network.makeInetSocketAddress(s, "0.0.0.0", Global.DEFAULT_PLUGIN_PORT, true);
        Config.setPropertyDirect("network.listenAddress", s);
    }

    public static String getKey() {
        return Config.getStringDirect("network.key", null);
    }

    public static void setKey(String s) {
        if ((s != null) && (s.equals("-") || s.equals("*"))) s = null;
        Config.setPropertyDirect("network.key", s);
    }

    public static int getSuppressConnectionAttempts() {
        return Config.getIntDirect("network.suppressConnectionAttempts", -1);
    }

    public static void setSuppressConnectionAttempts(int i) {
        Config.setPropertyDirect("network.suppressConnectionAttempts", i);
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

    public static String getCachedKey() {
        return key;
    }

    public static boolean isStopped() {
        return (state == ThreadState.STOP) || (state == ThreadState.STOPPING) || (state == ThreadState.STOPPED);
    }

    // called from main thread
    public static boolean addBannedAddress(String addrStr) throws NetworkException {
        Pattern pattern;
        try {
            pattern = Pattern.compile(addrStr);
        } catch (PatternSyntaxException pse) {
            throw new NetworkException("invalid pattern: %s", pse.getMessage());
        }
        synchronized (banned) {
            if (banned.contains(pattern)) return false;
            banned.remove(pattern);
            return true;
        }
    }

    // called from main thread
    public static boolean removeBannedAddress(String addrStr) {
        synchronized (banned) {
            Iterator<Pattern> i = banned.iterator();
            while (i.hasNext()) {
                Pattern p = i.next();
                if (p.pattern().equals(addrStr)) {
                    i.remove();
                    return true;
                }
            }
        }
        return false;
    }

    // called from main thread
    public static void removeAllBannedAddresses() {
        synchronized (banned) {
            banned.clear();
        }
    }

    public static List<String> getBannedAddresses() {
        List<String> l = new ArrayList<String>();
        synchronized (banned) {
            for (Pattern pattern : banned) {
                l.add(pattern.toString());
            }
        }
        return l;
    }

    /* Networking gunk */

    private static void run() {

        ServerSocketChannel serverChannel = null;

        try {
            // create the selector
            selector = Selector.open();
            serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);

            // bind to address and port
            serverChannel.socket().bind(listenAddress);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            Utils.info("network manager listening on %s:%d", listenAddress.getAddress().getHostAddress(), listenAddress.getPort());
            state = ThreadState.RUNNING;

            // processing
            while (true) {
                if (state == ThreadState.STOP) {
                    state = ThreadState.STOPPING;
                    serverChannel.keyFor(selector).cancel();
                    synchronized (closing) {
                        closing.addAll(channels.values());
                        for (Connection conn : closing)
                            wantWrite(conn);
                    }
                    synchronized (opening) {
                        opening.removeAll(channels.values());
                    }
                }
                if ((state == ThreadState.STOPPING) && channels.isEmpty()) break;

                // Close connections that are still waiting to open
                synchronized (closing) {
                    if (! closing.isEmpty()) {
                        for (Connection conn : new HashSet<Connection>(closing)) {
                            if (conn.onHasWriteData()) continue;
                            kill(conn);
                            conn.onClosed();
                        }
                    }
                }
                if ((state == ThreadState.STOPPING) && channels.isEmpty()) break;

                // Open connections that are waiting
                synchronized (opening) {
                    if (! opening.isEmpty()) {
                        for (Connection conn : opening) {
                            try {
                                SocketChannel channel = SocketChannel.open();
                                channel.configureBlocking(false);
                                try {
                                    InetSocketAddress address = makeInetSocketAddress(conn.getConnectAddress(), "localhost", Global.DEFAULT_PLUGIN_PORT, false);
                                    channel.connect(address);
                                } catch (Exception e) {}
                                channel.register(selector, SelectionKey.OP_CONNECT);
                                channels.put(channel, conn);
                                conn.onOpening(channel);
                            } catch (IOException e) {
                                conn.onException(e);
                            }
                        }
                        opening.clear();
                    }
                }

                // Tell connected servers to do keep alives
                for (Server server : Servers.getAll()) {
                    server.sendKeepAlive();
                    server.checkKeepAlive();
                }

                if (selector.select(selectInterval) > 0) {
                    Iterator keys = selector.selectedKeys().iterator();
                    while (keys.hasNext()) {
                        SelectionKey selKey = (SelectionKey)keys.next();
                        keys.remove();
                        if (! selKey.isValid()) continue;
                        if (selKey.isAcceptable()) onAccept(selKey);
                        else if (selKey.isConnectable()) onConnect(selKey);
                        else if (selKey.isReadable()) onRead(selKey);
                        else if (selKey.isWritable()) onWrite(selKey);
                    }
                }

            }

            Utils.info("network manager stopped listening");

        } catch (IOException ioe) {
            Utils.severe(ioe, "network manager IOException: " + ioe.getMessage());
        }
        state = ThreadState.STOPPED;

        if (selector != null)
            try {
                selector.close();
            } catch (IOException ioe) {}
        if (serverChannel != null)
            try {
                serverChannel.close();
            } catch (IOException ioe) {}

    }

    // called from selection thread
    private static void kill(Connection conn) {
        Utils.debug("kill %s", conn);
        SocketChannel channel = conn.getChannel();
        if (channel != null) {
            SelectionKey selKey = channel.keyFor(selector);
            if (selKey != null)
                selKey.cancel();
            try {
                channel.close();
            } catch (IOException e) {}
            channels.remove(channel);
        }
        synchronized (closing) {
            closing.remove(conn);
        }
        synchronized (opening) {
            opening.remove(conn);
        }
        conn.onKilled();
    }

    private static void onAccept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel)key.channel();
        SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);

        Socket socket = channel.socket();
        InetSocketAddress remoteAddress = (InetSocketAddress)socket.getRemoteSocketAddress();

        // rejected banned addresses
        synchronized (banned) {
            String addr = remoteAddress.getAddress().getHostAddress();
            for (Pattern p : banned) {
                if (p.matcher(addr).matches()) {
                    Utils.info("rejected connection from banned address '%s'", addr);
                    try {
                        socket.close();
                    } catch (IOException ioe) {}
                    return;
                }
            }
        }

        Connection conn = new Connection(channel);
        channels.put(channel, conn);
        channel.register(selector, SelectionKey.OP_READ);
        conn.onAccepted();
    }

    private static void onConnect(SelectionKey key) {
        SocketChannel channel = (SocketChannel)key.channel();
        Connection conn = channels.get(channel);
        if (conn == null) {
            key.cancel();
            try {
                channel.close();
            } catch (IOException e) {}
            return;
        }

        try {
            if (channel.isConnectionPending())
                channel.finishConnect();
        } catch (IOException e) {
            conn.onException(e);
            return;
        }
        key.interestOps(SelectionKey.OP_READ);
        conn.onOpened();
    }

    private static void onRead(SelectionKey key) {
        SocketChannel channel = (SocketChannel)key.channel();
        Connection conn = channels.get(channel);
        if (conn == null) {
            key.cancel();
            try {
                channel.close();
            } catch (IOException e) {}
            return;
        }

        ByteBuffer buffer = ByteBuffer.allocate(readBufferSize);
        int numRead = 0;
        while (true) {
            try {
                numRead = channel.read(buffer);
            } catch (IOException e) {
                conn.onException(e);
                return;
            }
            Utils.debug("read %d from %s", numRead, conn);
            if (numRead <= 0) break;
            conn.onReadData(Arrays.copyOfRange(buffer.array(), 0, numRead));
            if (numRead < readBufferSize) break;
            buffer.clear();
        }
        if (numRead == -1) {
            kill(conn);
            conn.onClosed();
        }
    }

    private static void onWrite(SelectionKey key) {
        SocketChannel channel = (SocketChannel)key.channel();
        Connection conn = channels.get(channel);
        if (conn == null) {
            key.cancel();
            try {
                channel.close();
            } catch (IOException e) {}
            return;
        }

        ByteBuffer buffer;
        byte[] data;
        int numWrote;
        while (true) {
            data = conn.onGetWriteData();
            if (data == null) break;
            buffer = ByteBuffer.wrap(data);
            try {
                numWrote = channel.write(buffer);
            } catch (IOException e) {
                conn.onException(e);
                return;
            }
            Utils.debug("wrote %d to %s", numWrote, conn);
            if (numWrote == data.length) continue;
            conn.onPutWriteData(Arrays.copyOfRange(data, numWrote, data.length - 1));
            break;
        }
        if (! conn.onHasWriteData()) {
            key.interestOps(SelectionKey.OP_READ);
            synchronized (closing) {
                if (closing.contains(conn)) {
                    kill(conn);
                    conn.onClosed();
                    return;
                }
            }
            conn.onWriteCompleted();
        }
    }

    // can be called from any thread
    public static void open(Connection conn) {
        synchronized (opening) {
            opening.add(conn);
        }
        if (selector != null)
            selector.wakeup();
    }

    // can be called from any thread
    public static void close(Connection conn) {
        synchronized (closing) {
            closing.add(conn);
        }
        wantWrite(conn);
    }

    // can be called from any thread
    public static void wantWrite(Connection conn) {
        if (conn == null) return;
        if (conn.getChannel() == null) return;
        SelectionKey selKey = conn.getChannel().keyFor(selector);
        if ((selKey == null) || (! selKey.isValid())) return;
        selKey.interestOps(selKey.interestOps() | SelectionKey.OP_WRITE);

        /*
        try {
            Utils.debug("wantWrite to %s: %s %s %s %s", conn,
                conn.getChannel().isOpen(),
                conn.getChannel().isConnected(),
                conn.getChannel().socket().isClosed(),
                conn.getChannel().socket().isConnected());
        } catch (Throwable t) {
            Utils.debug("wantWrite to %s: got throwable: %s: %s", conn, t.getClass().getName(), t.getMessage());
        }
        */

        selector.wakeup();
    }

}
