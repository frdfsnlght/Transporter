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

import com.frdfsnlght.transporter.api.GateType;
import com.frdfsnlght.transporter.api.RemoteServerGate;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class RemoteServerGateImpl extends RemoteGateImpl implements RemoteServerGate {

    public RemoteServerGateImpl(Server server, String name, boolean hidden) {
        super(server, name, hidden);
    }

    @Override
    public GateType getType() { return GateType.SERVER; }

    @Override
    public String toString() {
        return "RemoteServerGate[" + getFullName() + "]";
    }


}
