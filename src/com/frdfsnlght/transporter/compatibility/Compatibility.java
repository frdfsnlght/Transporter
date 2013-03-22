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
import com.frdfsnlght.transporter.TypeMap;
import com.frdfsnlght.transporter.Utils;
import java.lang.reflect.Constructor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public abstract class Compatibility {

    public static boolean setup() {
        String p = Global.plugin.getServer().getClass().getPackage().getName();
        String version = p.substring(p.lastIndexOf('.') + 1);
        try {
            String classname = null;
            if (version.contains("craftbukkit"))
                classname = Compatibility.class.getPackage().getName() + ".vPreClass";
            else
                classname = Compatibility.class.getPackage().getName() + ".v" + version.substring(1) + "Class";
            Utils.info("loading compatibility class %s", classname);
            Class<?> cls = Class.forName(classname);
            Constructor<?> cons = cls.getDeclaredConstructor();
            Object obj = cons.newInstance();
            if (obj instanceof Compatibility) {
                Global.compatibility = (Compatibility)obj;
                return true;
            }
        } catch (ClassNotFoundException e) {
            Utils.severe("compatibility class not found");
        } catch (Exception e) {
        }
        return false;
    }

    abstract public void sendAllPacket201PlayerInfo(String playerName, boolean b, int i);
    abstract public void sendPlayerPacket201PlayerInfo(Player player, String playerName, boolean b, int i);
    abstract public ItemStack createItemStack(int type, int amount, short durability);
    abstract public TypeMap getItemStackTag(ItemStack stack);
    abstract public ItemStack setItemStackTag(ItemStack stack, TypeMap tag);

}
