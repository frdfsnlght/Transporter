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

import java.util.Map;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * A block that belongs to a local gate.
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class GateBlock {

    private DesignBlockDetail detail;
    private Location location = null;

    public GateBlock(DesignBlockDetail detail, Location location) {
        this.detail = detail;
        this.location = location;
    }

    public GateBlock(TypeMap map) throws BlockException {
        detail = new DesignBlockDetail(map);

        String str;

        str = map.getString("location");
        if (str != null) {
            String[] coordsStr = str.split("\\s*,\\s*");
            if (coordsStr.length != 3)
                throw new BlockException("invalid location '%s'", str);
            int[] coords = new int[3];
            for (int i = 0; i < coords.length; i++)
                try {
                    coords[i] = Integer.parseInt(coordsStr[i]);
                } catch (NumberFormatException nfe) {
                    throw new BlockException("invalid ordinate '%s'", coordsStr[i]);
                }
            location = new Location(null, coords[0], coords[1], coords[2]);
        } else
            throw new BlockException("missing location");
    }

    public Map<String,Object> encode() {
        Map<String,Object> node = detail.encode();
        node.put("location", location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ());
        return node;
    }

    public void setWorld(World world) {
        location.setWorld(world);
    }

    public Location getLocation() {
        return location;
    }

    public DesignBlockDetail getDetail() {
        return detail;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder("GateBlock[");
        buf.append(location.getBlockX()).append(",")
           .append(location.getBlockY()).append(",")
           .append(location.getBlockZ()).append(":");
        buf.append(detail.toString());
        buf.append("]");
        return buf.toString();
    }

}
