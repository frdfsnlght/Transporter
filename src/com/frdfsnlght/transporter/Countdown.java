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

import com.frdfsnlght.transporter.api.ReservationException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class Countdown {

    private Player player;
    private LocalGateImpl gate;
    private int timeRemaining;
    private int timer;

    public Countdown(Player player, LocalGateImpl gate) {
        this.player = player;
        this.gate = gate;
    }

    public Player getPlayer() {
        return player;
    }

    public LocalGateImpl getGate() {
        return gate;
    }

    public void start() {
        ReservationImpl.addCountdown(this);
        timeRemaining = gate.getCountdown();
        timer = 0;
        String format = expand(gate.getCountdownFormat());
        if (format != null)
            player.sendMessage(format);
        schedule();
    }

    public void cancel() {
        if (timer != 0)
            Utils.cancelTask(timer);
        ReservationImpl.removeCountdown(this);
        String format = expand(gate.getCountdownCancelFormat());
        if (format != null)
            player.sendMessage(format);
    }

    public void tick() {
        timeRemaining -= gate.getCountdownInterval();
        if (timeRemaining <= 0) {
            ReservationImpl.removeCountdown(this);
            Context ctx = new Context(player);
            try {
                ReservationImpl r = new ReservationImpl(player, gate);
                r.depart();
            } catch (ReservationException re) {
                ctx.warnLog(re.getMessage());
            }
            return;
        }

        String format = expand(gate.getCountdownIntervalFormat());
        if (format != null)
            player.sendMessage(format);
        schedule();
    }

    private void schedule() {
        timer = Utils.fireDelayed(new Runnable() {
            @Override
            public void run() {
                tick();
            }
        }, gate.getCountdownInterval());
    }

    private String expand(String format) {
        Map<String,String> tokens = new HashMap<String,String>();
        tokens.put("time", NumberFormat.getNumberInstance().format((double)timeRemaining / 1000.0));
        tokens.put("fromGate", gate.getName());
        tokens.put("fromWorld", gate.getWorld().getName());
        return Chat.colorize(Utils.expandFormat(format, tokens));
    }

}
