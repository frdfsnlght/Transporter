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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.World;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class GateMap {

    private Map<World,WorldMap> worlds = new HashMap<World,WorldMap>();

    public GateMap() {}

    public void put(Volume volume) {
        World world = volume.getWorld();
        if (! worlds.containsKey(world))
            worlds.put(world, new WorldMap());
        worlds.get(world).add(volume);
    }

    public LocalGateImpl getGate(Location location) {
        World world = location.getWorld();
        WorldMap wmap = worlds.get(world);
        if (wmap == null) return null;
        VolumeNode node = wmap.getNode(location);
        if (node == null) return null;
        return node.getGate(location);
    }

    public void removeGate(LocalGateImpl gate) {
        World world = gate.getWorld();
        if (worlds.containsKey(world))
            worlds.get(world).removeGate(gate);
    }

    public void removeWorld(World world) {
        worlds.remove(world);
    }

    public int size() {
        int count = 0;
        for (WorldMap wmap : worlds.values())
            count+= wmap.size();
        return count;
    }

    public int nodeCount() {
        int count = 0;
        for (WorldMap wmap : worlds.values())
            count+= wmap.nodeCount();
        return count;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("GateMap[");
        sb.append(worlds.size()).append(" worlds: ");
        for (World world : worlds.keySet()) {
            sb.append(worlds.get(world).toString());
            sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    public String toString(World world) {
        StringBuilder sb = new StringBuilder();
        sb.append("GateMap.").append(world.getName()).append("[");
        WorldMap wmap = worlds.get(world);
        if (wmap != null)
            sb.append(wmap.toString());
        sb.append("]");
        return sb.toString();
    }

    public static final class Point {
        int x, y, z;
        public Point() {}
        public Point(int x, int y, int z) {
            this.x = x; this.y = y; this.z = z;
        }
        public Point(Point p) {
            this.x = p.x;
            this.y = p.y;
            this.z = p.z;
        }
        public Point(Location loc) {
            x = loc.getBlockX();
            y = loc.getBlockY();
            z = loc.getBlockZ();
        }
        public Location toLocation(World world) {
            return new Location(world, x, y, z);
        }
        @Override
        public boolean equals(Object o) {
            if (! (o instanceof Point)) return false;
            Point p = (Point)o;
            return (p.x == x) && (p.y == y) && (p.z == z);
        }
        @Override
        public int hashCode() {
            return x + y + x;
        }
        @Override
        public String toString() {
            return "(" + x + "," + y + "," + z + ")";
        }
        @Override
        public Point clone() {
            return new Point(this);
        }
    }

    public static final class Bounds {
        Point min = new Point();
        Point max = new Point();
        public Bounds() {};
        public Bounds(Point p1, Point p2) {
            set(p1);
            expand(p2);
        }
        public Bounds(Location l1, Location l2) {
            this(new Point(l1), new Point(l2));
        }
        public void set(Point p) {
            min.x = max.x = p.x;
            min.y = max.y = p.y;
            min.z = max.z = p.z;
        }
        public void expand(Point p) {
            min.x = Math.min(min.x, p.x);
            min.y = Math.min(min.y, p.y);
            min.z = Math.min(min.z, p.z);
            max.x = Math.max(max.x, p.x);
            max.y = Math.max(max.y, p.y);
            max.z = Math.max(max.z, p.z);
        }
        public void expand(Bounds b) {
            expand(b.min);
            expand(b.max);
        }
        public boolean contains(Location loc) {
            return (loc.getBlockX() >= min.x) && (loc.getBlockX() <= max.x) &&
                   (loc.getBlockZ() >= min.z) && (loc.getBlockZ() <= max.z) &&
                   (loc.getBlockY() >= min.y) && (loc.getBlockY() <= max.y);
        }
        public int sizeX() { return max.x - min.x; }
        public int sizeY() { return max.y - min.y; }
        public int sizeZ() { return max.z - min.z; }
        public Bounds trim(Point p, int quad) {
            switch (quad) {
                case 0:
                    if ((min.x >= p.x) || (min.z >= p.z)) return null;
                    return new Bounds(min, new Point(Math.min(max.x, p.x - 1), max.y, Math.min(max.z, p.z - 1)));
                case 1:
                    if ((min.x >= p.x) || (max.z < p.z)) return null;
                    return new Bounds(new Point(min.x, min.y, max.z), new Point(Math.min(max.x, p.x - 1), max.y, Math.max(min.z, p.z)));
                case 2:
                    if ((max.x < p.x) || (min.z >= p.z)) return null;
                    return new Bounds(new Point(Math.max(min.x, p.x), min.y, Math.min(max.z, p.z - 1)), new Point(max.x, max.y, min.z));
                case 3:
                    if ((max.x < p.x) || (max.z < p.z)) return null;
                    return new Bounds(max, new Point(Math.max(min.x, p.x), min.y, Math.max(min.z, p.z)));
                default:
                    return null;
            }
        }
        @Override
        public String toString() {
            return "[" + min + "," + max + "]";
        }
    }

    private class WorldMap {

        private Set<Volume> volumes = new HashSet<Volume>();
        private VolumeNode root = null;

        void add(Volume volume) {
            volumes.add(volume);
            recalculate();
        }

        VolumeNode getNode(Location loc) {
            if (root == null) return null;
            return root.getNode(loc);
        }

        void removeGate(LocalGateImpl gate) {
            for (Iterator<Volume> i = volumes.iterator(); i.hasNext(); )
                if (i.next().getGate() == gate)
                    i.remove();
            recalculate();
        }

        int size() {
            return volumes.size();
        }

        int nodeCount() {
            if (root == null) return 0;
            return root.nodeCount();
        }

        private void recalculate() {
//            Utils.debug("!!!! recalculate worldmap !!!!");
//            Utils.debug(toString());

            if (root != null) root.destroy();
            if ((volumes == null) || volumes.isEmpty()) return;
            root = new VolumeNode(null, volumes);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("WorldMap[");
            sb.append(volumes.size()).append(" volumes:\n");
            for (Volume volume : volumes) {
                sb.append(volume.toString());
                sb.append(",\n");
            }
            sb.append("]");
            return sb.toString();
        }

    }

    public static final class Volume {
        protected LocalGateImpl gate;
        protected Bounds bounds = new Bounds();
        protected Set<Point> points = null;
        public Volume(LocalGateImpl gate) {
            this.gate = gate;
        }
        public World getWorld() { return gate.getWorld(); }
        public Bounds getBounds() { return bounds; }
        public LocalGateImpl getGate() { return gate; }
        public void addPoint(Point p) {
            if (points == null) {
                points = new HashSet<Point>();
                points.add(p);
                bounds.set(p);
            } else {
                points.add(p);
                bounds.expand(p);
            }
        }
        public void setBounds(Bounds b) {
            bounds.min = b.min;
            bounds.max = b.max;
        }
        public void setBounds(Point p1, Point p2) {
            bounds.set(p1);
            bounds.expand(p2);
        }
        public boolean contains(Location loc) {
            Point lp = new Point(loc);
            if (points == null) {
                if (bounds.contains(loc)) {
                    return true;
                }
                return false;
            }
            for (Point p : points)
                if (p.equals(lp)) {
                    return true;
                }
            return false;
        }
        public Volume[] split(Point center) {
            Volume[] vols = new Volume[4];
            // check if we're completely in a quad
            if (bounds.max.x < center.x) {
                if (bounds.max.z < center.z) {
                    vols[0] = this;
                    return vols;
                }
                if (bounds.min.z >= center.z) {
                    vols[1] = this;
                    return vols;
                }
            } else if (bounds.min.x >= center.x) {
                if (bounds.max.z < center.z) {
                    vols[2] = this;
                    return vols;
                }
                if (bounds.min.z >= center.z) {
                    vols[3] = this;
                    return vols;
                }
            }

            if (points == null) {
                for (int quad = 0; quad < 4; quad++) {
                    Bounds b = bounds.trim(center, quad);
                    if (b == null) continue;
                    vols[quad] = new Volume(gate);
                    vols[quad].setBounds(b);
                }

            } else {
                for (Point p : points)
                    if (p.x < center.x) {
                        if (p.z < center.z) {
                            if (vols[0] == null) vols[0] = new Volume(gate);
                            vols[0].addPoint(p);
                        } else {
                            if (vols[1] == null) vols[1] = new Volume(gate);
                            vols[1].addPoint(p);
                        }
                    } else {
                        if (p.z < center.z) {
                            if (vols[2] == null) vols[2] = new Volume(gate);
                            vols[2].addPoint(p);
                        } else {
                            if (vols[3] == null) vols[3] = new Volume(gate);
                            vols[3].addPoint(p);
                        }
                    }
            }
            return vols;
        }
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("V[");
            if (gate != null) {
                sb.append("g=");
                sb.append(gate.getName());
                sb.append(",");
            }
            sb.append("b=");
            sb.append(bounds);
            if (points != null) {
                sb.append(",p=");
                sb.append(points.size());
            }
            sb.append("]");
            return sb.toString();
        }
    }

    private final class VolumeNode {
        private static final int LEAF_SIZE = 16;
        VolumeNode parent = null;
        Point center;
        Bounds bounds;
        VolumeNode[] children = null;
        Set<Volume> volumes = null;
        VolumeNode(VolumeNode parent, Set<Volume> volumes) {
            this.parent = parent;
            center = new Point();

            // find the center
            for (Volume vol : volumes) {
                center.x += vol.bounds.min.x + vol.bounds.max.x;
                center.y += vol.bounds.min.y + vol.bounds.max.y;
                center.z += vol.bounds.min.z + vol.bounds.max.z;
            }
            center.x = center.x / (volumes.size() * 2);
            center.y = center.y / (volumes.size() * 2);
            center.z = center.z / (volumes.size() * 2);

            bounds = new Bounds();
            bounds.set(center);
            Set<Volume> vols0 = new HashSet<Volume>();
            Set<Volume> vols1 = new HashSet<Volume>();
            Set<Volume> vols2 = new HashSet<Volume>();
            Set<Volume> vols3 = new HashSet<Volume>();

            // find bounds and create quads
            for (Volume vol : volumes) {
                bounds.expand(vol.bounds);
                Volume[] vols = vol.split(center);
                if (vols[0] != null) vols0.add(vols[0]);
                if (vols[1] != null) vols1.add(vols[1]);
                if (vols[2] != null) vols2.add(vols[2]);
                if (vols[3] != null) vols3.add(vols[3]);
            }

            // limit tree growth
            if ((bounds.sizeX() <= LEAF_SIZE) &&
                (bounds.sizeZ() <= LEAF_SIZE)) {
                this.volumes = volumes;
                return;
            }

            children = new VolumeNode[4];
            if (! vols0.isEmpty()) children[0] = new VolumeNode(this, vols0);
            if (! vols1.isEmpty()) children[1] = new VolumeNode(this, vols1);
            if (! vols2.isEmpty()) children[2] = new VolumeNode(this, vols2);
            if (! vols3.isEmpty()) children[3] = new VolumeNode(this, vols3);

        }
        void destroy() {
            volumes = null;
            parent = null;
            if (children != null) {
                for (int i = 0; i < 4; i++)
                    if (children[i] != null) children[i].destroy();
                children = null;
            }
        }
        int nodeCount() {
            if (children == null) return 1;
            int count = 1;
            for (VolumeNode child : children)
                if (child != null) count += child.nodeCount();
            return count;
        }
        LocalGateImpl getGate(Location loc) {
            if ((children != null) || (volumes == null)) return null;
            for (Volume vol : volumes)
                if (vol.contains(loc)) return vol.gate;
            return null;
        }
        VolumeNode getNode(Location loc) {
            if (! bounds.contains(loc)) return null;
            if (children == null) return this;
            if (loc.getBlockX() < center.x) {
                if (loc.getBlockZ() < center.z) {
                    if (children[0] == null) return null;
                    return children[0].getNode(loc);
                } else {
                    if (children[1] == null) return null;
                    return children[1].getNode(loc);
                }
            } else {
                if (loc.getBlockZ() < center.z) {
                    if (children[2] == null) return null;
                    return children[2].getNode(loc);
                } else {
                    if (children[3] == null) return null;
                    return children[3].getNode(loc);
                }
            }
        }
    }

}
