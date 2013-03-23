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
package com.frdfsnlght.transporter.compatibility;

import com.frdfsnlght.transporter.Global;
import com.frdfsnlght.transporter.Utils;
import com.frdfsnlght.transporter.compatibility.api.CompatibilityProvider;
import com.frdfsnlght.transporter.compatibility.api.TypeMap;

import java.lang.reflect.Constructor;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */

public class Compatibility {

    public static boolean setup() {
        final String packageName = Global.plugin.getServer().getClass().getPackage().getName();
        String cbversion = packageName.substring(packageName.lastIndexOf('.') + 1);
        if (cbversion.equals("craftbukkit")) {
            cbversion = "pre";
        }
        try {
            final Class<?> clazz = Class.forName("com.frdfsnlght.transporter.compatibility.v" + cbversion);
            if (Compatibility.class.isAssignableFrom(clazz)) {
            	Global.compatibility = (CompatibilityProvider) clazz.getConstructor().newInstance();
            } else {
                throw new Exception("Nope");
            }
        } catch (final Exception e) {
        	Global.plugin.getLogger().severe("Could not find support for this CraftBukkit version.");
            return false;
        }
        Global.plugin.getLogger().info("Loading support for " + (cbversion.equals("pre") ? "1.4.5-pre-RB" : cbversion));
        return true;
    }
}

