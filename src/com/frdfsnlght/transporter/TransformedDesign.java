/*
 * Copyright 2013 frdfsnlght <frdfsnlght@gmail.com>.
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class TransformedDesign {

    private Design design;
    private Location location;
    private BlockFace direction;
    private int nextIndex = 0;
    private List<GateBlock> gateBlocks = new ArrayList<GateBlock>();
    private Map<DesignBlockDetail,DesignBlockDetail> detailCache = new HashMap<DesignBlockDetail,DesignBlockDetail>();

    public TransformedDesign(Design design, Location location, BlockFace direction) {
        this.design = design;
        this.location = location;
        this.direction = direction;
    }

    public void reset() {
        nextIndex = 0;
    }

    public void clear() {
        reset();
        gateBlocks.clear();
        detailCache.clear();
    }

    public boolean hasMoreBlocks() {
        return nextIndex < design.getBlocks().size();
    }

    public GateBlock nextBlock() {
        //Utils.debug("getting block %s of %s", nextIndex + 1, design.getBlocks().size());
        if (nextIndex == design.getBlocks().size()) return null;
        DesignBlock db = design.getBlocks().get(nextIndex++);
        DesignBlockDetail detail;
        if (detailCache.containsKey(db.getDetail())) {
            detail = detailCache.get(db.getDetail());
        } else {
            detail = new DesignBlockDetail(db.getDetail(), direction);
            detailCache.put(db.getDetail(), detail);
        }
        GateBlock gb = new GateBlock(detail, rotate(location, direction, db.getX(), db.getY(), db.getZ()));
        //Utils.debug("return %s block at %s", gb.getDetail().getBuildBlock().getMaterial(), Utils.blockCoords(gb.getLocation()));
        gateBlocks.add(gb);
        return gb;
    }

    public List<GateBlock> getBlocks() {
        while (hasMoreBlocks()) nextBlock();
        return gateBlocks;
    }

    private Location rotate(Location loc, BlockFace facing, int offX, int offY, int offZ) {
        switch (facing) {
            case NORTH:
                return new Location(loc.getWorld(),
                        loc.getBlockX() + offX,
                        loc.getBlockY() + offY,
                        loc.getBlockZ() + offZ);
            case EAST:
                return new Location(loc.getWorld(),
                        loc.getBlockX() - offZ,
                        loc.getBlockY() + offY,
                        loc.getBlockZ() + offX);
            case SOUTH:
                return new Location(loc.getWorld(),
                        loc.getBlockX() - offX,
                        loc.getBlockY() + offY,
                        loc.getBlockZ() - offZ);
            case WEST:
                return new Location(loc.getWorld(),
                        loc.getBlockX() + offZ,
                        loc.getBlockY() + offY,
                        loc.getBlockZ() - offX);
//            case NORTH:
//                return new Location(loc.getWorld(),
//                        loc.getBlockX() - offZ,
//                        loc.getBlockY() + offY,
//                        loc.getBlockZ() + offX);
//            case EAST:
//                return new Location(loc.getWorld(),
//                        loc.getBlockX() - offX,
//                        loc.getBlockY() + offY,
//                        loc.getBlockZ() - offZ);
//            case SOUTH:
//                return new Location(loc.getWorld(),
//                        loc.getBlockX() + offZ,
//                        loc.getBlockY() + offY,
//                        loc.getBlockZ() - offX);
//            case WEST:
//                return new Location(loc.getWorld(),
//                        loc.getBlockX() + offX,
//                        loc.getBlockY() + offY,
//                        loc.getBlockZ() + offZ);
        }
        return null;
    }

}
