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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import com.frdfsnlght.transporter.GateMap.Bounds;
import com.frdfsnlght.transporter.GateMap.Point;
import com.frdfsnlght.transporter.GateMap.Volume;
import com.frdfsnlght.transporter.api.ExpandDirection;
import com.frdfsnlght.transporter.api.GateException;
import com.frdfsnlght.transporter.api.GateType;
import com.frdfsnlght.transporter.api.LocalAreaGate;
import com.frdfsnlght.transporter.api.SpawnDirection;
import com.frdfsnlght.transporter.api.SpawnSearch;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class LocalAreaGateImpl extends LocalGateImpl implements LocalAreaGate {

    private static final Set<String> OPTIONS = new HashSet<String>(LocalGateImpl.BASEOPTIONS);

    static {
        OPTIONS.add("p1");
        OPTIONS.add("p2");
        OPTIONS.add("spawnDirection");
        OPTIONS.add("spawnAir");
        OPTIONS.add("spawnSolid");
        OPTIONS.add("spawnLiquid");
        OPTIONS.add("spawnSearch");
        OPTIONS.add("box");
        OPTIONS.add("boxMaterial");
    }

    private Location p1;
    private Location p2;
    private SpawnDirection spawnDirection;
    private boolean spawnAir;
    private boolean spawnSolid;
    private boolean spawnLiquid;
    private SpawnSearch spawnSearch;
    private boolean box;
    private Material boxMaterial;

    private List<SavedBlock> boxBlocks = null;


    // creation from file
    public LocalAreaGateImpl(World world, TypeMap conf) throws GateException {
        super(world, conf);
        options = new Options(this, OPTIONS, "trp.gate", this);

        try {
            p1 = parseLocation(conf, "p1");
        } catch (GateException ge) {
            throw new GateException("p1: %s", ge.getMessage());
        }
        try {
            p2 = parseLocation(conf, "p2");
        } catch (GateException ge) {
            throw new GateException("p2: %s", ge.getMessage());
        }

        try {
            spawnDirection = Utils.valueOf(SpawnDirection.class, conf.getString("spawnDirection", "PLAYER"));
        } catch (IllegalArgumentException iae) {
            throw new GateException(iae.getMessage() + " spawnDirection");
        }
        spawnAir = conf.getBoolean("spawnAir", false);
        spawnSolid = conf.getBoolean("spawnSolid", false);
        spawnLiquid = conf.getBoolean("spawnLiquid", false);
        try {
            spawnSearch = Utils.valueOf(SpawnSearch.class, conf.getString("spawnSearch", "DOWNUP"));
        } catch (IllegalArgumentException iae) {
            throw new GateException(iae.getMessage() + " spawnSearch");
        }
        box = conf.getBoolean("box", false);
        try {
            boxMaterial = Utils.valueOf(Material.class, conf.getString("boxMaterial", "GLASS"));
        } catch (IllegalArgumentException iae) {
            throw new GateException(iae.getMessage() + " boxMaterial");
        }

        List<TypeMap> maps = conf.getMapList("boxBlocks");
        if (maps != null) {
            boxBlocks = new ArrayList<SavedBlock>();
            for (TypeMap map : maps) {
                try {
                    SavedBlock block = new SavedBlock(map);
                    block.setWorld(world);
                    boxBlocks.add(block);
                } catch (BlockException be) {
                    throw new GateException(be.getMessage());
                }
            }
            if (boxBlocks.isEmpty()) boxBlocks = null;
        }

        calculateCenter();
        validate();
    }

    // creation in-game
    public LocalAreaGateImpl(World world, String gateName, String playerName, BlockFace direction, Location location) throws GateException {
        super(world, gateName, playerName, direction);
        options = new Options(this, OPTIONS, "trp.gate", this);

        Location l1 = new Location(world, location.getBlockX(), location.getBlockY(), location.getBlockZ());
        Location l2 = new Location(world, location.getBlockX(), location.getBlockY(), location.getBlockZ());
        setCorners(l1, l2);
        spawnDirection = SpawnDirection.PLAYER;
        spawnAir = false;
        spawnSolid = false;
        spawnLiquid = false;
        spawnSearch = SpawnSearch.DOWNUP;
        box = false;
        boxMaterial = Material.GLASS;
        boxBlocks = null;

        calculateCenter();
        validate();
        generateFile();
        dirty = true;
    }

    // Abstract implementations

    @Override
    public GateType getType() { return GateType.AREA; }

    @Override
    public Location getSpawnLocation(Location fromLocation, BlockFace fromDirection) {
        Random random = new Random();
        Bounds bounds = new Bounds(p1, p2);
        for (int tries = 0; tries < 1000; tries++) {
            // find location of feet
            int x = bounds.min.x + ((bounds.sizeX() > 0) ? random.nextInt(bounds.sizeX()) : 0);
            int y = bounds.min.y + ((bounds.sizeY() > 0) ? random.nextInt(bounds.sizeY()) : 0);
            int z = bounds.min.z + ((bounds.sizeZ() > 0) ? random.nextInt(bounds.sizeZ()) : 0);
            // adjust down to make room for head
            if (y > bounds.max.y) y--;
            else if (y < bounds.min.y) y++;

            SpawnSearch dir = spawnSearch;
            if (dir == SpawnSearch.DOWNUP) dir = SpawnSearch.DOWN;
            else if (dir == SpawnSearch.UPDOWN) dir = SpawnSearch.UP;
            boolean goodLocation = false;
            boolean endLoop = false;

            while (! endLoop) {
                // can the location hold the player?
                Material footBlock = world.getBlockAt(x, y, z).getType();
                Material headBlock = world.getBlockAt(x, y + 1, z).getType();

                if ((footBlock == Material.AIR) && (headBlock == Material.AIR))
                    goodLocation = true;
                else if (spawnSolid && (isSolid(footBlock) || isSolid(headBlock)))
                    goodLocation = true;
                else if (spawnLiquid &&
                        (
                            (isLiquid(footBlock) && (! isSolid(headBlock))) ||
                            (isLiquid(headBlock) && (! isSolid(footBlock)))
                        ))
                    goodLocation = true;

                // check for air block under player
                if (goodLocation && (! spawnAir)) {
                    if ((y == 0) || (world.getBlockAt(x, y - 1, z).getType() == Material.AIR))
                        goodLocation = false;
                }

                if (goodLocation)
                    endLoop = true;
                else {
                    switch (dir) {
                        case UP:
                            y++;
                            if (y > bounds.max.y) {
                                if (spawnSearch == SpawnSearch.UPDOWN) {
                                    dir = SpawnSearch.DOWN;
                                    y = y - 2;
                                } else
                                    endLoop = true;
                            }
                            break;
                        case DOWN:
                            y--;
                            if (y < bounds.min.y) {
                                if (spawnSearch == SpawnSearch.DOWNUP) {
                                    dir = SpawnSearch.UP;
                                    y = y + 2;
                                } else
                                    endLoop = true;
                            }
                    }
                }
            }
            if (goodLocation) {
                Location toLocation = new Location(world, (double)x + 0.5, y, (double)z + 0.5);
                toLocation.setYaw(spawnDirection.calculateYaw(fromLocation.getYaw(), fromDirection, getDirection()));
                toLocation.setPitch(fromLocation.getPitch());
                return toLocation;
            }
        }
        Utils.warning("Unable to find a suitable spawnlocation for gate '%s'!", getLocalName());
        return p1;
    }

    @Override
    public void onSend(Entity entity) {}

    @Override
    public void onReceive(Entity entity) {}

    @Override
    public void onProtect(Location loc) {}

    @Override
    public void rebuild() {
        if (box) showBox();
    }

    @Override
    protected void onValidate() throws GateException {}

    @Override
    protected void onAdd() {
        if (portalOpen)
            Gates.addPortalVolume(getPortalVolume());
        if (protect && box)
            Gates.addProtectionVolume(getBoxVolume());
    }

    @Override
    protected void onRemove() {
        Gates.removePortalVolume(this);
    }

    @Override
    protected void onDestroy(boolean unbuild) {
        Gates.removePortalVolume(this);
        if (unbuild && box) hideBox();
    }

    @Override
    protected void onOpen() {
        Gates.addPortalVolume(getPortalVolume());
    }

    @Override
    protected void onClose() {
        Gates.removePortalVolume(this);
    }

    @Override
    protected void onNameChanged() {}

    @Override
    protected void onDestinationChanged() {}

    @Override
    protected void onSave(TypeMap conf) {
        conf.set("p1", encodeLocation(p1));
        conf.set("p2", encodeLocation(p2));
        conf.set("spawnDirection", spawnDirection.toString());
        conf.set("spawnAir", spawnAir);
        conf.set("spawnSolid", spawnSolid);
        conf.set("spawnLiquid", spawnLiquid);
        conf.set("spawnSearch", spawnSearch.toString());
        conf.set("box", box);
        conf.set("boxMaterial", boxMaterial.toString());

        if (boxBlocks != null) {
            List<Object> node = new ArrayList<Object>();
            for (SavedBlock block : boxBlocks)
                node.add(block.encode());
            conf.set("boxBlocks", node);
        }
    }

    @Override
    protected void calculateCenter() {
        double cx = (p1.getBlockX() + p2.getBlockX()) / 2;
        double cy = (p1.getBlockY() + p2.getBlockY()) / 2;
        double cz = (p1.getBlockZ() + p2.getBlockZ()) / 2;
        center = new Vector(cx, cy, cz);
    }




    // Overrides

    @Override
    public String toString() {
        return "LocalAreaGate[" + getLocalName() + "]";
    }

    // Custom methods

    @Override
    public void resize(int num, ExpandDirection dir) {
        Bounds bounds = new Bounds(p1, p2);
        Location min = bounds.min.toLocation(world);
        Location max = bounds.max.toLocation(world);
        switch (dir) {
            case UP: max.add(0, num, 0); break;
            case DOWN: min.subtract(0, num, 0); break;
            case NORTH: min.subtract(0, 0, num); break;
            case SOUTH: max.add(0, 0, num); break;
            case EAST: max.add(num, 0, 0); break;
            case WEST: min.subtract(num, 0, 0); break;
            case ALL:
                min.subtract(num, num, num);
                max.add(num, num, num);
                break;
        }
        if (min.getBlockY() < 0) min.setY(0);
        if (max.getBlockX() > 255) max.setY(255);
        Utils.debug("old corners: %s %s", Utils.blockCoords(p1), Utils.blockCoords(p2));
        setCorners(min, max);
        Utils.debug("new corners: %s %s", Utils.blockCoords(p1), Utils.blockCoords(p2));
    }

    private void setCorners(Location l1, Location l2) {
        p1 = l1;
        p2 = l2;
        if (box) showBox();
        if (portalOpen) {
            Gates.removePortalVolume(this);
            Gates.addPortalVolume(getPortalVolume());
        }
    }

    private Location parseLocation(TypeMap conf, String name) throws GateException {
        String v = conf.getString(name);
        if (v == null)
            throw new GateException("missing");
        try {
            return parseLocation(v);
        } catch (IllegalArgumentException iae) {
            throw new GateException(iae.getMessage());
        }
    }

    private Location parseLocation(String locStr) {
        String[] ords = locStr.split(",");
        if (ords.length != 3)
            throw new IllegalArgumentException("invalid coordinate set");
        try {
            int x = Integer.parseInt(ords[0]);
            int y = Integer.parseInt(ords[1]);
            int z = Integer.parseInt(ords[2]);
            if ((y < 0) || (y > 255))
                throw new IllegalArgumentException("invalid y-ordinate");
            return new Location(world, x, y, z);
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("invalid ordinate number");
        }
    }

    private String encodeLocation(Location l) {
        return l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ();
    }

    private Volume getPortalVolume() {
        Volume vol = new Volume(this);
        vol.setBounds(new Point(p1), new Point(p2));
        return vol;
    }

    private boolean isSolid(Material m) {
        return (! isLiquid(m)) &&
               (m != Material.AIR) &&
               (m != Material.WEB);
    }

    private boolean isLiquid(Material m) {
        return (m == Material.WATER) ||
               (m == Material.STATIONARY_WATER) ||
               (m == Material.LAVA) ||
               (m == Material.STATIONARY_LAVA);
    }

    private void hideBox() {
        if (boxBlocks == null) return;
        for (SavedBlock b : boxBlocks)
            b.restore();
        boxBlocks = null;
    }

    private void showBox() {
        if (boxBlocks != null) hideBox();
        boxBlocks = new ArrayList<SavedBlock>();
        Bounds bounds = new Bounds(p1, p2);
        Location l = bounds.min.toLocation(world);
        //Set<Location> hitLocations = new HashSet<Location>();

        for (int x = bounds.min.x; x <= bounds.max.x; x++) {
            l.setX(x);
            for (int y = bounds.min.y; y <= bounds.max.y; y++) {
                l.setY(y);
                for (int z = bounds.min.z; z <= bounds.max.z; z++) {
                    l.setZ(z);
                    if ((x == bounds.min.x) || (x == bounds.max.x) ||
                        (y == bounds.min.y) || (y == bounds.max.y) ||
                        (z == bounds.min.z) || (z == bounds.max.z)) {
                        boxBlocks.add(new SavedBlock(l));
                        l.getBlock().setType(boxMaterial);
                    }
                }
            }
        }
    }

    private Volume getBoxVolume() {
        Volume vol = new Volume(this);
        Bounds bounds = new Bounds(p1, p2);
        Point p = bounds.min.clone();
        for (int x = bounds.min.x; x <= bounds.max.x; x++) {
            p.x = x;
            for (int y = bounds.min.y; y <= bounds.max.y; y++) {
                p.y = y;
                for (int z = bounds.min.z; z <= bounds.max.z; z++) {
                    p.z = z;
                    if ((x == bounds.min.x) || (x == bounds.max.x) ||
                        (y == bounds.min.y) || (y == bounds.max.y) ||
                        (z == bounds.min.z) || (z == bounds.max.z)) {
                        vol.addPoint(p.clone());
                    }
                }
            }
        }
        return vol;
    }

    /* Begin options */

    @Override
    public String getP1() {
        return encodeLocation(p1);
    }

    @Override
    public void setP1(String s) {
        setCorners(parseLocation(s), p2);
        dirty = true;
    }

    @Override
    public Location getP1Location() {
        return p1.clone();
    }

    @Override
    public void setP1Location(Location l) {
        setCorners(new Location(world, l.getBlockX(), l.getBlockY(), l.getBlockZ()), p2);
        dirty = true;
    }

    @Override
    public String getP2() {
        return encodeLocation(p2);
    }

    @Override
    public void setP2(String s) {
        setCorners(p1, parseLocation(s));
        dirty = true;
    }

    @Override
    public Location getP2Location() {
        return p2.clone();
    }

    @Override
    public void setP2Location(Location l) {
        setCorners(p1, new Location(world, l.getBlockX(), l.getBlockY(), l.getBlockZ()));
        dirty = true;
    }

    @Override
    public SpawnDirection getSpawnDirection() {
        return spawnDirection;
    }

    @Override
    public void setSpawnDirection(SpawnDirection dir) {
        spawnDirection = dir;
        dirty = true;
    }

    @Override
    public boolean getSpawnSolid() {
        return spawnSolid;
    }

    @Override
    public void setSpawnAir(boolean b) {
        spawnAir = b;
        dirty = true;
    }

    @Override
    public boolean getSpawnAir() {
        return spawnAir;
    }

    @Override
    public void setSpawnSolid(boolean b) {
        spawnSolid = b;
        dirty = true;
    }

    @Override
    public boolean getSpawnLiquid() {
        return spawnLiquid;
    }

    @Override
    public void setSpawnLiquid(boolean b) {
        spawnLiquid = b;
        dirty = true;
    }

    @Override
    public SpawnSearch getSpawnSearch() {
        return spawnSearch;
    }

    @Override
    public void setSpawnSearch(SpawnSearch s) {
        spawnSearch = s;
        dirty = true;
    }

    @Override
    public boolean getBox() {
        return box;
    }

    @Override
    public void setBox(boolean b) {
        if (box) hideBox();
        box = b;
        dirty = true;
        setCorners(p1, p2);
        if (protect && box)
            Gates.addProtectionVolume(getBoxVolume());
        else if (protect)
            Gates.removeProtectionVolume(this);
    }

    @Override
    public Material getBoxMaterial() {
        return boxMaterial;
    }

    @Override
    public void setBoxMaterial(Material m) {
        boxMaterial = m;
        if (box) showBox();
    }

    @Override
    public void onOptionSet(Context ctx, String name, String value) {
        super.onOptionSet(ctx, name, value);
        if (name.equals("protect")) {
            if (protect && box)
                Gates.addProtectionVolume(getBoxVolume());
            else
                Gates.removeProtectionVolume(this);
        }
    }

    /* End options */

}
