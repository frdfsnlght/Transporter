package com.frdfsnlght.transporter.compatibility.api;


import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface CompatibilityProvider {

    abstract public void sendAllPacket201PlayerInfo(String playerName, boolean b, int i);
    abstract public void sendPlayerPacket201PlayerInfo(Player player, String playerName, boolean b, int i);
    abstract public ItemStack createItemStack(int type, int amount, short durability);
    abstract public TypeMap getItemStackTag(ItemStack stack);
    abstract public ItemStack setItemStackTag(ItemStack stack, TypeMap tag);
}