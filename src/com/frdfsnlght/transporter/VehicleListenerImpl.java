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

import com.frdfsnlght.transporter.api.ReservationException;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public class VehicleListenerImpl implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onVehicleMove(VehicleMoveEvent event) {
        Vehicle vehicle = event.getVehicle();
        LocalGateImpl fromGate = Gates.findGateForPortal(event.getTo());
        if (fromGate == null) {
            ReservationImpl.removeGateLock(vehicle);
            return;
        }
        if (ReservationImpl.isGateLocked(vehicle)) return;

        try {
            ReservationImpl r = new ReservationImpl(vehicle, fromGate);
            r.depart();
        } catch (ReservationException re) {
            if (vehicle.getPassenger() instanceof Player) {
                Context ctx = new Context((Player)vehicle.getPassenger());
                ctx.warnLog(re.getMessage());
            } else
                Utils.warning(re.getMessage());
        }
    }

}
