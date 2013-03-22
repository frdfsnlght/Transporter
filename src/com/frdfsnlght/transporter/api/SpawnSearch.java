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

/**
 * Specifies directions a {@link LocalAreaGate} will search for a
 * spawn location when a player arrives.
 * <p>
 * When a player teleports to a LocalAreaGate, the gates randomly picks
 * a location within the volume defined between its two corners. It then
 * adjusts the y-coordinate (vertical) based on the gate options that
 * allow or disallow spawning into solid or liquid blocks. This enumeration
 * specifies how the gate will search for a suitable spawn location.
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public enum SpawnSearch {

    /**
     * Search in an ascending vertical direction.
     */
    UP,
    /**
     * Search in an descending vertical direction.
     */
    DOWN,
    /**
     * Search in an ascending vertical direction, then in a descending direction.
     */
    UPDOWN,
    /**
     * Search in an descending vertical direction, then in an ascending direction.
     */
    DOWNUP;

}
