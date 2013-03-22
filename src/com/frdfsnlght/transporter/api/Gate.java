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

/**
 * This interface specifies methods common to all gates managed by the
 * Transporter plugin, whether local or remote.
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public interface Gate {

    /**
     * Returns the simple name of the gate.
     * <p>
     * A gate's simple name is the part of the name before any period ('.')
     * in the gate's fully qualified name (or local name or full name).
     *
     * @return the gate's simple name
     */
    public String getName();

    // worldname.simplename
    /**
     * Returns the local name of the gate.
     * <p>
     * A gate's local name is how the gate is unambiguously referred to
     * on the local server. For local gates, it is the name world where the
     * gate is located, followed by a period ('.'), followed by the gate's
     * simple name. For a remote gate, it is the same, except the server's name
     * and an additional period are prefixed. For example:
     * <ul>
     *  <li>world.Gate (for a gate on the local server)</li>
     *  <li>Server1.world.Gate (for a gate on a remote server)</li>
     * </ul>
     *
     * @return the gate's local name
     */
    public String getLocalName();

    /**
     * Returns the full name of the gate.
     * <p>
     * A gate's full name is just like it's local name except that for local
     * gates a "server name" of "local" is used. For example:
     * <ul>
     *  <li>local.world.Gate (for a gate on the local server)</li>
     *  <li>Server1.world.Gate (for a gate on a remote server)</li>
     * </ul>
     *
     * @return the gate's full name
     */
    public String getFullName();

    /**
     * Returns the type of the gate.
     *
     * @return the type of the gate
     */
    public GateType getType();

    /**
     * Returns the value of the "hidden" option.
     *
     * @return      option value.
     */
    public boolean getHidden();

}
