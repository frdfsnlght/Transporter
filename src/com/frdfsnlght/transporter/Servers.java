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

import com.frdfsnlght.transporter.api.RemoteServer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class Servers {

    private static final int CONNECT_DELAY = 4000;

    private static final Map<String,Server> servers = new HashMap<String,Server>();

    public static void onConfigLoad(Context ctx) {
        removeAll();
        servers.clear();
        List<TypeMap> serverMaps = Config.getMapList("servers");
        if (serverMaps != null) {
            for (TypeMap map : serverMaps) {
                try {
                    Server server = new Server(map);
                    add(server);
                    ctx.sendLog("loaded server '%s'", server.getName());
                } catch (ServerException se) {
                    ctx.warnLog("unable to load server: %s", se.getMessage());
                }
            }
        }
    }

    public static void onConfigSave() {
        List<Map<String,Object>> serverNodes = new ArrayList<Map<String,Object>>();
        for (Server server : servers.values())
            serverNodes.add(server.encode());
        Config.setPropertyDirect("servers", serverNodes);
    }

    public static void add(final Server server) throws ServerException {
        String name = server.getName();
        if (servers.containsKey(name))
            throw new ServerException("a server with the same name already exists");
        servers.put(server.getName(), server);
        if (server.isEnabled())
            Utils.fireDelayed(new Runnable() {
                @Override
                public void run() {
                    server.connect();
                }
            }, CONNECT_DELAY);
    }

    public static void remove(Server server) {
        String name = server.getName();
        if (! servers.containsKey(name)) return;
        servers.remove(name);
        server.disconnect(false);
    }

    public static void removeAll() {
        for (Server server : new ArrayList<Server>(servers.values()))
            remove(server);
    }

    public static void connectAll() {
        for (final Server server : servers.values()) {
            if ((! server.isConnectionConnected()) && server.isEnabled())
                Utils.fireDelayed(new Runnable() {
                    @Override
                    public void run() {
                        server.connect();
                    }
                }, CONNECT_DELAY);
        }
    }

    public static void disconnectAll() {
        for (Server server : servers.values())
            server.disconnect(false);
    }

    public static Server get(String name) {
        return servers.get(name);
    }

    public static Server find(String name) {
        if (servers.containsKey(name)) return servers.get(name);
        Server server = null;
        name = name.toLowerCase();
        for (String key : servers.keySet()) {
            if (key.toLowerCase().startsWith(name)) {
                if (server == null) server = servers.get(key);
                else return null;
            }
        }
        return server;
    }

    public static Server findByRemoteName(String name) {
        Server server = null;
        for (Server s : servers.values()) {
            if (! s.isConnected()) continue;
            if (s.getRemoteServer().equals(name)) {
                if (server == null) server = s;
                else return null;
            }
        }
        return server;
    }

    public static List<Server> getAll() {
        return new ArrayList<Server>(servers.values());
    }

    public static boolean isEmpty() {
        return size() == 0;
    }

    public static int size() {
        return servers.size();
    }

    public static RemoteServer getRemoteServer(String name) {
        Server server = find(name);
        if (server == null)
            throw new IllegalArgumentException("server '" + name + "' does not exist");
        return server;
    }

}
