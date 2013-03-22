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

import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class Pins {

    private static final Map<String,String> pins = new HashMap<String,String>();

    public static boolean isValidPin(String pin) {
        return pin.length() < 20;
    }

    public static void onConfigLoad(Context ctx) {
        pins.clear();
        TypeMap map = Config.getMap("pins");
        if (map != null) {
            for (String playerName : map.getKeys())
                pins.put(playerName, map.getString(playerName));
        }
    }

    public static void onConfigSave() {
        Config.setPropertyDirect("pins", new HashMap<String,String>(pins));
    }

    public static void add(Player player, String pin) {
        pins.put(player.getName(), pin);
    }

    public static String get(Player player) {
        if (player == null) return null;
        return get(player.getName());
    }

    public static String get(String playerName) {
        return pins.get(playerName);
    }

}
