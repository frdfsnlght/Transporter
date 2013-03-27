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
package com.frdfsnlght.transporter.test;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import com.frdfsnlght.transporter.GateMap.Point;
import com.frdfsnlght.transporter.GateMap.Volume;
import com.frdfsnlght.transporter.LocalGateImpl;
import com.frdfsnlght.transporter.api.TypeMap;
import com.frdfsnlght.transporter.api.GateException;
import com.frdfsnlght.transporter.api.GateType;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class TestGate extends LocalGateImpl {

    public static final int MAX_SIZE = 20;
    public static final int MIN_SIZE = 5;
    public static final int MAX_RANGE = 5000;

    private Random random;
    private Set<Location> blocks = null;

    public TestGate(World world, String gateName, String playerName, BlockFace direction, Random r) throws GateException {
        super(world, gateName, playerName, direction);
        random = r;
    }

    @Override
    public GateType getType() { return null; }

    @Override
    public Location getSpawnLocation(Location fromLoc, BlockFace fromDirection) { return null; }

    @Override
    public void rebuild() {}

    @Override
    public void onSend(Entity entity) {}

    @Override
    public void onReceive(Entity entity) {}

    @Override
    public void onProtect(Location loc) {}

    @Override
    protected void onValidate() throws GateException {}

    @Override
    protected void onDestroy(boolean unbuild) {}

    @Override
    protected void onAdd() {}

    @Override
    protected void onRemove() {}

    @Override
    protected void onOpen() {}

    @Override
    protected void onClose() {}

    @Override
    protected void onNameChanged() {}

    @Override
    protected void onDestinationChanged() {}

    @Override
    protected void onSave(TypeMap map) {}

    @Override
    protected void calculateCenter() {}

    /*
    public OldGateMap getOldGateMap() {
        OldGateMap map = new OldGateMap();
        for (Location l : getBlocks())
            map.put(this, l);
        return map;
    }
    */

    public Volume getVolume() {
        Volume vol = new Volume(this);
        for (Location l : getBlocks())
            vol.addPoint(new Point(l));
        return vol;
    }

    private Set<Location> getBlocks() {
        if (blocks == null) {
            int cx = random.nextInt((MAX_RANGE - MAX_SIZE) * 2) - MAX_RANGE;
            int cy = random.nextInt(256 - (MAX_SIZE / 2));
            int cz = random.nextInt((MAX_RANGE - MAX_SIZE) * 2) - MAX_RANGE;
            int size = random.nextInt(MAX_SIZE - MIN_SIZE) + MIN_SIZE;
            int orient = random.nextInt(3);
            blocks = new HashSet<Location>();
            switch (orient) {
                case 0: // x-z plain
                    for (int x = cx - (size / 2); x < cx + (size / 2); x++)
                        for (int z = cz - (size / 2); z < cz + (size / 2); z++)
                            blocks.add(new Location(world, x, cy, z));
                    break;
                case 1: // x-y plain
                    for (int x = cx - (size / 2); x < cx + (size / 2); x++)
                        for (int y = cy - (size / 2); y < cy + (size / 2); y++)
                            blocks.add(new Location(world, x, y, cz));
                    break;
                case 2: // y-z plain
                    for (int y = cy - (size / 2); y < cy + (size / 2); y++)
                        for (int z = cz - (size / 2); z < cz + (size / 2); z++)
                            blocks.add(new Location(world, cx, y, z));
                    break;
            }
        }
        return blocks;
    }

}
