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

import org.bukkit.entity.Entity;

/**
 * Represents a reservation to teleport an entity.
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public interface Reservation {

    /**
     * Returns the local entity this reservation is for.
     *
     * @return the local entity this reservation is for
     */
    public Entity getEntity();

    /**
     * Returns the departure gate the entity left from, or null
     * if the entity didn't depart from a gate.
     *
     * @return the departure gate
     */
    public Gate getDepartureGate();

    /**
     * Returns the arrival gate the entity arrived at, or
     * null if the entity didn't arrive at a gate.
     *
     * @return the arrival gate
     */
    public Gate getArrivalGate();

}
