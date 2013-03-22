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
 * Represents a gate on a remote server.
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public interface RemoteGate extends Gate {

    /**
     * Returns the world in which this gate is located.
     *
     * @return the world in which this gate is located
     */
    public RemoteWorld getRemoteWorld();

    /**
     * Returns the server on which is gate is located.
     *
     * @return the server on which is gate is located
     */
    public RemoteServer getRemoteServer();

}
