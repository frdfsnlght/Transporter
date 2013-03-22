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

import com.frdfsnlght.transporter.api.GateException;
import com.frdfsnlght.transporter.api.GateType;
import com.frdfsnlght.transporter.api.RemoteGate;
import com.frdfsnlght.transporter.api.RemoteServer;
import com.frdfsnlght.transporter.api.RemoteWorld;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public abstract class RemoteGateImpl extends GateImpl implements RemoteGate {

    public static RemoteGateImpl create(Server server, GateType type, String name, boolean hidden) throws GateException {
        switch (type) {
            case BLOCK:
                return new RemoteBlockGateImpl(server, name, hidden);
            case AREA:
                return new RemoteAreaGateImpl(server, name, hidden);
            case SERVER:
                return new RemoteServerGateImpl(server, name, hidden);
        }
        throw new GateException("unknown gate type '%s'", type.toString());
    }

    protected Server server;
    protected String worldName;
    protected boolean hidden;

    protected RemoteGateImpl(Server server, String name, boolean hidden) {
        this.server = server;
        if (name == null) throw new IllegalArgumentException("name is required");
        String[] parts = name.split("\\.", 2);
        worldName = parts[0];
        this.name = parts[1];
        this.hidden = hidden;
    }

    @Override
    public abstract GateType getType();

    @Override
    public String getLocalName() {
        return worldName + "." + getName();
    }

    @Override
    public String getFullName() {
        return server.getName() + "." + getLocalName();
    }

    @Override
    public String getGlobalName() {
        return getFullName();
    }

    @Override
    public String getName(Context ctx) {
        return getFullName();
    }

    @Override
    public RemoteWorld getRemoteWorld() {
        return server.getRemoteWorld(worldName);
    }

    @Override
    public RemoteServer getRemoteServer() {
        return server;
    }

    @Override
    public boolean isSameServer() {
        return false;
    }

    @Override
    protected void attach(GateImpl origin) {
        if (! (origin instanceof LocalGateImpl)) return;
        server.sendGateAttach(this, (LocalGateImpl)origin);
    }

    @Override
    protected void detach(GateImpl origin) {
        if (! (origin instanceof LocalGateImpl)) return;
        server.sendGateDetach(this, (LocalGateImpl)origin);
    }

    @Override
    public boolean getHidden() {
        return hidden;
    }

}
