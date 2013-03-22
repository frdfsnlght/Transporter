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
package com.frdfsnlght.transporter.api;

import com.frdfsnlght.transporter.Servers;

/**
 * Represents a location in a remote world on a remote server.
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class RemoteLocation {

    private RemoteServer server;
    private RemoteWorld world;
    private double x;
    private double y;
    private double z;

    /**
     * Creates a remote location for the specified server, world, and coordinates.
     *
     * @param serverName    the name of the remove server
     * @param worldName     the name of the remote world
     * @param x             the x-ordinate
     * @param y             the y-ordinate
     * @param z             the z-ordinate
     */
    public RemoteLocation(String serverName, String worldName, double x, double y, double z) {
        setRemoteServer(serverName);
        setRemoteWorld(worldName);
        setX(x);
        setY(y);
        setZ(z);
    }

    /**
     * Creates a remote location for the specified server, world, and coordinates.
     *
     * @param server    the remove server
     * @param world     the remote world
     * @param x         the x-ordinate
     * @param y         the y-ordinate
     * @param z         the z-ordinate
     */
    public RemoteLocation(RemoteServer server, RemoteWorld world, double x, double y, double z) {
        setRemoteServer(server);
        setRemoteWorld(world);
        setX(x);
        setY(y);
        setZ(z);
    }

    /**
     * Returns the remote server where this location is located.
     *
     * @return the remote server
     */
    public RemoteServer getRemoteServer() {
        return server;
    }

    private void setRemoteServer(RemoteServer server) {
        if (server == null) throw new IllegalArgumentException("server is required");
        this.server = server;
    }

    private void setRemoteServer(String serverName) {
        server = Servers.getRemoteServer(serverName);
    }

    /**
     * Returns the remote world where this location is located.
     *
     * @return the remote world
     */
    public RemoteWorld getRemoteWorld() {
        return world;
    }

    private void setRemoteWorld(RemoteWorld world) {
        if (world == null) throw new IllegalArgumentException("world is required");
        this.world = world;
    }

    private void setRemoteWorld(String worldName) {
        world = server.getRemoteWorld(worldName);
    }

    /**
     * Returns the x-ordinate of this location.
     *
     * @return the x-ordinate
     */
    public double getX() {
        return x;
    }

    private void setX(double x) {
        this.x = x;
    }

    /**
     * Returns the y-ordinate of this location.
     *
     * @return the y-ordinate
     */
    public double getY() {
        return y;
    }

    private void setY(double y) {
        this.y = y;
    }

    /**
     * Returns the z-ordinate of this location.
     *
     * @return the z-ordinate
     */
    public double getZ() {
        return z;
    }

    private void setZ(double z) {
        this.z = z;
    }

}
