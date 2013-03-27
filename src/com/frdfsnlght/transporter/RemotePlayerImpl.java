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

import com.frdfsnlght.transporter.api.TypeMap;
import com.frdfsnlght.transporter.api.Callback;
import com.frdfsnlght.transporter.api.RemoteException;
import com.frdfsnlght.transporter.api.RemoteLocation;
import com.frdfsnlght.transporter.api.RemotePlayer;
import com.frdfsnlght.transporter.api.RemoteServer;
import com.frdfsnlght.transporter.api.RemoteWorld;
import org.bukkit.entity.Player;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class RemotePlayerImpl implements RemotePlayer {

    private Server server;
    private String name;
    private String displayName;
    private String worldName;
    private String prefix;
    private String suffix;

    public RemotePlayerImpl(Server server, String name, String displayName, String worldName, String prefix, String suffix) {
        this.server = server;
        if (name == null) throw new IllegalArgumentException("name is required");
        this.name = name;
        if (displayName == null) displayName = name;
        this.displayName = displayName;
        setWorld(worldName);
        this.worldName = worldName;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public String format(String f) {
        if (f == null) return "";
        f = f.replace("%prefix%", (prefix == null) ? "" : prefix);
        f = f.replace("%suffix%", (suffix == null) ? "" : suffix);
        f = f.replace("%player%", getDisplayName());
        f = f.replace("%world%", (getRemoteWorld() == null) ? "unknown" : getRemoteWorld().getName());
        f = f.replace("%server%", (getRemoteServer() == null) ? "unknown" : getRemoteServer().getName());
        return f;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public RemoteWorld getRemoteWorld() {
        return server.getRemoteWorld(worldName);
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public void setWorld(String worldName) {
        if (worldName == null) throw new IllegalArgumentException("worldName is required");
        this.worldName = worldName;
    }

    public void setWorld(RemoteWorld world) {
        worldName = world.getName();
    }

    @Override
    public RemoteServer getRemoteServer() {
        return server;
    }

    @Override
    public void getRemoteLocation(final Callback<RemoteLocation> cb) {
        TypeMap args = new TypeMap();
        args.put("player", name);
        server.sendAPIRequest(new APICallback<TypeMap>() {
            @Override
            public void onSuccess(TypeMap m) {
                TypeMap locMsg = m.getMap("result");
                RemoteLocation loc = new RemoteLocation(server, server.getRemoteWorld(locMsg.getString("world")), locMsg.getDouble("x"), locMsg.getDouble("y"), locMsg.getDouble("z"));
                cb.onSuccess(loc);
            }
            @Override
            public void onFailure(RemoteException re) {
                cb.onFailure(re);
            }
        }, "player", "getLocation", args);
    }

    @Override
    public void sendMessage(final Callback<Void> cb, String msg) {
        TypeMap args = new TypeMap();
        args.put("player", name);
        args.put("message", msg);
        server.sendAPIRequest(new APICallback<TypeMap>() {
            @Override
            public void onSuccess(TypeMap m) {
                if (cb != null) cb.onSuccess(null);
            }
            @Override
            public void onFailure(RemoteException re) {
                if (cb != null) cb.onFailure(re);
            }
        }, "player", "sendMessage", args);
    }

    @Override
    public void sendRawMessage(final Callback<Void> cb, String msg) {
        TypeMap args = new TypeMap();
        args.put("player", name);
        args.put("message", msg);
        server.sendAPIRequest(new APICallback<TypeMap>() {
            @Override
            public void onSuccess(TypeMap m) {
                if (cb != null) cb.onSuccess(null);
            }
            @Override
            public void onFailure(RemoteException re) {
                if (cb != null) cb.onFailure(re);
            }
        }, "player", "sendRawMessage", args);
    }

    @Override
    public void sendPM(Player fromPlayer, String message) {
        server.sendPrivateMessage(fromPlayer, this, message);
    }

}
