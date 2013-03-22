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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.MaterialData;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class Inventory {

    public static List<TypeMap> encodeItemStackArray(ItemStack[] isa) {
        if (isa == null) return null;
        List<TypeMap> inv = new ArrayList<TypeMap>();
        for (int slot = 0; slot < isa.length; slot++)
            inv.add(encodeItemStack(isa[slot]));
        return inv;
    }

    public static ItemStack[] decodeItemStackArray(List<TypeMap> inv) {
        if (inv == null) return null;
        ItemStack[] decoded = new ItemStack[inv.size()];
        for (int slot = 0; slot < inv.size(); slot++)
            decoded[slot] = decodeItemStack(inv.get(slot));
        return decoded;
    }

    public static TypeMap encodeItemStack(ItemStack stack) {
        if (stack == null) return null;
        TypeMap s = new TypeMap();
        s.put("type", stack.getTypeId());
        s.put("amount", stack.getAmount());
        s.put("durability", stack.getDurability());
        MaterialData data = stack.getData();
        if (data != null)
            s.put("data", (int)data.getData());
        TypeMap tag = Global.compatibility.getItemStackTag(stack);
        if (tag != null)
            s.put("tag", tag);
        return s;
    }

    public static ItemStack decodeItemStack(TypeMap s) {
        if (s == null) return null;
        ItemStack stack = Global.compatibility.createItemStack(
                s.getInt("type"),
                s.getInt("amount"),
                (short)s.getInt("durability"));
        if (s.containsKey("data")) {
            MaterialData data = stack.getData();
            if (data != null)
                data.setData((byte)s.getInt("data"));
        }
        if (s.containsKey("tag"))
            stack = Global.compatibility.setItemStackTag(stack, s.getMap("tag"));
        return stack;
    }

    public static String normalizeItem(String item) {
        if (item == null) return null;
        if (item.equals("*")) return item;
        item = item.toUpperCase();
        String parts[] = item.split(":");
        if (parts.length > 2) return null;
        try {
            int typeId = Integer.parseInt(parts[0]);
            Material material = Material.getMaterial(typeId);
            if (material == null) return null;
            item = material.toString();
        } catch (NumberFormatException nfe) {
            try {
                Material material = Utils.valueOf(Material.class, parts[0]);
                item = material.toString();
            } catch (IllegalArgumentException iae) {
                return null;
            }
        }
        if (parts.length > 1)
            try {
                short dura = Short.parseShort(parts[1]);
                item += ":" + dura;
            } catch (NumberFormatException e) {
                return null;
            }
        return item;
    }

    public static boolean appendItemList(Set<String> items, String item) throws InventoryException {
        item = normalizeItem(item);
        if (item == null)
            throw new InventoryException("invalid item");
        if (items.contains(item)) return false;
        items.add(item);
        return true;
    }

    public static boolean removeItemList(Set<String> items, String item) throws InventoryException {
        item = normalizeItem(item);
        if (item == null)
            throw new InventoryException("invalid item");
        if (! items.contains(item)) return false;
        items.remove(item);
        return true;
    }

    public static boolean itemListContains(Set<String> items, String item, boolean matchAir) {
        if (item.equals("*")) return true;
        if ((! matchAir) && item.equals("AIR")) return false;
        String parts[] = item.split(":");
        return items.contains("*") ||
               items.contains(parts[0]) ||
               items.contains(item);
    }

    public static boolean appendItemMap(Map<String,String> items, String fromItem, String toItem) throws InventoryException {
        fromItem = Inventory.normalizeItem(fromItem);
        if (fromItem == null)
            throw new InventoryException("invalid from item");
        toItem = Inventory.normalizeItem(toItem);
        if (toItem == null)
            throw new InventoryException("invalid to item");
        if (items.containsKey(fromItem)) return false;
        items.put(fromItem, toItem);
        return true;
    }

    public static boolean removeItemMap(Map<String,String> items, String fromItem) throws InventoryException {
        fromItem = Inventory.normalizeItem(fromItem);
        if (fromItem == null)
            throw new InventoryException("invalid from item");
        if (! items.containsKey(fromItem)) return false;
        items.remove(fromItem);
        return true;
    }

    public static ItemStack filterItemStack(ItemStack stack, Map<String,String> replace, Set<String> allowed, Set<String> banned) {
        if (stack == null) return null;
        String item = stringifyItemStack(stack);
        if (item == null) return null;
        String newItem;
        String parts[] = item.split(":");
        if (replace != null) {
            if (replace.containsKey(parts[0]))
                newItem = replace.get(parts[0]);
            else
                newItem = replace.get(item);
        } else
            newItem = item;

        if ((newItem != null) && (! newItem.equals("*"))) {
            stack = destringifyItem(stack, newItem);
            item = newItem;
        }
        if ((allowed != null) && (! allowed.isEmpty())) {
            if (itemListContains(allowed, item, true)) return stack;
            return null;
        }
        if (banned != null)
            if (itemListContains(banned, item, false)) return null;
        return stack;
    }

    private static String stringifyItemStack(ItemStack stack) {
        if ((stack == null) || (stack.getType() == null)) return null;
        String item = stack.getType().toString();
        if (stack.getDurability() > 0)
            item += ":" + stack.getDurability();
        return item;
    }

    private static ItemStack destringifyItem(ItemStack oldItem, String item) {
        String[] parts = item.split(":");
        Material material;
        try {
            material = Utils.valueOf(Material.class, parts[0]);
        } catch (IllegalArgumentException iae) {
            material = Material.AIR;
        }
        int amount = oldItem.getAmount();
        short damage = oldItem.getDurability();
        if (parts.length > 1)
            try {
                damage = Short.parseShort(parts[1]);
            } catch (NumberFormatException e) {}
        return new ItemStack(material, amount, damage);
    }

    public static void requireBlocks(Player player, Map<Material,Integer> blocks) throws InventoryException {
        if ((player == null) || (blocks == null) || blocks.isEmpty()) return;
        PlayerInventory inv = player.getInventory();
        for (Material material : blocks.keySet()) {
            int needed = blocks.get(material);
            if (needed <= 0) continue;
            switch (material) {
                case WALL_SIGN:
                case SIGN_POST:
                    material = Material.SIGN;
                    break;
            }
            HashMap<Integer,? extends ItemStack> slots = inv.all(material);
            for (int slotNum : slots.keySet()) {
                ItemStack stack = slots.get(slotNum);
                needed -= stack.getAmount();
                if (needed <= 0) break;
            }
            if (needed > 0)
                throw new InventoryException("need %d more %s", needed, material);
        }
    }

    public static boolean deductBlocks(Player player, Map<Material,Integer> blocks) throws InventoryException {
        if ((player == null) || (blocks == null) || blocks.isEmpty()) return false;
        PlayerInventory inv = player.getInventory();
        for (Material material : blocks.keySet()) {
            int needed = blocks.get(material);
            if (needed <= 0) continue;
            switch (material) {
                case WALL_SIGN:
                case SIGN_POST:
                    material = Material.SIGN;
                    break;
            }
            HashMap<Integer,? extends ItemStack> slots = inv.all(material);
            for (int slotNum : slots.keySet()) {
                ItemStack stack = slots.get(slotNum);
                if (stack.getAmount() > needed) {
                    stack.setAmount(stack.getAmount() - needed);
                    needed = 0;
                } else {
                    needed -= stack.getAmount();
                    inv.clear(slotNum);
                }
                blocks.put(material, needed);
                if (needed <= 0) break;
            }
            if (needed > 0)
                throw new InventoryException("need %d more %s", needed, material);
        }
        return true;
    }

}
