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

import com.frdfsnlght.transporter.api.GateException;
import com.frdfsnlght.transporter.api.GateType;
import com.frdfsnlght.transporter.api.LocalServerGate;
import com.frdfsnlght.transporter.api.TransporterException;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class LocalServerGateImpl extends LocalGateImpl implements LocalServerGate {

    private static final Set<String> OPTIONS = new HashSet<String>(LocalGateImpl.BASEOPTIONS);


    // creation from file
    public LocalServerGateImpl(World world, TypeMap conf) throws GateException {
        super(world, conf);
        options = new Options(this, OPTIONS, "trp.gate", this);
        calculateCenter();
        validate();
    }

    // creation in-game
    public LocalServerGateImpl(World world, String gateName, String playerName) throws GateException {
        super(world, gateName, playerName, BlockFace.NORTH);
        options = new Options(this, OPTIONS, "trp.gate", this);
        calculateCenter();
        validate();
        generateFile();
        dirty = true;
    }

    // Abstract implementations

    @Override
    public GateType getType() { return GateType.SERVER; }

    @Override
    public Location getSpawnLocation(Location fromLocation, BlockFace fromDirection) {
        return null;
    }

    @Override
    public void onSend(Entity entity) {}

    @Override
    public void onReceive(Entity entity) {}

    @Override
    public void onProtect(Location loc) {}

    @Override
    public void rebuild() {}

    @Override
    protected void onValidate() throws GateException {}

    @Override
    protected void onAdd() {}

    @Override
    protected void onRemove() {}

    @Override
    protected void onDestroy(boolean unbuild) {}

    @Override
    protected void onOpen() {}

    @Override
    protected void onClose() {}

    @Override
    protected void onNameChanged() {}

    @Override
    protected void onDestinationChanged() {}

    @Override
    protected void onSave(TypeMap conf) {}

    @Override
    protected void calculateCenter() {
        center = null;
    }

    @Override
    public void addLink(Context ctx, String toGateName) throws TransporterException {
        throw new GateException("gate '%s' cannot accept links", getName(ctx));
    }

    @Override
    public void removeLink(Context ctx, String toGateName) throws TransporterException {
        throw new GateException("gate '%s' cannot accept links", getName(ctx));
    }

    // Overrides

    @Override
    public String toString() {
        return "LocalServerGate[" + getLocalName() + "]";
    }

    /* Begin options */

    /* End options */

}
