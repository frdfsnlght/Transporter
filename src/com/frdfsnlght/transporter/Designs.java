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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Location;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class Designs {

    private static final Map<String,Design> designs = new HashMap<String,Design>();
    private static Map<String,List<SavedBlock>> buildUndos = new HashMap<String,List<SavedBlock>>();

    public static void load(Context ctx) {
        designs.clear();
        File designsFolder = new File(Global.plugin.getDataFolder(), "designs");
        for (File designFile : Utils.listYAMLFiles(designsFolder)) {
            try {
                Design design = new Design(designFile);
                if (! design.isEnabled()) continue;
                try {
                    add(design);
                    ctx.sendLog("loaded design '%s' %s", design.getName(), (design.getAttribution() == null) ? "" : design.getAttribution());
                } catch (DesignException de) {
                    ctx.warnLog("unable to load design '%s': %s", design.getName(), de.getMessage());
                }
            } catch (Throwable t) {
                ctx.warnLog("'%s' contains an invalid design: %s", designFile.getPath(), t.getMessage());
                if (Config.getDebug())
                    Utils.severe(t, "Trace:");
            }
        }
        if (isEmpty())
            ctx.sendLog("no designs loaded");
    }

    private static void add(Design design) throws DesignException {
        if (designs.containsKey(design.getName()))
            throw new DesignException("a design with the same type already exists");
        designs.put(design.getName(), design);
    }

    public static Design get(String name) {
        if (designs.containsKey(name)) return designs.get(name);
        Design design = null;
        name = name.toLowerCase();
        for (String key : designs.keySet()) {
            if (key.toLowerCase().startsWith(name)) {
                if (design == null) design = designs.get(key);
                else return null;
            }
        }
        return design;
    }

    public static List<Design> getAll() {
        return new ArrayList<Design>(designs.values());
    }

    public static boolean isEmpty() {
        return size() == 0;
    }

    public static int size() {
        return designs.size();
    }

    public static boolean undoBuild(String playerName) {
        List<SavedBlock> blocks = buildUndos.remove(playerName);
        if (blocks == null) return false;
        for (SavedBlock block : blocks)
            block.restore();
        return true;
    }

    // Attempts to match the blocks around the given location with a design.
    // The location should be the location of a design's screen.
    public static DesignMatch matchScreen(Location location) {
        for (Design design : designs.values()) {
            if (! design.isCreatable()) continue;
            DesignMatch match = design.matchScreen(location);
            if (match != null) return match;
        }
        return null;
    }

    static void setBuildUndo(String playerName, List<SavedBlock> savedBlocks) {
        if (playerName == null) return;
        buildUndos.put(playerName, savedBlocks);
    }

    public static void clearBuildUndo(String playerName) {
        buildUndos.remove(playerName);
    }

}
