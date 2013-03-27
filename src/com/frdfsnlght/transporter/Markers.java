/*
 * Copyright 2011 Thomas A. Bennedum <tab@bennedum.org>.
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
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

/**
 *
 * @author Thomas A. Bennedum <tab@bennedum.org>
 */
public final class Markers {

    private static final String DYNMAP_MARKERSET_ID = "transporter-markers";
    private static final String DYNMAP_MARKERICON_ID = "transporter-marker";

    private static DynmapAPI dynmapPlugin = null;

    public static boolean dynmapAvailable() {
        if (! Config.getUseDynmap()) return false;
        if (dynmapPlugin != null) return true;
        Plugin p = Global.plugin.getServer().getPluginManager().getPlugin("dynmap");
        if ((p == null) || (! p.isEnabled())) return false;
        dynmapPlugin = (org.dynmap.DynmapAPI)p;
        return true;
    }

    public static void update() {
        exportJSON();

        if (dynmapAvailable()) {
            MarkerAPI api = dynmapPlugin.getMarkerAPI();
            MarkerIcon markerIcon = api.getMarkerIcon(DYNMAP_MARKERICON_ID);
            if (markerIcon == null) {
                markerIcon = api.createMarkerIcon(DYNMAP_MARKERICON_ID, DYNMAP_MARKERICON_ID, Markers.class.getResourceAsStream("/resources/images/transporter-tiny.png"));
                if (markerIcon != null)
                    Utils.debug("dynmap marker icon created");
            }
            MarkerSet markerSet = api.getMarkerSet(DYNMAP_MARKERSET_ID);
            if (markerSet == null) {
                markerSet = api.createMarkerSet(DYNMAP_MARKERSET_ID, Config.getDynmapMarkerSetLabel(), null, false);
                if (markerSet != null)
                    Utils.debug("dynmap marker set created");
            }
            Map<String,Marker> currentMarkers = new HashMap<String,Marker>();
            for (Marker marker : markerSet.getMarkers())
                currentMarkers.put(marker.getMarkerID(), marker);

            // gates
            for (Iterator<LocalGateImpl> i = Gates.getLocalGates().iterator(); i.hasNext();) {
                LocalGateImpl gate = i.next();
                Vector center = gate.getCenter();
                if (center == null) continue;
                Marker marker = currentMarkers.remove(gate.getFullName());

                String format = gate.getMarkerFormat();
                if (format == null) {
                    if (marker != null)
                        marker.deleteMarker();
                    continue;
                }

                format = format.replace("\\n", "\n");
                format = format.replace("%name%", gate.getName());
                format = format.replace("%type%", gate.getType().toString());
                format = format.replace("%creator%", gate.getCreatorName());
                format = format.replace("%sendLocal%", Economy.format(gate.getSendLocalCost()));
                format = format.replace("%sendWorld%", Economy.format(gate.getSendWorldCost()));
                format = format.replace("%sendServer%", Economy.format(gate.getSendServerCost()));
                format = format.replace("%receiveLocal%", Economy.format(gate.getReceiveLocalCost()));
                format = format.replace("%receiveWorld%", Economy.format(gate.getReceiveWorldCost()));
                format = format.replace("%receiveServer%", Economy.format(gate.getReceiveServerCost()));

                if (format.trim().isEmpty()) {
                    if (marker != null)
                        marker.deleteMarker();
                    continue;
                }

                if (marker == null) {
                    marker = markerSet.createMarker(gate.getFullName(), format, gate.getWorld().getName(), center.getX(), center.getY(), center.getZ(), markerIcon, false);
                    if (marker != null)
                        Utils.debug("marker for %s created", gate.getFullName());
                } else {
                    Utils.debug("reusing marker for %s", gate.getFullName());
                    marker.setLabel(format);
                }
            }

            for (Marker marker : currentMarkers.values()) {
                marker.deleteMarker();
                Utils.debug("marker for %s created", marker.getMarkerID());
            }
        }

    }

    private static void exportJSON() {
        String fileName = Config.getExportedGatesFile();
        if (fileName == null) return;
        File file = new File(fileName);
        if (! file.isAbsolute())
            file = new File(Global.plugin.getDataFolder(), fileName);

        Utils.debug("exporting endpoints to %s", file.getAbsolutePath());
        try {
            PrintStream out = new PrintStream(file);
            out.println("[");

            // gates
            for (Iterator<LocalGateImpl> i = Gates.getLocalGates().iterator(); i.hasNext();) {
                LocalGateImpl gate = i.next();
                Vector center = gate.getCenter();
                out.println("  {");
                out.println("    \"name\": \"" + gate.getName() + "\",");
                out.println("    \"world\": \"" + gate.getWorld().getName() + "\",");
                out.println("    \"type\": \"" + gate.getType().toString() + "\",");
                out.println("    \"links\": [");
                for (Iterator<String> li = gate.getLinks().iterator(); li.hasNext();) {
                    out.print("      \"" + li.next() + "\"");
                    out.println(li.hasNext() ? "," : "");
                }
                out.println("    ],");
                out.println("    \"x\": " + center.getX() + ",");
                out.println("    \"y\": " + center.getY() + ",");
                out.println("    \"z\": " + center.getZ() + ",");
                if (Economy.isAvailable()) {
                    if (gate.getLinkLocal()) {
                        out.println("    \"onWorldSend\": \"" + Economy.format(gate.getSendLocalCost()) + "\",");
                        out.println("    \"onWorldReceive\": \"" + Economy.format(gate.getReceiveLocalCost()) + "\",");
                    }
                    if (gate.getLinkWorld()) {
                        out.println("    \"offWorldSend\": \"" + Economy.format(gate.getSendWorldCost()) + "\",");
                        out.println("    \"offWorldReceive\": \"" + Economy.format(gate.getReceiveWorldCost()) + "\",");
                    }
                    if (gate.getLinkServer()) {
                        out.println("    \"offServerSend\": \"" + Economy.format(gate.getSendServerCost()) + "\",");
                        out.println("    \"offServerReceive\": \"" + Economy.format(gate.getReceiveServerCost()) + "\",");
                    }
                }
                out.println("    \"creator\": \"" + gate.getCreatorName() + "\"");
                out.println("  }" + (i.hasNext() ? "," : ""));
            }

            out.println("]");
            out.close();
        } catch (IOException ioe) {
            Utils.warning("unable to write %s: %s", file.getAbsolutePath(), ioe.getMessage());
        }
    }


}
