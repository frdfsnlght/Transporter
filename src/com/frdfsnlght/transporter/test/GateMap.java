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
package com.frdfsnlght.transporter.test;

import java.util.Random;
import com.frdfsnlght.transporter.GateMap.Bounds;
import com.frdfsnlght.transporter.GateMap.Point;
import com.frdfsnlght.transporter.GateMap.Volume;
import com.frdfsnlght.transporter.LocalGateImpl;
import com.frdfsnlght.transporter.api.GateException;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public class GateMap {

    public static void main(String[] args) {

        //testBounds();
        //testVolume();
        //benchmark();

    }

    private static void testBounds() {
        Bounds b = new Bounds();
        b.set(new Point(-10, -10, -10));
        b.expand(new Point(10, 10, 10));
        System.out.println("Bounds: " + b);

        Point c = new Point(0, 0, 0);
        System.out.println("Trim @ " + c + ":");
        for (int q = 0; q < 4; q++)
            System.out.println(" q=" + q + ": " + b.trim(c, q));

        c = new Point(-10, 0, -10);
        System.out.println("Trim @ " + c + ":");
        for (int q = 0; q < 4; q++)
            System.out.println(" q=" + q + ": " + b.trim(c, q));

        c = new Point(-11, 0, -11);
        System.out.println("Trim @ " + c + ":");
        for (int q = 0; q < 4; q++)
            System.out.println(" q=" + q + ": " + b.trim(c, q));

        c = new Point(10, 0, 10);
        System.out.println("Trim @ " + c + ":");
        for (int q = 0; q < 4; q++)
            System.out.println(" q=" + q + ": " + b.trim(c, q));

        c = new Point(11, 0, 11);
        System.out.println("Trim @ " + c + ":");
        for (int q = 0; q < 4; q++)
            System.out.println(" q=" + q + ": " + b.trim(c, q));
    }

    private static void testVolume() {
        Volume v = new Volume(null);
        v.setBounds(new Point(-10, -10, -10), new Point(10, 10, 10));
        System.out.println("Volume: " + v);

        Point s = new Point(0, 0, 0);
        System.out.println("Split @ " + s + ":");
        Volume[] vols = v.split(s);
        for (int i = 0; i < vols.length; i++)
            System.out.println(" i=" + i + ": " + vols[i]);

        v = new Volume(null);
        for (int i = 0; i < 10; i++)
            v.addPoint(new Point(i, i, i));
        System.out.println("Volume: " + v);
        s = new Point(5, 5, 5);
        System.out.println("Split @ " + s + ":");
        vols = v.split(s);
        for (int i = 0; i < vols.length; i++)
            System.out.println(" i=" + i + ": " + vols[i]);

    }

    /*
    private static void benchmark() {
        int numGates = 1000;
        OldGateMap ogm = new OldGateMap();
        org.bennedum.transporter.GateMap gm = new org.bennedum.transporter.GateMap();
        for (int i = 0; i < numGates; i++) {
            if ((i % (numGates / 100)) == 0)
                System.out.println("Gate " + i + "...");
            try {
                TestGate gate = new TestGate(null, "Gate" + i, "tab", BlockFace.NORTH, new Random(i));
                ogm.putAll(gate.getOldGateMap());
                gm.put(gate.getVolume());
            } catch (GateException ge) {}
        }
        System.out.println("Old gate map has " + ogm.size() + " locations");
        System.out.println("New gate map has " + gm.size() + " volumes");
        System.out.println("New gate map has " + gm.nodeCount() + " nodes");

        int numTests = 100000;
        int foundCount = 0;
        Random r = new Random(1);
        long startTime = System.currentTimeMillis();
        for (int i = 0 ; i < numTests; i++) {
            if ((i % (numTests / 10)) == 0) System.out.print(".");
            int x = r.nextInt(TestGate.MAX_RANGE * 2) - TestGate.MAX_RANGE;
            int y = r.nextInt(256);
            int z = r.nextInt(TestGate.MAX_RANGE * 2) - TestGate.MAX_RANGE;
            LocalGateImpl gate = ogm.getGate(new Location(null, x, y, z));
            if (gate != null)
                foundCount++;
        }
        long endTime = System.currentTimeMillis();

        System.out.println();
        System.out.println(numTests + " tests, " + foundCount + " found, " + (endTime - startTime) + "ms");

        foundCount = 0;
        r = new Random(1);
        startTime = System.currentTimeMillis();
        for (int i = 0 ; i < numTests; i++) {
            if ((i % (numTests / 10)) == 0) System.out.print(".");
            int x = r.nextInt(TestGate.MAX_RANGE * 2) - TestGate.MAX_RANGE;
            int y = r.nextInt(256);
            int z = r.nextInt(TestGate.MAX_RANGE * 2) - TestGate.MAX_RANGE;
            LocalGateImpl gate = gm.getGate(new Location(null, x, y, z));
            if (gate != null)
                foundCount++;
        }
        endTime = System.currentTimeMillis();

        System.out.println();
        System.out.println(numTests + " tests, " + foundCount + " found, " + (endTime - startTime) + "ms");

    }
    */
}
