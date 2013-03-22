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
package com.frdfsnlght.transporter.api;

import com.frdfsnlght.transporter.Utils;
import org.bukkit.block.BlockFace;

/**
 * Specifies directions a {@link LocalAreaGate} will spawn and arriving player.
 * <p>
 * The final direction a player is facing when exiting a gate is determined
 * by a number of factors, spawn direction being one of them. See the wiki
 * page on Designs for more information about how the final direction is
 * determined.
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public enum SpawnDirection {

    /**
     * Spawn facing north.
     */
    NORTH,
    /**
     * Spawn facing south.
     */
    SOUTH,
    /**
     * Spawn facing east.
     */
    EAST,
    /**
     * Spawn facing west.
     */
    WEST,
    /**
     * Spawn facing north-east.
     */
    NORTH_EAST,
    /**
     * Spawn facing east-north-east.
     */
    EAST_NORTH_EAST,
    /**
     * Spawn facing north-north-east.
     */
    NORTH_NORTH_EAST,
    /**
     * Spawn facing north-west.
     */
    NORTH_WEST,
    /**
     * Spawn facing west-north-west.
     */
    WEST_NORTH_WEST,
    /**
     * Spawn facing north-north-west.
     */
    NORTH_NORTH_WEST,
    /**
     * Spawn facing south-east.
     */
    SOUTH_EAST,
    /**
     * Spawn facing east-south-east.
     */
    EAST_SOUTH_EAST,
    /**
     * Spawn facing east-south-east.
     */
    SOUTH_SOUTH_EAST,
    /**
     * Spawn facing south-south-east.
     */
    SOUTH_WEST,
    /**
     * Spawn facing west-south-west.
     */
    WEST_SOUTH_WEST,
    /**
     * Spawn facing south-west.
     */
    SOUTH_SOUTH_WEST,
    /**
     * Spawn facing a random direction.
     */
    RANDOM,
    /**
     * Spawn facing the same direction the player faced when they entered the sending gate.
     */
    PLAYER,
    /**
     * Spawn facing "forward" relative to the gate's direction.
     */
    FORWARD,
    /**
     * Spawn facing "backwards" relative to the gate's direction.
     */
    REVERSE;

    /**
     * Returns a new spawn direction based on this spawn direction and a gate's direction.
     *
     * @param direction     the gate's direction
     * @return              a new spawn direction
     */
    public SpawnDirection rotate(BlockFace direction) {
        switch (this) {
            case RANDOM:
            case PLAYER:
            case FORWARD:
            case REVERSE:
                return this;
            default:
                return SpawnDirection.fromFacing(Utils.rotate(toFacing(), direction));
        }
    }

    /**
     * Returns a yaw angle based on this spawn direction, a player's current yaw,
     * a sending gate's direction, and a destination gate's direction.
     *
     * @param playerYaw         the player's yaw angle
     * @param fromGateDirection the sending gate's direction
     * @param toGateDirection   the destination gate's direction
     * @return                  the new yaw angle
     */
    public float calculateYaw(float playerYaw, BlockFace fromGateDirection, BlockFace toGateDirection) {
        switch (this) {
            case RANDOM:
                return (float)((Math.random() * 360) - 180);
            case PLAYER:
                return playerYaw;
            case FORWARD:
            case REVERSE:

//                Utils.debug("calculate yaw for %s", this);
//                Utils.debug("playerYaw=%s", playerYaw);
//                Utils.debug("fromGateDirection=%s", fromGateDirection);
//                Utils.debug("toGateDirection=%s", toGateDirection);

                float yawDiff = playerYaw - Utils.directionToYaw(fromGateDirection);
                float newYaw = yawDiff + Utils.directionToYaw(toGateDirection);
                newYaw += (this == FORWARD) ? 0 : 180;

//                Utils.debug("yawDiff=%s", yawDiff);

                while (newYaw > 180) newYaw -= 360;
                while (newYaw <= -180) newYaw += 360;

//                Utils.debug("newYaw=%s", newYaw);

                return newYaw;
            default:
                return Utils.directionToYaw(toFacing());
        }
    }

    /**
     * Returns the equivalent <code>BlockFace</code> for this spawn direction, or
     * null if there is no equivalent.
     *
     * @return an equivalent <code>BlockFace</code>
     */
    public BlockFace toFacing() {
        try {
            return Utils.valueOf(BlockFace.class, toString());
        } catch (IllegalArgumentException iae) {
            return null;
        }
    }

    /**
     * Returns an equivalent spawn direction for the specified <code>BlockFace</code>,
     * or null if there is no equivalent.
     *
     * @param facing    the <code>BlockFace</code>
     * @return          the equivalent spawn direction
     */
    public static SpawnDirection fromFacing(BlockFace facing) {
        if (facing == null) return null;
        try {
            return Utils.valueOf(SpawnDirection.class, facing.toString());
        } catch (IllegalArgumentException iae) {
            return null;
        }
    }

}
