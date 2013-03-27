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
import com.frdfsnlght.transporter.api.RemoteServer;
import com.frdfsnlght.transporter.api.RemoteWorld;
import org.bukkit.Difficulty;
import org.bukkit.World.Environment;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class RemoteWorldImpl implements RemoteWorld {

    private Server server;
    private String name;

    public RemoteWorldImpl(Server server, String name) {
        this.server = server;
        if (name == null) throw new IllegalArgumentException("name is required");
        this.name = name;
    }

    @Override
    public RemoteServer getRemoteServer() {
        return server;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void getDifficulty(final Callback<Difficulty> cb) {
        TypeMap args = new TypeMap();
        args.put("world", name);
        server.sendAPIRequest(new APICallback<TypeMap>() {
            @Override
            public void onSuccess(TypeMap m) {
                if (cb != null) cb.onSuccess(Utils.valueOf(Difficulty.class, m.getString("result")));
            }
            @Override
            public void onFailure(RemoteException re) {
                if (cb != null) cb.onFailure(re);
            }
        }, "world", "getDifficulty", args);
    }

    @Override
    public void getEnvironment(final Callback<Environment> cb) {
        TypeMap args = new TypeMap();
        args.put("world", name);
        server.sendAPIRequest(new APICallback<TypeMap>() {
            @Override
            public void onSuccess(TypeMap m) {
                if (cb != null) cb.onSuccess(Utils.valueOf(Environment.class, m.getString("result")));
            }
            @Override
            public void onFailure(RemoteException re) {
                if (cb != null) cb.onFailure(re);
            }
        }, "world", "getEnvironment", args);
    }

    @Override
    public void getFullTime(final Callback<Long> cb) {
        TypeMap args = new TypeMap();
        args.put("world", name);
        server.sendAPIRequest(new APICallback<TypeMap>() {
            @Override
            public void onSuccess(TypeMap m) {
                if (cb != null) cb.onSuccess(m.getLong("result"));
            }
            @Override
            public void onFailure(RemoteException re) {
                if (cb != null) cb.onFailure(re);
            }
        }, "world", "getFullTime", args);
    }

    @Override
    public void getSeed(final Callback<Long> cb) {
        TypeMap args = new TypeMap();
        args.put("world", name);
        server.sendAPIRequest(new APICallback<TypeMap>() {
            @Override
            public void onSuccess(TypeMap m) {
                if (cb != null) cb.onSuccess(m.getLong("result"));
            }
            @Override
            public void onFailure(RemoteException re) {
                if (cb != null) cb.onFailure(re);
            }
        }, "world", "getSeed", args);
    }

    @Override
    public void getTime(final Callback<Long> cb) {
        TypeMap args = new TypeMap();
        args.put("world", name);
        server.sendAPIRequest(new APICallback<TypeMap>() {
            @Override
            public void onSuccess(TypeMap m) {
                if (cb != null) cb.onSuccess(m.getLong("result"));
            }
            @Override
            public void onFailure(RemoteException re) {
                if (cb != null) cb.onFailure(re);
            }
        }, "world", "getTime", args);
    }

}
