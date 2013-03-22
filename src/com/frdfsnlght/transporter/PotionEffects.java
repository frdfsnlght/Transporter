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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class PotionEffects {

    public static List<TypeMap> encodePotionEffects(Collection<PotionEffect> effects) {
        return encodePotionEffects(effects.toArray(new PotionEffect[] {}));
    }

    public static List<TypeMap> encodePotionEffects(PotionEffect[] effects) {
        if (effects == null) return null;
        List<TypeMap> eff = new ArrayList<TypeMap>();
        for (PotionEffect pe : effects) {
            if (pe == null) continue;
            TypeMap pm = new TypeMap();
            pm.put("type", pe.getType().toString());
            pm.put("duration", pe.getDuration());
            pm.put("amplifier", pe.getAmplifier());
            eff.add(pm);
        }
        return eff;
    }

    public static PotionEffect[] decodePotionEffects(List<TypeMap> eff) {
        if (eff == null) return null;
        PotionEffect[] effects = new PotionEffect[eff.size()];
        for (int i = 0; i < eff.size(); i++) {
            TypeMap pm = eff.get(i);
            if (pm == null)
                effects[i] = null;
            else {
                PotionEffectType type = PotionEffectType.getByName(pm.getString("type"));
                if (type == null)
                    effects[i] = null;
                else
                    effects[i] = type.createEffect(pm.getInt("duration"), pm.getInt("amplifier"));
            }
        }
        return effects;
    }


    public static String normalizePotion(String potion) {
        if (potion == null) return null;
        if (potion.equals("*")) return potion;
        potion = potion.toUpperCase();
        if (potion.equals("NONE")) return potion;
        String parts[] = potion.split(":");
        if ((parts.length == 2) || (parts.length > 3)) return null;
        PotionEffectType type = PotionEffectType.getByName(parts[0]);
        if (type == null) return null;
        potion = type.getName();
        if (parts.length > 1) {
            try {
                int duration = Integer.parseInt(parts[1]);
                potion += ":" + duration;
                int amplifier = Integer.parseInt(parts[2]);
                potion += ":" + amplifier;
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return potion;
    }

    public static boolean appendPotionList(Set<String> potions, String potion) throws PotionEffectException {
        potion = normalizePotion(potion);
        if (potion == null)
            throw new PotionEffectException("invalid potion effect");
        if (potion.equals("NONE"))
            throw new PotionEffectException("invalid potion effect");
        String potionType = potion.split(":")[0];
        for (String p : potions) {
            String pType = p.split(":")[0];
            if (potionType.equals(pType)) return false;
        }
        potions.add(potion);
        return true;
    }

    public static boolean removePotionList(Set<String> potions, String potion) throws PotionEffectException {
        potion = normalizePotion(potion);
        if (potion == null)
            throw new PotionEffectException("invalid potion effect");
        if (potion.equals("NONE"))
            throw new PotionEffectException("invalid potion effect");
        String potionType = potion.split(":")[0];
        for (String p : new HashSet<String>(potions)) {
            String pType = p.split(":")[0];
            if (potionType.equals(pType)) {
                potions.remove(p);
                return true;
            }
        }
        return false;
    }

    public static boolean potionListContains(Set<String> potions, String potion) {
        if (potion.equals("*")) return true;
        potion = normalizePotion(potion);
        String potionType = potion.split(":")[0];
        for (String p : potions) {
            String pType = p.split(":")[0];
            if (potionType.equals(pType)) return true;
        }
        return false;
    }

    public static boolean appendPotionMap(Map<String,String> potions, String fromPotion, String toPotion) throws PotionEffectException {
        fromPotion = normalizePotion(fromPotion);
        if (fromPotion == null)
            throw new PotionEffectException("invalid from potion effect");
        if (fromPotion.equals("NONE"))
            throw new PotionEffectException("invalid from potion effect");
        toPotion = normalizePotion(toPotion);
        if (toPotion == null)
            throw new PotionEffectException("invalid to potion effect");
        String potionType = fromPotion.split(":")[0];
        for (String p : potions.keySet()) {
            String pType = p.split(":")[0];
            if (potionType.equals(pType)) return false;
        }
        potions.put(fromPotion, toPotion);
        return true;
    }

    public static boolean removePotionMap(Map<String,String> potions, String fromPotion) throws PotionEffectException {
        fromPotion = normalizePotion(fromPotion);
        if (fromPotion == null)
            throw new PotionEffectException("invalid from potion effect");
        if (fromPotion.equals("NONE"))
            throw new PotionEffectException("invalid from potion effect");
        String potionType = fromPotion.split(":")[0];
        for (String p : new HashSet<String>(potions.keySet())) {
            String pType = p.split(":")[0];
            if (potionType.equals(pType)) {
                potions.remove(p);
                return true;
            }
        }
        return false;
    }

    public static PotionEffect filterPotionEffect(PotionEffect potion, Map<String,String> replace, Set<String> allowed, Set<String> banned) throws PotionEffectException {
        if (potion == null)
            throw new PotionEffectException("not allowed");
        String potionType = potion.getType().getName();
        PotionEffectType newType = potion.getType();
        int newDuration = potion.getDuration();
        int newAmplifier = potion.getAmplifier();

        if (replace != null) {
            for (String p : replace.keySet()) {
                String pType = p.split(":")[0];
                if (pType.equals(potionType) || pType.equals("*")) {
                    String[] replaceParts = replace.get(p).split(":");
                    if (replaceParts[0].equals("NONE")) return null;
                    newType = PotionEffectType.getByName(replaceParts[0]);
                    if (replaceParts.length > 1)
                        newDuration = Integer.parseInt(replaceParts[1]);
                    if (replaceParts.length > 2)
                        newAmplifier = Integer.parseInt(replaceParts[2]);
                    break;
                }
            }
        }

        if (newType == null)
            throw new PotionEffectException("not allowed");

        PotionEffect newPotion = newType.createEffect(newDuration, newAmplifier);
        if (newPotion.equals(potion)) newPotion = potion;
        String newPotionStr = newType.getName() + ":" + newDuration + ":" + newAmplifier;

        if (allowed != null) {
            if (potionListContains(allowed, newPotionStr)) return newPotion;
            throw new PotionEffectException("not allowed");
        }
        if (banned != null)
            if (potionListContains(banned, newPotionStr))
                throw new PotionEffectException("not allowed");
        return newPotion;
    }

}
